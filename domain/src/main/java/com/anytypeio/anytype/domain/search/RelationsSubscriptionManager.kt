package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscription
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


class RelationsSubscriptionManager @Inject constructor(
    private val scope: CoroutineScope = GlobalScope,
    private val container: RelationsSubscriptionContainer,
    private val spaceManager: SpaceManager
): GlobalSubscription {
    val pipeline get() = spaceManager.state().flatMapLatest { state ->
        when(state) {
            is SpaceManager.State.Space.Active -> {
                val params = buildParams(
                    spaces = listOf(
                        state.config.space,
                        state.config.techSpace,
                        Marketplace.MARKETPLACE_SPACE_ID
                    )
                )
                container.observe(params)
            }
            is SpaceManager.State.Space.Idle -> {
                flow {
                    emit(RelationsSubscriptionContainer.Index.empty())
                }
            }
            is SpaceManager.State.NoSpace -> {
                flow {
                    container.unsubscribe()
                    emit(RelationsSubscriptionContainer.Index.empty())
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
        job = scope.launch { pipeline.collect() }
    }

    fun onStop() {
        scope.launch {
            container.unsubscribe()
            job?.cancel()
            job = null
        }
    }

    companion object {
        fun buildParams(spaces: List<Id>) = RelationsSubscriptionContainer.Params(
            subscription = RelationsSubscriptionContainer.SUBSCRIPTION_ID,
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.RELATION.code.toDouble()
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
                    relation = Relations.SPACE_ID,
                    condition = DVFilterCondition.IN,
                    value = spaces
                )
            ),
            limit = 0,
            offset = 0L,
            sorts = emptyList(),
            sources = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.SPACE_ID,
                Relations.TYPE,
                Relations.LAYOUT,
                Relations.NAME,
                Relations.RELATION_FORMAT,
                Relations.RELATION_KEY,
                Relations.SCOPE,
                Relations.IS_READ_ONLY,
                Relations.IS_HIDDEN,
                Relations.IS_DELETED,
                Relations.IS_ARCHIVED,
                Relations.IS_FAVORITE,
                Relations.RESTRICTIONS,
                Relations.MAX_COUNT,
                Relations.RELATION_READ_ONLY_VALUE,
                Relations.RELATION_DEFAULT_VALUE,
                Relations.RELATION_FORMAT_OBJECT_TYPES,
                Relations.SOURCE_OBJECT
            ),
            ignoreWorkspace = true
        )
    }
}