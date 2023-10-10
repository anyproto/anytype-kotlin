package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.Condition
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.FileSyncStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_ID
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.AUDIO
import com.anytypeio.anytype.core_models.ObjectTypeIds.BOOKMARK
import com.anytypeio.anytype.core_models.ObjectTypeIds.DASHBOARD
import com.anytypeio.anytype.core_models.ObjectTypeIds.DATE
import com.anytypeio.anytype.core_models.ObjectTypeIds.FILE
import com.anytypeio.anytype.core_models.ObjectTypeIds.IMAGE
import com.anytypeio.anytype.core_models.ObjectTypeIds.OBJECT_TYPE
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION_OPTION
import com.anytypeio.anytype.core_models.ObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectTypeIds.SPACE
import com.anytypeio.anytype.core_models.ObjectTypeIds.TEMPLATE
import com.anytypeio.anytype.core_models.ObjectTypeIds.VIDEO
import com.anytypeio.anytype.core_models.Relations

/**
 * This class contains all filters and sorts for different use cases using Rpc.Object.Search command
 */
object ObjectSearchConstants {

    //region SEARCH OBJECTS
    fun filterSearchObjects(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                DATE,
                RELATION_OPTION
            )
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun getFilterLinkTo(ignore: Id?, workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                DATE,
                RELATION_OPTION
            )
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filterMoveTo(ctx: Id, types: List<String>, workspaceId: String) = listOf(
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filterAddObjectToRelation(workspaceId: String) = listOf(
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
        workspaceId: String,
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
        if (limitObjectTypes.isNotEmpty()) {
            add(
                DVFilter(
                    relation = Relations.TYPE,
                    condition = DVFilterCondition.IN,
                    value = limitObjectTypes
                )
            )
        } else {
            add(
                DVFilter(
                    relation = Relations.TYPE,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        OBJECT_TYPE,
                        RELATION,
                        TEMPLATE,
                        RELATION_OPTION,
                        DASHBOARD,
                        DATE
                    )
                )
            )
        }
        add(
            DVFilter(
                relation = Relations.WORKSPACE_ID,
                condition = DVFilterCondition.EQUAL,
                value = workspaceId
            )
        )
    }

    val sortAddObjectToFilter = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region TAB FAVORITES
    fun filterTabFavorites(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                RELATION_OPTION,
                DASHBOARD,
                DATE
            )
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filterTabRecent(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                RELATION_OPTION,
                DASHBOARD,
                DATE
            )
        ),
        DVFilter(
            relation = Relations.LAST_MODIFIED_DATE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = null
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortTabRecent = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )

    fun filterTabRecentLocal(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                RELATION_OPTION,
                DASHBOARD,
                DATE
            )
        ),
        DVFilter(
            relation = Relations.LAST_OPENED_DATE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = null
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filterTabSets(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = ObjectTypeIds.SET
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filterTabArchive(workspaceId: String) = listOf(
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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
    fun filtersBackLinkOrAddToObject(ignore: Id?, workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                DATE,
                RELATION_OPTION,
                SPACE,
                SET,
                BOOKMARK
            )
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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

    fun filterObjectTypeLibrary(workspaceId: String) = listOf(
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = OBJECT_TYPE
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
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

    fun defaultDataViewFilters(workspaceId: String) = listOf(
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val filterObjectTypeMarketplace = listOf(
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = MarketplaceObjectTypeIds.OBJECT_TYPE
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
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = MARKETPLACE_ID
        )
    )

    val defaultKeysObjectType = listOf(
        Relations.ID,
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
        Relations.RECOMMENDED_LAYOUT
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = RELATION
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = MarketplaceObjectTypeIds.RELATION
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
        )
    )

    fun collectionFilters(workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                IMAGE,
                FILE,
                VIDEO,
                AUDIO,
                DASHBOARD,
                RELATION_OPTION,
                DASHBOARD,
                DATE
            )
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        ),
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = ObjectTypeIds.COLLECTION
        )
    )

    val collectionsSorts = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true
        )
    )

    fun filesFilters(workspaceId: String) = listOf(
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.IN,
            value = listOf(
                IMAGE,
                FILE,
                VIDEO,
                AUDIO
            )
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        ),
        DVFilter(
            relation = Relations.FILE_SYNC_STATUS,
            condition = DVFilterCondition.EQUAL,
            value = FileSyncStatus.SYNCED.value.toDouble()
        )
    )

    fun setsByObjectTypeFilters(types: List<Id>, workspaceId: String) = listOf(
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
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = SET
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        ),
        DVFilter(
            relation = Relations.SET_OF,
            condition = DVFilterCondition.IN,
            value = types
        )
    )
}