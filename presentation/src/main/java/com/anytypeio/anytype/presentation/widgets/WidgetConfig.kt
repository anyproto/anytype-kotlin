package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.objects.SupportedLayouts

object WidgetConfig {
    val excludedTypes = listOf(
        ObjectTypeIds.OBJECT_TYPE,
        ObjectTypeIds.RELATION,
        ObjectTypeIds.TEMPLATE,
        ObjectTypeIds.IMAGE,
        ObjectTypeIds.FILE,
        ObjectTypeIds.VIDEO,
        ObjectTypeIds.AUDIO,
        ObjectTypeIds.DASHBOARD,
        ObjectTypeIds.DATE,
        ObjectTypeIds.RELATION_OPTION
    )

    fun isValidObject(obj: ObjectWrapper.Basic): Boolean {
        return !excludedTypes.contains(obj.type.firstOrNull())
                && obj.isArchived != true
                && obj.isDeleted != true
                && SupportedLayouts.isSupported(obj.layout)
    }
}

object BundledWidgetSourceIds {
    const val FAVORITE = "favorite"
    const val RECENT = "recent"
    const val SETS = "sets"
    val ids = listOf(FAVORITE, RECENT, SETS)
}