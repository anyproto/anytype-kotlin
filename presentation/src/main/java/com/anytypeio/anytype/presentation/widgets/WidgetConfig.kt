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

    fun resolveListWidgetLimit(isCompact: Boolean, limit: Int) : Int {
        return if (isCompact) {
            if (compactListLimitOptions.contains(limit)) {
                limit
            } else {
                DEFAULT_COMPACT_LIST_MAX_COUNT
            }
        } else {
            if (listLimitOptions.contains(limit)) {
                limit
            } else {
                DEFAULT_LIST_MAX_COUNT
            }
        }
    }

    const val DEFAULT_LIST_MAX_COUNT = 4
    const val DEFAULT_COMPACT_LIST_MAX_COUNT = 6

    private val listLimitOptions = intArrayOf(DEFAULT_LIST_MAX_COUNT, 6, 8)
    private val compactListLimitOptions = intArrayOf(DEFAULT_COMPACT_LIST_MAX_COUNT, 10, 14)
}

object BundledWidgetSourceIds {
    const val FAVORITE = "favorite"
    const val RECENT = "recent"
    const val SETS = "set"
    const val COLLECTIONS = "collection"
    val ids = listOf(FAVORITE, RECENT, SETS, COLLECTIONS)
}