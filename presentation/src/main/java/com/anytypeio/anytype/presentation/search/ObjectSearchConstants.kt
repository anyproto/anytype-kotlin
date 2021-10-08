package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ObjectType.Companion.AUDIO_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.FILE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.IMAGE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.OBJECT_TYPE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.PROFILE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.RELATION_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.TEMPLATE_URL
import com.anytypeio.anytype.core_models.ObjectType.Companion.VIDEO_URL

/**
 * This class contains all filters and sorts for different use cases using Rpc.Object.Search command
 */
object ObjectSearchConstants {

    //region SEARCH OBJECTS
    val filterSearchObjects = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                PROFILE_URL,
                OBJECT_TYPE_URL,
                RELATION_URL,
                TEMPLATE_URL,
                IMAGE_URL,
                FILE_URL,
                VIDEO_URL,
                AUDIO_URL
            )
        )
    )

    val sortsSearchObjects = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )
    //endregion

    //region LINK TO
    val filterLinkTo = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                PROFILE_URL,
                OBJECT_TYPE_URL,
                RELATION_URL,
                TEMPLATE_URL,
                IMAGE_URL,
                FILE_URL,
                VIDEO_URL,
                AUDIO_URL
            )
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
    fun filterMoveTo(types: List<String>) = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relationKey = Relations.IS_READ_ONLY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.IN,
            value = types
        )
    )

    val sortMoveTo = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region ADD OBJECT TO RELATION VALUE
    val filterAddObjectToRelation = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
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
    val filterAddObjectToFilter = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    val sortAddObjectToFilter = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion

    //region TAB HISTORY
    val filterTabHistory = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.NOT_IN,
            value = listOf(
                PROFILE_URL,
                OBJECT_TYPE_URL,
                RELATION_URL,
                TEMPLATE_URL,
                IMAGE_URL,
                FILE_URL,
                VIDEO_URL,
                AUDIO_URL
            )
        )
    )

    val sortTabHistory = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC
        )
    )

    val limitTabHistory = 30
    //endregion

    //region TAB SETS
    val filterTabSets = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = false
        ),
        DVFilter(
            relationKey = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.SET_URL
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
    val filterTabArchive = listOf(
        DVFilter(
            relationKey = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.EQUAL,
            value = true
        )
    )

    val sortTabArchive = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC
        )
    )
    //endregion
}