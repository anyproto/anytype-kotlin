package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ObjectTypesSubscriptionManager(
    private val scope: CoroutineScope = GlobalScope,
    private val subscription: ObjectTypesSubscriptionContainer
) {

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch {
            val params = ObjectTypesSubscriptionContainer.Params(
                subscription = RelationsSubscriptionContainer.SUBSCRIPTION_ID,
                filters = listOf(
                    DVFilter(
                        relationKey = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.OBJECT_TYPE
                    ),
                    DVFilter(
                        relationKey = Relations.IS_DELETED,
                        condition = DVFilterCondition.EQUAL,
                        value = false
                    ),
                    DVFilter(
                        relationKey = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.EQUAL,
                        value = false
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
                    Relations.ICON_EMOJI
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