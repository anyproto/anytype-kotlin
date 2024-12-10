package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts

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
                && obj.isValid
                && obj.isArchived != true
                && obj.isDeleted != true
                && SupportedLayouts.isSupportedForWidgets(obj.layout)
    }

    fun resolveListWidgetLimit(
        isCompact: Boolean,
        isGallery: Boolean = false,
        limit: Int
    ) : Int {
        return if (isCompact || isGallery) {
            if (compactListLimitOptions.contains(limit)) {
                limit
            } else {
                DEFAULT_COMPACT_LIST_LIMIT
            }
        } else {
            if (listLimitOptions.contains(limit)) {
                limit
            } else {
                DEFAULT_LIST_LIMIT
            }
        }
    }

    fun resolveTreeWidgetLimit(limit: Int) : Int {
        return if (treeLimitOptions.contains(limit))
            limit
        else
            DEFAULT_TREE_LIMIT
    }

    const val NO_LIMIT = 0
    const val DEFAULT_LIST_LIMIT = 4
    private const val DEFAULT_COMPACT_LIST_LIMIT = 6
    const val DEFAULT_TREE_LIMIT = 6

    private val listLimitOptions = intArrayOf(DEFAULT_LIST_LIMIT, 6, 8)
    private val compactListLimitOptions = intArrayOf(DEFAULT_COMPACT_LIST_LIMIT, 10, 14)
    private val treeLimitOptions = intArrayOf(DEFAULT_TREE_LIMIT, 10, 14)
}

object BundledWidgetSourceIds {
    const val FAVORITE = "favorite"
    const val RECENT = "recent"
    const val RECENT_LOCAL = "recentOpen"
    const val SETS = "set"
    const val COLLECTIONS = "collection"
    val ids = listOf(FAVORITE, RECENT, RECENT_LOCAL, SETS, COLLECTIONS)
}