package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
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
}

class DefaultUserPermissionProvider @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val repo: AuthRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope,
    private val logger: Logger
) : UserPermissionProvider {

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

    override fun start() {
        clear()
        jobs += scope.launch(dispatchers.io) {
            val account = repo.getCurrentAccountId()
            container.subscribe(
                StoreSearchParams(
                    subscription = GLOBAL_SUBSCRIPTION,
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
                    limit = NO_LIMIT,
                    keys = listOf(
                        Relations.ID,
                        Relations.SPACE_ID,
                        Relations.IDENTITY,
                        Relations.PARTICIPANT_PERMISSIONS
                    )
                )
            ).collect { results ->
                members.value = results.map {
                    ObjectWrapper.SpaceMember(it.map)
                }
            }
        }
    }

    override fun stop() {
        clear()
        scope.launch(dispatchers.io) {
            container.unsubscribe(listOf(GLOBAL_SUBSCRIPTION))
        }
    }

    private fun clear() {
        jobs.forEach { it.cancel() }
    }

    companion object {
        const val GLOBAL_SUBSCRIPTION = "subscription.global.user-as-space-member-permissions"
        const val NO_LIMIT = 0
    }
}