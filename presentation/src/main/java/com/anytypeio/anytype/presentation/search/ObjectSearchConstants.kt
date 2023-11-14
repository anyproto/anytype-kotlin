package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.Condition
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.FileSyncStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_SPACE_ID
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.objects.SupportedLayouts

/**
 * This class contains all filters and sorts for different use cases using Rpc.Object.Search command
 */
object ObjectSearchConstants {

    //region SEARCH OBJECTS
    fun filterSearchObjects(space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = listOf(
                ObjectType.Layout.BASIC.code.toDouble(),
                ObjectType.Layout.PROFILE.code.toDouble(),
                ObjectType.Layout.SET.code.toDouble(),
                ObjectType.Layout.COLLECTION.code.toDouble(),
                ObjectType.Layout.TODO.code.toDouble(),
                ObjectType.Layout.NOTE.code.toDouble(),
                ObjectType.Layout.BOOKMARK.code.toDouble()
            )
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortsSearchObjects = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )
    //endregion

    //region LINK TO
    fun getFilterLinkTo(ignore: Id?, space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = SupportedLayouts.layouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortLinkTo = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )
    //endregion

    //region MOVE TO
    fun filterMoveTo(ctx: Id, types: List<String>, space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.IS_READ_ONLY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.IN,
            value = types
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(ctx)
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortMoveTo = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )
    //endregion

    //region ADD OBJECT TO RELATION VALUE
    fun filterAddObjectToRelation(space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortAddObjectToRelation = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region ADD OBJECT TO FILTER
    fun filterAddObjectToFilter(
        space: Id,
        limitObjectTypes: List<Key>
    ) = buildList {
        add(
            DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            )
        )
        add(
            DVFilter(
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.EQUAL,
                value = space
            )
        )
        // TODO check these filters
        if (limitObjectTypes.isEmpty()) {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.IN,
                    value = SupportedLayouts.layouts.map { it.code.toDouble() }
                )
            )
        } else {
            add(
                DVFilter(
                    relation = Relations.TYPE_UNIQUE_KEY,
                    condition = DVFilterCondition.IN,
                    value = limitObjectTypes
                )
            )
        }
    }

    val sortAddObjectToFilter = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region TAB FAVORITES
    fun filterTabFavorites(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL ,
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
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = SupportedLayouts.layouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        ),
        DVFilter(
            relation = Relations.IS_FAVORITE,
            condition = DVFilterCondition.EQUAL,
            value = true
        )
    )

    const val limitTabFavorites = 100

    //endregion

    //region TAB RECENT
    fun filterTabRecent(
        space: Id,
        spaceCreationDateInSeconds: Long? = null
    ) = buildList {
        add(
            DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            )
        )
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = SupportedLayouts.layouts.map { it.code.toDouble() }
            )
        )
        if (spaceCreationDateInSeconds != null) {
            add(
                DVFilter(
                    relation = Relations.LAST_MODIFIED_DATE,
                    condition = DVFilterCondition.GREATER_OR_EQUAL,
                    value = (spaceCreationDateInSeconds + 3).toDouble()
                )
            )
        } else {
            add(
                DVFilter(
                    relation = Relations.LAST_MODIFIED_DATE,
                    condition = DVFilterCondition.NOT_EQUAL,
                    value = null
                )
            )
        }
        add(
            DVFilter(
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.EQUAL,
                value = space
            )
        )
    }

    val sortTabRecent = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )

    fun filterTabRecentLocal(space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = SupportedLayouts.layouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.LAST_OPENED_DATE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = null
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortTabRecentLocal = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )

    const val limitTabRecent = 50

    //endregion

    //region TAB SETS
    fun filterTabSets(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.SET.code.toDouble()
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortTabSets = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )
    //endregion

    //region TAB ARCHIVE
    fun filterTabArchive(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortTabArchive = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region BACK LINK OR ADD TO OBJECT
    fun filtersBackLinkOrAddToObject(ignore: Id?, space: Id) = listOf(
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
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = SupportedLayouts.editorLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val sortBackLinkOrAddToObject = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )
    //endregion

    val defaultKeys = listOf(
        Relations.ID,
        Relations.SPACE_ID,
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
        Relations.DONE
    )

    val defaultOptionKeys = listOf(
        Relations.NAME,
        Relations.RELATION_OPTION_COLOR
    )

    val objectCoverKeys = listOf(
        Relations.COVER_TYPE,
        Relations.COVER_ID,
        Relations.PAGE_COVER
    )

    val defaultDataViewKeys = listOf(
        Relations.ID,
        Relations.NAME,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.TYPE,
        Relations.LAYOUT,
        Relations.IS_ARCHIVED,
        Relations.IS_DELETED,
        Relations.IS_HIDDEN,
        Relations.DESCRIPTION,
        Relations.SNIPPET,
        Relations.DONE,
        Relations.RELATION_OPTION_COLOR,
        Relations.COVER_TYPE,
        Relations.COVER_ID,
        Relations.PAGE_COVER,
        Relations.FILE_EXT,
        Relations.FILE_MIME_TYPE
    )

    //endregion

    //region OBJECT TYPES

    fun filterObjectTypeLibrary(space: Id) = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    fun defaultDataViewFilters() = listOf(
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = Condition.NOT_EQUAL,
            value = true,
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = Condition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = Condition.NOT_EQUAL,
            value = true
        )
    )

    fun defaultDataViewFilters(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = Condition.NOT_EQUAL,
            value = true,
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = Condition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = Condition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        )
    )

    val filterObjectTypeMarketplace = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = MARKETPLACE_SPACE_ID
        )
    )

    val defaultKeysObjectType = listOf(
        Relations.ID,
        Relations.UNIQUE_KEY,
        Relations.NAME,
        Relations.DESCRIPTION,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.TYPE,
        Relations.LAYOUT,
        Relations.IS_ARCHIVED,
        Relations.IS_DELETED,
        Relations.SMARTBLOCKTYPES,
        Relations.SOURCE_OBJECT,
        Relations.RECOMMENDED_LAYOUT,
        Relations.DEFAULT_TEMPLATE_ID
    )

    //endregion

    fun defaultObjectSearchSorts() : List<DVSort> = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )

    fun filterMyRelations() : List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.RELATION.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    fun filterMarketplaceRelations() : List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.RELATION.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = MARKETPLACE_SPACE_ID
        )
    )

    fun filterTypes() : List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
        )
    )

    fun collectionFilters(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL ,
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
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.COLLECTION.code.toDouble()
        )
    )

    val collectionsSorts = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )

    fun filesFilters(space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = listOf(
                ObjectType.Layout.IMAGE.code.toDouble(),
                ObjectType.Layout.FILE.code.toDouble(),
                ObjectType.Layout.VIDEO.code.toDouble(),
                ObjectType.Layout.AUDIO.code.toDouble()
            )
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        ),
        DVFilter(
            relation = Relations.FILE_SYNC_STATUS,
            condition = DVFilterCondition.EQUAL,
            value = FileSyncStatus.SYNCED.value.toDouble()
        )
    )

    fun setsByObjectTypeFilters(types: List<Id>, space: Id) = listOf(
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        // TODO fix
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = SET
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = space
        ),
        DVFilter(
            relation = Relations.SET_OF,
            condition = DVFilterCondition.IN,
            value = types
        )
    )
}