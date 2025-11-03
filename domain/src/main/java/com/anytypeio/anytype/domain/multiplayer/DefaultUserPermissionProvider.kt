package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * App-scoped and auth-scoped user-as-space-member permission store.
 * Provides user-as-space-member permissions.
 */
interface UserPermissionProvider  {
    /**
     * Starts user-permission flow — should be scoped to the auth session.
     */
    fun start()
    /**
     * Stops user-permission flow — should be scoped to the auth session.
     */
    fun stop()

    /**
     * @return [SpaceMemberPermissions] for [space] or null if user permission could not be defined.
     */
    fun get(space: SpaceId) : SpaceMemberPermissions?

    /**
     * @return [SpaceMemberPermissions] for [space] or null if user permission could not be defined.
     */
    fun observe(space: SpaceId) : Flow<SpaceMemberPermissions?>

    /**
     * @return Flow of the current user's [ObjectWrapper.SpaceMember] or null if not available.
     */
    fun getCurrent() : Flow<ObjectWrapper.SpaceMember?>

    /**
     * Provide permissions of the current user in all available spaces.
     * Maps space to permissions.
     */
    fun all() : Flow<Map<Id, SpaceMemberPermissions>>
}

class DefaultUserPermissionProvider @Inject constructor(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val container: StorelessSubscriptionContainer,
    private val repo: AuthRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope,
    private val logger: Logger
) : UserPermissionProvider {

    /**
     * User-as-member in all available spaces
     */
    private val members = MutableStateFlow<List<ObjectWrapper.SpaceMember>>(emptyList())
    private val jobs = mutableListOf<Job>()

    override fun get(space: SpaceId): SpaceMemberPermissions? {
        return members.value.firstOrNull { member -> member.spaceId == space.id }?.permissions
    }
    override fun observe(space: SpaceId): Flow<SpaceMemberPermissions?> {
        return members.map { all ->
            all.firstOrNull { member -> member.spaceId == space.id }?.permissions
        }
    }

    override fun getCurrent(): Flow<ObjectWrapper.SpaceMember?> {
        return members.map { all -> all.firstOrNull() }
    }

    override fun start() {
        logger.logInfo("Starting DefaultUserPermissionProvider")
        clear()
        jobs += scope.launch(dispatchers.io) {
            val account = repo.getCurrentAccountId()
            spaceViewSubscriptionContainer
                .observe()
                .map { spaceViews ->
                    spaceViews.mapNotNull { spaceView -> spaceView.targetSpaceId }
                }
                .distinctUntilChanged()
                .flatMapLatest { spaces ->
                    val subscriptions = spaces.map { space ->
                        container.subscribe(
                            StoreSearchParams(
                                space = SpaceId(space),
                                subscription = "$GLOBAL_SUBSCRIPTION-$space",
                                filters = buildList {
                                    add(
                                        DVFilter(
                                            relation = Relations.LAYOUT,
                                            value = ObjectType.Layout.PARTICIPANT.code.toDouble(),
                                            condition = DVFilterCondition.EQUAL
                                        )
                                    )
                                    add(
                                        DVFilter(
                                            relation = Relations.IDENTITY,
                                            value = account,
                                            condition = DVFilterCondition.EQUAL
                                        )
                                    )
                                },
                                limit = 1,
                                keys = listOf(
                                    Relations.ID,
                                    Relations.SPACE_ID,
                                    Relations.IDENTITY,
                                    Relations.PARTICIPANT_PERMISSIONS,
                                    Relations.GLOBAL_NAME
                                )
                            )
                        ).map { results ->
                            results.map { ObjectWrapper.SpaceMember(it.map) }
                        }
                    }
                    combine(flows = subscriptions) { flow ->
                        flow.toList().flatten()
                    }
                }.catch { error ->
                    logger.logException(
                        e = error,
                        msg = "Failed to subscribe to user-as-space-member-permissions"
                    )
                }.collect {
                    members.value = it
                }
        }
    }

    override fun all(): Flow<Map<Id, SpaceMemberPermissions>> {
        return members.map { all ->
            all.filter { member ->
                !member.spaceId.isNullOrEmpty()
            }.associate { member ->
                val space = requireNotNull(member.spaceId)
                val permissions = member.permissions ?: SpaceMemberPermissions.NO_PERMISSIONS
                space to permissions
            }
        }
    }

    override fun stop() {
        clear()
        scope.launch(dispatchers.io) {
            val subscriptions = spaceViewSubscriptionContainer
                .get()
                .mapNotNull { it.targetSpaceId }
                .map { space -> "$GLOBAL_SUBSCRIPTION-$space" }
            container.unsubscribe(subscriptions)
        }
    }

    private fun clear() {
        jobs.forEach { it.cancel() }
    }

    companion object {
        const val GLOBAL_SUBSCRIPTION = "subscription.global.user-as-space-member-permissions"
    }
}

typealias Permissions = UserPermissionProvider