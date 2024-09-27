package com.anytypeio.anytype.feature_allcontent.models

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations

val allContentTabLayouts = mapOf(
    AllContentTab.OBJECTS to listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.PARTICIPANT,
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE
    ),
    AllContentTab.FILES to listOf(
        ObjectType.Layout.FILE,
        ObjectType.Layout.PDF
    ),
    AllContentTab.MEDIA to listOf(
        ObjectType.Layout.IMAGE,
        ObjectType.Layout.VIDEO,
        ObjectType.Layout.AUDIO
    ),
    AllContentTab.BOOKMARKS to listOf(
        ObjectType.Layout.BOOKMARK
    )
)

fun AllContentTab.filtersForSubscribe(spaces: List<Id>): List<DVFilter> {
    val tab = this
    return when (this) {
        AllContentTab.OBJECTS, AllContentTab.FILES, AllContentTab.MEDIA, AllContentTab.BOOKMARKS -> {
            buildList {
                addAll(getHiddenArchivedDeletedFilters())
                add(buildLayoutFilter(allContentTabLayouts.getValue(tab)))
                add(buildSpaceIdFilter(spaces))
                if (tab == AllContentTab.OBJECTS) {
                    add(buildObjectTypeFilter())
                }
            }
        }

        AllContentTab.TYPES -> TODO()
        AllContentTab.RELATIONS -> TODO()
    }
}

private fun buildLayoutFilter(layouts: List<ObjectType.Layout>): DVFilter {
    return DVFilter(
        relation = Relations.LAYOUT,
        condition = DVFilterCondition.IN,
        value = layouts.map { it.code.toDouble() }
    )
}

private fun buildObjectTypeFilter(): DVFilter {
    return DVFilter(
        relation = Relations.TYPE_UNIQUE_KEY,
        condition = DVFilterCondition.NOT_EQUAL,
        value = ObjectTypeUniqueKeys.TEMPLATE
    )
}

private fun buildSpaceIdFilter(spaces: List<Id>): DVFilter {
    return DVFilter(
        relation = Relations.SPACE_ID,
        condition = DVFilterCondition.IN,
        value = spaces
    )
}

private fun getHiddenArchivedDeletedFilters(): List<DVFilter> {
    return listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )
}

fun AllContentSort.toDVSort(): DVSort {
    return when (this) {
        is AllContentSort.ByDateCreated -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.DATE,
            includeTime = true,
        )

        is AllContentSort.ByDateUpdated -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.DATE,
            includeTime = true,
        )

        is AllContentSort.ByName -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.LONG_TEXT,
            includeTime = false
        )
    }
}