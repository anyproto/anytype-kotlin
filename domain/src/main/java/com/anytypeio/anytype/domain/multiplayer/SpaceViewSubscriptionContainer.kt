package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType.SHARED
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions.OWNER
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

interface SpaceViewSubscriptionContainer {

    fun start()
    fun stop()
    fun observe(): Flow<List<ObjectWrapper.SpaceView>>
    fun observe(space: SpaceId) : Flow<ObjectWrapper.SpaceView>

    fun get(): List<ObjectWrapper.SpaceView>
    fun get(space: SpaceId) : ObjectWrapper.SpaceView?

    class Default @Inject constructor(
        private val container: StorelessSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
        private val awaitAccountStart: AwaitAccountStartManager,
        private val logger: Logger,
        private val config: ConfigStorage
    ) : SpaceViewSubscriptionContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
        private val jobs = mutableListOf<Job>()

        init {
            logger.logInfo("SpaceViewSubscriptionContainer initialized")
            scope.launch {
                awaitAccountStart.state().collect { state ->
                    when(state) {
                        AwaitAccountStartManager.State.Init -> {
                            logger.logInfo("AwaitAccountStartManager.State.Init - waiting for account start")
                            // Do nothing
                        }
                        AwaitAccountStartManager.State.Started -> {
                            logger.logInfo("AwaitAccountStartManager.State.Started - starting subscription")
                            start()
                        }
                        AwaitAccountStartManager.State.Stopped -> {
                            logger.logInfo("AwaitAccountStartManager.State.Stopped - stopping subscription")
                            stop()
                        }
                    }
                }
            }
        }

        override fun observe(): Flow<List<ObjectWrapper.SpaceView>> {
            return data
        }

        override fun observe(space: SpaceId): Flow<ObjectWrapper.SpaceView> {
            return data.mapNotNull { all ->
                all.firstOrNull { spaceView -> spaceView.targetSpaceId == space.id }
            }
        }

        override fun get(): List<ObjectWrapper.SpaceView> {
            return data.value
        }

        override fun get(space: SpaceId): ObjectWrapper.SpaceView? {
            return data.value.find { spaceView -> spaceView.targetSpaceId == space.id }
        }

        override fun start() {
            logger.logInfo("Starting SpaceViewSubscriptionContainer")
            jobs += scope.launch(dispatchers.io) {
                val techSpace = config.getOrNull()?.techSpace
                if (techSpace != null) {
                    proceedWithSubscription(techSpace)
                } else {
                    logger.logException(IllegalStateException("Tech space was missing"))
                }
            }
        }

        private suspend fun proceedWithSubscription(techSpace: Id) {
            container.subscribe(
                StoreSearchParams(
                    space = SpaceId(techSpace),
                    subscription = GLOBAL_SUBSCRIPTION,
                    keys = listOf(
                        Relations.ID,
                        Relations.SPACE_UX_TYPE,
                        Relations.TARGET_SPACE_ID,
                        Relations.CHAT_ID,
                        Relations.SPACE_ACCOUNT_STATUS,
                        Relations.SPACE_LOCAL_STATUS,
                        Relations.SPACE_ACCESS_TYPE,
                        Relations.SHARED_SPACES_LIMIT,
                        Relations.READERS_LIMIT,
                        Relations.WRITERS_LIMIT,
                        Relations.NAME,
                        Relations.PLURAL_NAME,
                        Relations.CREATED_DATE,
                        Relations.CREATOR,
                        Relations.ICON_IMAGE,
                        Relations.ICON_OPTION,
                        Relations.SPACE_PUSH_NOTIFICATIONS_KEY,
                        Relations.SPACE_PUSH_NOTIFICATIONS_TOPIC,
                        Relations.SPACE_PUSH_NOTIFICATION_MODE,
                        Relations.SPACE_ORDER
                    ),
                    filters = listOf(
                        DVFilter(
                            relation = Relations.LAYOUT,
                            value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                            condition = DVFilterCondition.EQUAL
                        ),
                        DVFilter(
                            relation = Relations.SPACE_ACCOUNT_STATUS,
                            value = buildList {
                                add(SpaceStatus.SPACE_DELETED.code.toDouble())
                            },
                            condition = DVFilterCondition.NOT_IN
                        ),
                        DVFilter(
                            relation = Relations.SPACE_LOCAL_STATUS,
                            value = buildList {
                                add(SpaceStatus.OK.code.toDouble())
                                add(SpaceStatus.UNKNOWN.code.toDouble())
                                add(SpaceStatus.LOADING.code.toDouble())
                            },
                            condition = DVFilterCondition.IN
                        )
                    ),
                    sorts = listOf(
                        DVSort(
                            relationKey = Relations.LAST_OPENED_DATE,
                            type = DVSortType.DESC,
                            includeTime = true,
                            relationFormat = RelationFormat.DATE
                        )
                    )
                )
            ).map { objects ->
                objects.map { obj ->
                    ObjectWrapper.SpaceView(obj.map)
                }
            }.catch { error ->
                logger.logException(
                    e = error,
                    msg = "Failed to subscribe to space-views"
                )
            }.collect {
                data.value = it
            }
        }

        override fun stop() {
            logger.logInfo("Stopping SpaceViewSubscriptionContainer")
            jobs.forEach { it.cancel() }
            scope.launch(dispatchers.io) {
                container.unsubscribe(listOf(GLOBAL_SUBSCRIPTION))
            }
        }

        companion object {
            const val GLOBAL_SUBSCRIPTION = "subscription.global.space-views"
        }
    }
}

@Deprecated("To de deleted")
fun SpaceViewSubscriptionContainer.isSharingLimitReached(
    spaceToUserPermissions: Flow<Map<Id, SpaceMemberPermissions>>
) : Flow<Pair<Boolean, Int>> {
    val sharedSpacesCount = combine(
        observe(),
        spaceToUserPermissions
    ) { spaceViews, permissions ->
        spaceViews.count { spaceView ->
            val permission = permissions[spaceView.targetSpaceId]
            spaceView.spaceAccessType == SHARED && permission == OWNER
        }
    }
    val sharedSpaceLimit = observe().map { spaceViews ->
        val defaultSpace = spaceViews.firstOrNull { space ->
            space.spaceAccessType == SpaceAccessType.DEFAULT
        }
        defaultSpace?.sharedSpaceLimit ?: 0
    }
    return combine(
        sharedSpaceLimit,
        sharedSpacesCount
    ) { limit, count ->
        val isLimitReached = limit == 0 || count >= limit
        Pair(isLimitReached, limit)
    }
}

fun SpaceViewSubscriptionContainer.sharedSpaceCount(
    spaceToUserPermissions: Flow<Map<Id, SpaceMemberPermissions>>
) : Flow<Int> {
    return combine(
        observe(),
        spaceToUserPermissions
    ) { spaceViews, permissions ->
        spaceViews.count { spaceView ->
            val permission = permissions[spaceView.targetSpaceId]
            spaceView.spaceAccessType == SHARED && permission == OWNER
        }
    }
}

