package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface ActiveSpaceMemberSubscriptionContainer {

    fun start()
    fun stop()
    fun observe() : Flow<List<ObjectWrapper.SpaceMember>>
    fun get() : List<ObjectWrapper.SpaceMember>

    class Default @Inject constructor(
        private val manager: SpaceManager,
        private val container: StorelessSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
    ) : ActiveSpaceMemberSubscriptionContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.SpaceMember>>(emptyList())
        private val jobs = mutableListOf<Job>()

        override fun observe(): Flow<List<ObjectWrapper.SpaceMember>> {
            return data
        }

        override fun get(): List<ObjectWrapper.SpaceMember> {
            return data.value
        }

        override fun start() {
            jobs += scope.launch(dispatchers.io) {
                manager
                    .observe()
                    .flatMapLatest { config ->
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
                                            relation = Relations.SPACE_ID,
                                            condition = DVFilterCondition.EQUAL,
                                            value = config.space
                                        )
                                    )
                                },
                                limit = 0,
                                keys = listOf(
                                    Relations.ID,
                                    Relations.SPACE_ID,
                                    Relations.IDENTITY,
                                    Relations.PARTICIPANT_PERMISSIONS,
                                    Relations.LAYOUT,
                                    Relations.NAME,
                                    Relations.ICON_IMAGE,
                                )
                            )
                        )
                    }.map { objects ->
                        objects.map { obj ->
                            ObjectWrapper.SpaceMember(obj.map)
                        }
                    }.collect {
                        data.value = it
                    }
            }
        }

        override fun stop() {
            jobs.forEach { it.cancel() }
            scope.launch(dispatchers.io) {
                container.unsubscribe(listOf(DefaultUserPermissionProvider.GLOBAL_SUBSCRIPTION))
            }
        }

        companion object {
            const val GLOBAL_SUBSCRIPTION = "subscription.global.active-space-members"
        }
    }
}