package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * App-scoped and auth-scoped user-as-space-member permission provider.
 * Provide user-as-space-member permissions for the currently active space.
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
     * Get current space member permission for currently active space.
     * @return null if user permission could not be defined.
     */
    fun get() : SpaceMemberPermissions?
    /**
     * Subscribes to the current space member permission for currently active space.
     * @return null if user permission could not be defined.
     */
    fun observe() : Flow<SpaceMemberPermissions?>
}

class DefaultUserPermissionProvider @Inject constructor(
    private val container: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager,
    private val repo: AuthRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope
) : UserPermissionProvider {

    val permission = MutableStateFlow<SpaceMemberPermissions?>(null)
    val jobs = mutableListOf<Job>()

    override fun get(): SpaceMemberPermissions? = permission.value
    override fun observe(): Flow<SpaceMemberPermissions?> = permission

    override fun start() {
        jobs.forEach { it.cancel() }
        jobs += scope.launch(dispatchers.io) {
            val account = repo.getCurrentAccountId()
            check(account.isNotEmpty()) { "No account data." }
            spaceManager
                .observe()
                .onEach { permission.value = null }
                .flatMapLatest { config ->
                    container.subscribe(
                        StoreSearchParams(
                            subscription = SUBSCRIPTION,
                            filters = buildList {
                                add(
                                    DVFilter(
                                        relation = Relations.SPACE_ID,
                                        value = config.space,
                                        condition = DVFilterCondition.EQUAL
                                    )
                                )
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
                                Relations.IDENTITY,
                                Relations.PARTICIPANT_PERMISSIONS
                            )
                        )
                    )
                }.collect { results ->
                    val obj = results.firstOrNull()
                    if (obj != null) {
                        val member = ObjectWrapper.SpaceMember(obj.map)
                        permission.value = member.permissions
                    }
                }
        }
    }

    override fun stop() {
        scope.launch(dispatchers.io) {
            container.unsubscribe(listOf(SUBSCRIPTION))
        }
    }

    companion object {
        const val SUBSCRIPTION = "subscription.global.user-as-space-member-permissions"
    }
}