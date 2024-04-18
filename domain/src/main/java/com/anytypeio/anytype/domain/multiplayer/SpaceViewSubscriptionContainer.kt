package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface SpaceViewSubscriptionContainer {

    fun start()
    fun stop()
    fun observe(): Flow<List<ObjectWrapper.SpaceView>>
    fun get(): List<ObjectWrapper.SpaceView>

    class Default @Inject constructor(
        private val container: StorelessSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
    ) : SpaceViewSubscriptionContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
        private val jobs = mutableListOf<Job>()

        override fun observe(): Flow<List<ObjectWrapper.SpaceView>> {
            return data
        }

        override fun get(): List<ObjectWrapper.SpaceView> {
            return data.value
        }

        override fun start() {
            jobs += scope.launch(dispatchers.io) {
                container.subscribe(
                    StoreSearchParams(
                        subscription = GLOBAL_SUBSCRIPTION,
                        keys = listOf(
                            Relations.ID,
                            Relations.TARGET_SPACE_ID,
                            Relations.SPACE_ACCOUNT_STATUS,
                            Relations.SPACE_LOCAL_STATUS,
                            Relations.SPACE_ACCESS_TYPE,
                            Relations.SHARED_SPACES_LIMIT,
                            Relations.READERS_LIMIT,
                            Relations.WRITERS_LIMIT,
                            Relations.NAME,
                            Relations.CREATED_DATE,
                            Relations.CREATOR,
                            Relations.ICON_IMAGE,
                            Relations.ICON_OPTION,
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
                                    add(SpaceStatus.SPACE_REMOVING.code.toDouble())
                                },
                                condition = DVFilterCondition.NOT_IN
                            ),
                            DVFilter(
                                relation = Relations.SPACE_LOCAL_STATUS,
                                value = SpaceStatus.OK.code.toDouble(),
                                condition = DVFilterCondition.EQUAL
                            )
                        ),
                        sorts = listOf(
                            DVSort(
                                relationKey = Relations.LAST_OPENED_DATE,
                                type = DVSortType.DESC,
                                includeTime = true
                            )
                        )
                    )
                ).map { objects ->
                    objects.map { obj ->
                        ObjectWrapper.SpaceView(obj.map)
                    }
                }.collect {
                    data.value = it
                }
            }
        }

        override fun stop() {
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