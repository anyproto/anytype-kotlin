package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_ID
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeIds.AUDIO
import com.anytypeio.anytype.core_models.ObjectTypeIds.DASHBOARD
import com.anytypeio.anytype.core_models.ObjectTypeIds.DATE
import com.anytypeio.anytype.core_models.ObjectTypeIds.FILE
import com.anytypeio.anytype.core_models.ObjectTypeIds.IMAGE
import com.anytypeio.anytype.core_models.ObjectTypeIds.OBJECT_TYPE
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION
import com.anytypeio.anytype.core_models.ObjectTypeIds.RELATION_OPTION
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
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
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
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
    )

    val sortsSearchObjects = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC
        )
    )
    //endregion

    //region LINK TO
    fun getFilterLinkTo(ignore: Id?, workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
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
            relationKey = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortLinkTo = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )
    //endregion

    //region MOVE TO
    fun filterMoveTo(ctx: Id, types: List<String>, workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_READ_ONLY,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.IN,
            value = types
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.ID,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(ctx)
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortMoveTo = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )
    //endregion

    //region ADD OBJECT TO RELATION VALUE
    fun filterAddObjectToRelation(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
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
    fun filterAddObjectToFilter(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                OBJECT_TYPE,
                RELATION,
                TEMPLATE,
                RELATION_OPTION,
                DASHBOARD,
                DATE
            )
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortAddObjectToFilter = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region TAB RECENT
    fun filterTabRecent(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.LAST_OPENED_DATE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = null
        ),
        DVFilter(
            relationKey = Relations.TYPE,
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
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortTabRecent = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )

    const val limitTabRecent = 50

    //endregion

    //region TAB SETS
    fun filterTabSets(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = ObjectTypeIds.SET
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val sortTabSets = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )
    //endregion

    //region TAB ARCHIVE
    fun filterTabArchive(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = true
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
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

    //region TAB SHARED

    val filterTabShared = listOf(
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeIds.WORKSPACE
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.NOT_EMPTY,
            value = null
        ),
        DVFilter(
            relationKey = Relations.IS_HIGHLIGHTED,
            condition = DVFilterCondition.EQUAL,
            value = true
        )
    )

    val sortTabShared = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )

    val defaultKeys = listOf(
        Relations.ID,
        Relations.NAME,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.TYPE,
        Relations.LAYOUT,
        Relations.IS_ARCHIVED,
        Relations.IS_DELETED,
        Relations.SNIPPET,
        Relations.DONE
    )

    val defaultOptionKeys = listOf(
        Relations.NAME,
        Relations.RELATION_OPTION_COLOR
    )

    val defaultDataViewKeys = defaultKeys + defaultOptionKeys

    //endregion

    //region OBJECT TYPES
    fun filterObjectTypeLibrary(workspaceId: String) = listOf(
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = OBJECT_TYPE
        ),
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

    val filterObjectTypeMarketplace = listOf(
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = MarketplaceObjectTypeIds.OBJECT_TYPE
        ),
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.WORKSPACE_ID,
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
        Relations.SOURCE_OBJECT
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
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = RELATION
        ),
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        )
    )

    fun filterMarketplaceRelations() : List<DVFilter> = listOf(
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = MarketplaceObjectTypeIds.RELATION
        ),
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_DELETED,
            condition = DVFilterCondition.EQUAL,
            value = false
        )
    )
}