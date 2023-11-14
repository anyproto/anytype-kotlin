package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SpaceViewSubscriptionContainer(
    private val container: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager,
    private val dispatchers: AppCoroutineDispatchers
) {

    private val scope = MainScope()
    private val jobs = mutableListOf<Job>()

    fun onStart() {
        jobs += scope.launch(dispatchers.io) {
            spaceManager
                .observe()
                .flatMapLatest { config ->
                    container.subscribe(
                        searchParams = StoreSearchByIdsParams(
                            subscription = GLOBAL_SPACE_VIEW_SUBSCRIPTION,
                            targets = listOf(config.spaceView),
                            keys = buildList {
                                add(Relations.ID)
                                add(Relations.TARGET_SPACE_ID)
                                add(Relations.SPACE_ACCOUNT_STATUS)
                            }
                        )
                    )
                }
                .mapNotNull { results -> results.firstOrNull() }
                .onEach { result ->
                    val spaceView = result.let {
                        ObjectWrapper.SpaceView(it.map)
                    }
                    if (spaceView.spaceAccountStatus == SpaceStatus.SPACE_DELETED) {
                        spaceManager.clear()
                    }
                }
        }
    }

    fun onStop() {
        scope.launch(dispatchers.io) {
            container.unsubscribe(listOf(GLOBAL_SPACE_VIEW_SUBSCRIPTION))
            with(jobs) {
                forEach { it.cancel() }
                clear()
            }
        }
    }

    companion object {
        const val GLOBAL_SPACE_VIEW_SUBSCRIPTION = "subscription.global.space-view"
    }
}