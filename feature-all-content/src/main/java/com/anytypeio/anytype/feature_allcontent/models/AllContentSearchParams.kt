package com.anytypeio.anytype.feature_allcontent.models

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.objects.toDVSort
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeysObjectType
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultRelationKeys
import com.anytypeio.anytype.presentation.search.buildDeletedFilter
import com.anytypeio.anytype.presentation.search.buildLayoutFilter
import com.anytypeio.anytype.presentation.search.buildLimitedObjectIdsFilter
import com.anytypeio.anytype.presentation.search.buildSpaceIdFilter
import com.anytypeio.anytype.presentation.search.buildTemplateFilter
import com.anytypeio.anytype.presentation.search.buildUnlinkedObjectFilter

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
    activeMode: UiTitleState,
    activeTab: AllContentTab,
    activeSort: ObjectsListSort,
    limitedObjectIds: List<String>,
    limit: Int,
    subscriptionId: String
): StoreSearchParams {
    val keys = when (activeTab) {
        AllContentTab.TYPES -> defaultKeysObjectType
        AllContentTab.RELATIONS -> defaultRelationKeys
        else -> defaultKeys
    }
    val (filters, sorts) = activeTab.filtersForSubscribe(
        spaces = listOf(spaceId),
        activeSort = activeSort,
        limitedObjectIds = limitedObjectIds,
        activeMode = activeMode
    )
    return StoreSearchParams(
        space = SpaceId(spaceId),
        filters = filters,
        sorts = sorts,
        keys = keys,
        limit = limit,
        subscription = subscriptionId
    )
}

fun AllContentTab.filtersForSubscribe(
    spaces: List<Id>,
    activeSort: ObjectsListSort,
    limitedObjectIds: List<Id>,
    activeMode: UiTitleState
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
                if (activeMode == UiTitleState.OnlyUnlinked) {
                    addAll(buildUnlinkedObjectFilter())
                }
            }
            val sorts = listOf(activeSort.toDVSort())
            return filters to sorts
        }
        AllContentTab.TYPES -> {
            val filters = buildList {
                addAll(buildDeletedFilter())
                add(buildSpaceIdFilter(spaces))
                if (limitedObjectIds.isNotEmpty()) {
                    add(buildLimitedObjectIdsFilter(limitedObjectIds = limitedObjectIds))
                }
                add(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectType.Layout.PARTICIPANT.code.toDouble()
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeIds.CHAT_DERIVED
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeIds.CHAT
                    )
                )
            }
            val sorts = listOf(activeSort.toDVSort())
            return filters to sorts
        }
        AllContentTab.RELATIONS -> {
            val filters = buildList {
                addAll(buildDeletedFilter())
                add(buildSpaceIdFilter(spaces))
                if (limitedObjectIds.isNotEmpty()) {
                    add(buildLimitedObjectIdsFilter(limitedObjectIds = limitedObjectIds))
                }
                add(
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.RELATION.code.toDouble()
                    ),
                )
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeIds.CHAT_DERIVED
                    )
                )
            }
            val sorts = listOf(activeSort.toDVSort())
            return filters to sorts
        }
    }
}

fun AllContentTab.filtersForSearch(
    spaces: List<Id>
): List<DVFilter> {
    val tab = this
    val filters = buildList {
        addAll(buildDeletedFilter())
        add(buildSpaceIdFilter(spaces))
        if (tab == AllContentTab.PAGES) {
            add(buildTemplateFilter())
        }
    }
    return filters
}