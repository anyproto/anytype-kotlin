package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts

object WidgetConfig {
    val excludedTypes = listOf(
        ObjectTypeIds.OBJECT_TYPE,
        ObjectTypeIds.RELATION,
        ObjectTypeIds.TEMPLATE,
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

    fun isLinkOnlyLayout(code: Int): Boolean {
        return code == ObjectType.Layout.DATE.code ||
                code == ObjectType.Layout.PARTICIPANT.code ||
                code == ObjectType.Layout.IMAGE.code ||
                code == ObjectType.Layout.VIDEO.code ||
                code == ObjectType.Layout.AUDIO.code ||
                code == ObjectType.Layout.FILE.code
    }

    /**
     * The limit comes from the widget block, and any client can set it.
     * The desktop client offers more options (for example 30 and 50) than this client knows,
     * so every positive limit is valid. The default applies only when the limit is not set.
     */
    fun resolveListWidgetLimit(
        isCompact: Boolean,
        isGallery: Boolean = false,
        limit: Int
    ) : Int {
        return when {
            limit > NO_LIMIT -> limit
            isCompact || isGallery -> DEFAULT_COMPACT_LIST_LIMIT
            else -> DEFAULT_LIST_LIMIT
        }
    }

    fun resolveTreeWidgetLimit(limit: Int) : Int {
        return if (limit > NO_LIMIT) limit else DEFAULT_TREE_LIMIT
    }

    const val NO_LIMIT = 0
    const val DEFAULT_LIST_LIMIT = 4
    const val DEFAULT_COMPACT_LIST_LIMIT = 6
    const val DEFAULT_TREE_LIMIT = 6
}
