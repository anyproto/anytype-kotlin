package com.anytypeio.anytype.feature_allcontent.models

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams

val allContentTabLayouts = mapOf(
    AllContentTab.PAGES to listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE
    ),
    AllContentTab.LISTS to listOf(
        ObjectType.Layout.SET,
        ObjectType.Layout.COLLECTION
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

// Function to create subscription params
fun createSubscriptionParams(
    spaceId: Id,
    activeMode: AllContentMode,
    activeTab: AllContentTab,
    activeSort: AllContentSort,
    limitedObjectIds: List<String>,
    limit: Int,
    subscriptionId: String
): StoreSearchParams {
    val (filters, sorts) = activeTab.filtersForSubscribe(
        spaces = listOf(spaceId),
        activeSort = activeSort,
        limitedObjectIds = limitedObjectIds,
        activeMode = activeMode
    )
    return StoreSearchParams(
        filters = filters,
        sorts = sorts,
        keys = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.TARGET_SPACE_ID,
            Relations.UNIQUE_KEY,
            Relations.NAME,
            Relations.ICON_IMAGE,
            Relations.ICON_EMOJI,
            Relations.ICON_OPTION,
            Relations.TYPE,
            Relations.LAYOUT,
            Relations.IS_ARCHIVED,
            Relations.IS_DELETED,
            Relations.IS_HIDDEN,
            Relations.SNIPPET,
            Relations.DONE,
            Relations.IDENTITY_PROFILE_LINK,
            Relations.RESTRICTIONS,
            Relations.SIZE_IN_BYTES,
            Relations.FILE_MIME_TYPE,
            Relations.FILE_EXT,
            Relations.LAST_OPENED_DATE,
            Relations.LAST_MODIFIED_DATE,
            Relations.CREATED_DATE,
            Relations.LINKS,
            Relations.BACKLINKS
        ),
        limit = limit,
        subscription = subscriptionId
    )
}

fun AllContentTab.filtersForSubscribe(
    spaces: List<Id>,
    activeSort: AllContentSort,
    limitedObjectIds: List<Id>,
    activeMode: AllContentMode
): Pair<List<DVFilter>, List<DVSort>> {
    val tab = this
    when (this) {
        AllContentTab.PAGES,
        AllContentTab.LISTS,
        AllContentTab.FILES,
        AllContentTab.MEDIA,
        AllContentTab.BOOKMARKS -> {
            val filters = buildList {
                addAll(buildDeletedFilter())
                add(buildLayoutFilter(layouts = allContentTabLayouts.getValue(tab)))
                add(buildSpaceIdFilter(spaces))
                if (tab == AllContentTab.PAGES) {
                    add(buildTemplateFilter())
                }
                if (limitedObjectIds.isNotEmpty()) {
                    add(buildLimitedObjectIdsFilter(limitedObjectIds = limitedObjectIds))
                }
                if (activeMode == AllContentMode.Unlinked) {
                    addAll(buildUnlinkedObjectFilter())
                }
            }
            val sorts = listOf(activeSort.toDVSort())
            return filters to sorts
        }

        AllContentTab.TYPES -> TODO()
    }
}

fun AllContentTab.filtersForSearch(
    spaces: List<Id>
): List<DVFilter> {
    val tab = this
    when (this) {
        AllContentTab.PAGES,
        AllContentTab.LISTS,
        AllContentTab.FILES,
        AllContentTab.MEDIA,
        AllContentTab.BOOKMARKS -> {
            val filters = buildList {
                addAll(buildDeletedFilter())
                add(buildLayoutFilter(layouts = allContentTabLayouts.getValue(tab)))
                add(buildSpaceIdFilter(spaces))
                if (tab == AllContentTab.PAGES) {
                    add(buildTemplateFilter())
                }
            }
            return filters
        }

        AllContentTab.TYPES -> TODO()
    }
}

private fun buildLayoutFilter(layouts: List<ObjectType.Layout>): DVFilter = DVFilter(
    relation = Relations.LAYOUT,
    condition = DVFilterCondition.IN,
    value = layouts.map { it.code.toDouble() }
)

private fun buildTemplateFilter(): DVFilter = DVFilter(
    relation = Relations.TYPE_UNIQUE_KEY,
    condition = DVFilterCondition.NOT_EQUAL,
    value = ObjectTypeUniqueKeys.TEMPLATE
)

private fun buildSpaceIdFilter(spaces: List<Id>): DVFilter = DVFilter(
    relation = Relations.SPACE_ID,
    condition = DVFilterCondition.IN,
    value = spaces
)

private fun buildUnlinkedObjectFilter(): List<DVFilter> = listOf(
    DVFilter(
        relation = Relations.LINKS,
        condition = DVFilterCondition.EMPTY
    ),
    DVFilter(
        relation = Relations.BACKLINKS,
        condition = DVFilterCondition.EMPTY
    )
)

private fun buildLimitedObjectIdsFilter(limitedObjectIds: List<Id>): DVFilter = DVFilter(
    relation = Relations.ID,
    condition = DVFilterCondition.IN,
    value = limitedObjectIds
)

private fun buildDeletedFilter(): List<DVFilter> {
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