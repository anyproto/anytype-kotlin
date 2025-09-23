package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
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

class ObjectTypesSubscriptionManager (
    private val scope: CoroutineScope = GlobalScope,
    private val container: ObjectTypesSubscriptionContainer,
    private val spaceManager: SpaceManager
): GlobalSubscription {

    val pipeline get() = spaceManager.state().flatMapLatest { state ->
        when(state) {
            is SpaceManager.State.Space.Active -> {
                val params = buildParams(state.config)
                container.observe(params)
            }
            is SpaceManager.State.Space.Idle -> {
                flow {
                    emit(ObjectTypesSubscriptionContainer.Index.empty())
                }
            }
            is SpaceManager.State.NoSpace -> {
                flow {
                    container.unsubscribe()
                    emit(ObjectTypesSubscriptionContainer.Index.empty())
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
            ObjectTypesSubscriptionContainer.Params(
                space = SpaceId(config.space),
                subscription = ObjectTypesSubscriptionContainer.SUBSCRIPTION_ID,
                filters = listOf(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                    ),
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NONE
                    ),
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EMPTY
                    ),
                ),
                limit = 0,
                offset = 0L,
                sorts = emptyList(),
                sources = emptyList(),
                keys = listOf(
                    Relations.ID,
                    Relations.NAME,
                    Relations.PLURAL_NAME,
                    Relations.IS_HIDDEN,
                    Relations.IS_DELETED,
                    Relations.IS_ARCHIVED,
                    Relations.SMARTBLOCKTYPES,
                    Relations.LAYOUT,
                    Relations.DESCRIPTION,
                    Relations.ICON_EMOJI,
                    Relations.ICON_NAME,
                    Relations.ICON_OPTION,
                    Relations.SOURCE_OBJECT,
                    Relations.IS_READ_ONLY,
                    Relations.RECOMMENDED_LAYOUT,
                    Relations.DEFAULT_TEMPLATE_ID,
                    Relations.SPACE_ID,
                    Relations.UNIQUE_KEY,
                    Relations.RESTRICTIONS,
                    Relations.TARGET_SPACE_ID,
                    Relations.TYPE,
                    Relations.RECOMMENDED_RELATIONS,
                    Relations.RECOMMENDED_FEATURED_RELATIONS,
                    Relations.RECOMMENDED_HIDDEN_RELATIONS,
                    Relations.RECOMMENDED_FILE_RELATIONS,
                    Relations.IS_ARCHIVED,
                    Relations.WIDGET_LAYOUT,
                    Relations.WIDGET_LIMIT,
                    Relations.WIDGET_VIEW_ID
                    ),
                ignoreWorkspace = true
            )
    }
}