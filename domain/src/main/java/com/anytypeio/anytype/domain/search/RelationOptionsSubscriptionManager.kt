package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscription
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class RelationOptionsSubscriptionManager(
    private val scope: CoroutineScope = GlobalScope,
    private val container: RelationOptionsSubscriptionContainer,
    private val spaceManager: SpaceManager
) : GlobalSubscription {

    val pipeline get() = spaceManager.state().flatMapLatest { state ->
        when (state) {
            is SpaceManager.State.Space.Active -> {
                val params = buildParams(state.config)
                container.observe(params)
            }
            is SpaceManager.State.Space.Idle -> {
                flow {
                    emit(RelationOptionsSubscriptionContainer.Index.empty())
                }
            }
            is SpaceManager.State.NoSpace -> {
                flow {
                    container.unsubscribe()
                    emit(RelationOptionsSubscriptionContainer.Index.empty())
                }
            }
            is SpaceManager.State.Init -> {
                emptyFlow()
            }
        }
    }

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch {
            pipeline.collect()
        }
    }

    fun onStop() {
        scope.launch {
            container.unsubscribe()
            job?.cancel()
            job = null
        }
    }

    companion object {
        fun buildParams(config: Config) =
            RelationOptionsSubscriptionContainer.Params(
                space = SpaceId(config.space),
                subscription = RelationOptionsSubscriptionContainer.SUBSCRIPTION_ID,
                filters = listOf(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.RELATION_OPTION.code.toDouble()
                    ),
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_HIDDEN,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_HIDDEN_DISCOVERY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                ),
                limit = 0,
                offset = 0L,
                sorts = listOf(
                    DVSort(
                        relationKey = Relations.RELATION_OPTION_ORDER,
                        type = DVSortType.ASC,
                        relationFormat = RelationFormat.LONG_TEXT
                    ),
                    DVSort(
                        relationKey = Relations.NAME,
                        type = DVSortType.ASC,
                        relationFormat = RelationFormat.LONG_TEXT
                    )
                ),
                sources = emptyList(),
                keys = listOf(
                    Relations.ID,
                    Relations.SPACE_ID,
                    Relations.NAME,
                    Relations.RELATION_OPTION_COLOR,
                    Relations.RELATION_KEY,
                    Relations.RELATION_OPTION_ORDER,
                    Relations.ORDER_ID
                ),
                ignoreWorkspace = true
            )
    }
}
