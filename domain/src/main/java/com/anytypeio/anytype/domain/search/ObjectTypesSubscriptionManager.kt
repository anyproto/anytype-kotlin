package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ObjectTypesSubscriptionManager (
    private val scope: CoroutineScope = GlobalScope,
    private val subscription: ObjectTypesSubscriptionContainer,
    private val workspaceManager: WorkspaceManager
) {

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch {
            val params = ObjectTypesSubscriptionContainer.Params(
                subscription = ObjectTypesSubscriptionContainer.SUBSCRIPTION_ID,
                filters = listOf(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.OBJECT_TYPE
                    ),
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = workspaceManager.getCurrentWorkspace()
                    )
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
                    Relations.DEFAULT_TEMPLATE_ID
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