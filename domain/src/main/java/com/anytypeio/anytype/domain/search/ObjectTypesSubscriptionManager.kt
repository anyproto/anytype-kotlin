package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ObjectTypesSubscriptionManager (
    private val scope: CoroutineScope = GlobalScope,
    private val container: ObjectTypesSubscriptionContainer,
    private val spaceManager: SpaceManager
) {

    private val pipeline = spaceManager.observe().flatMapLatest { config ->
        val params = buildParams(config)
        container.observe(params)
    }

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch { pipeline.collect() }
    }

    private fun buildParams(config: Config) =
        ObjectTypesSubscriptionContainer.Params(
            subscription = ObjectTypesSubscriptionContainer.SUBSCRIPTION_ID,
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.EQUAL,
                    value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                ),
                DVFilter(
                    relation = Relations.SPACE_ID,
                    condition = DVFilterCondition.EQUAL,
                    value = config.space
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
                Relations.IS_HIDDEN,
                Relations.IS_DELETED,
                Relations.IS_ARCHIVED,
                Relations.SMARTBLOCKTYPES,
                Relations.LAYOUT,
                Relations.DESCRIPTION,
                Relations.ICON_EMOJI,
                Relations.SOURCE_OBJECT,
                Relations.IS_READ_ONLY,
                Relations.RECOMMENDED_LAYOUT,
                Relations.DEFAULT_TEMPLATE_ID,
                Relations.SPACE_ID,
                Relations.UNIQUE_KEY,
                Relations.RESTRICTIONS
            ),
            ignoreWorkspace = true
        )

    fun onStop() {
        scope.launch {
            container.unsubscribe()
            job?.cancel()
            job = null
        }
    }
}