package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import timber.log.Timber

fun ObjectState.DataView.getActiveViewTypeAndTemplate(
    ctx: Id,
    activeView: DVViewer
): Pair<ObjectWrapper.Type?, Id?> {
    when (this) {
        is ObjectState.DataView.Collection -> TODO()
        is ObjectState.DataView.Set -> {
            val setOfValue = getSetOfValue(ctx)
            return if (isSetByRelation(setOfValue = setOfValue)) {
                Pair(null, null)
                //viewer?.defaultObjectType ?: VIEW_DEFAULT_OBJECT_TYPE
            } else {
                val setOf = setOfValue.firstOrNull()
                if (setOf.isNullOrBlank()) {
                    Timber.d("Set by type setOf param is null or empty, not possible to get Type and Template")
                    Pair(null, null)
                } else {
                    val defaultSetObjectType = ObjectWrapper.Type(details[setOf]?.map.orEmpty())
                    if (activeView.defaultTemplate.isNullOrEmpty()) {
                        val defaultTemplateId = defaultSetObjectType.defaultTemplateId
                        Pair(defaultSetObjectType, defaultTemplateId)
                    } else {
                        Pair(defaultSetObjectType, activeView.defaultTemplate)
                    }
                }
            }
        }
    }
}
