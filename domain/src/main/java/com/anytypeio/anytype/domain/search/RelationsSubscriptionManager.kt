package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


class RelationsSubscriptionManager @Inject constructor(
    private val scope: CoroutineScope = GlobalScope,
    private val container: RelationsSubscriptionContainer,
    private val spaceManager: SpaceManager
) {
    private val pipeline = spaceManager.observe().flatMapLatest { config ->
        val params = buildParams(config.space)
        container.observe(params)
    }

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch { pipeline.collect() }
    }

    private fun buildParams(space: Id) = RelationsSubscriptionContainer.Params(
        subscription = RelationsSubscriptionContainer.SUBSCRIPTION_ID,
        filters = listOf(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.EQUAL,
                value = ObjectType.Layout.RELATION.code.toDouble()
            ),
            DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.EQUAL,
                value = false
            ),
            DVFilter(
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.EQUAL,
                value = space
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
            Relations.RELATION_FORMAT_OBJECT_TYPES
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