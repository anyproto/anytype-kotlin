package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class RelationsSubscriptionManager @Inject constructor(
    private val scope: CoroutineScope = GlobalScope,
    private val subscription: RelationsSubscriptionContainer,
    private val spaceManager: SpaceManager
) {

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch {
            val params = RelationsSubscriptionContainer.Params(
                subscription = RelationsSubscriptionContainer.SUBSCRIPTION_ID,
                filters = listOf(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.RELATION
                    ),
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.EQUAL,
                        value = false
                    ),
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = spaceManager.get()
                    )
                ),
                limit = 0,
                offset = 0L,
                sorts = emptyList(),
                sources = emptyList(),
                keys = listOf(
                    Relations.ID,
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
            subscription.observe(params).collect()
        }
    }

    fun onStop() {
        job?.cancel()
        job = null
    }
}