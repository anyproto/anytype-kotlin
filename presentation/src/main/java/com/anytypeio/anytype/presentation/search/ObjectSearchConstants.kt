package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.Condition
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Marketplace.MARKETPLACE_SPACE_ID
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.presentation.objects.SupportedLayouts

/**
 * This class contains all filters and sorts for different use cases using Rpc.Object.Search command
 */
object ObjectSearchConstants {

    //region SEARCH OBJECTS
    fun filterSearchObjects(spaces: List<Id>) = listOf(
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
            value = SupportedLayouts.globalSearchLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortsSearchObjects = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )

    //endregion

    //region LINK TO
    fun getFilterLinkTo(
        ignore: Id?, spaces: List<Id>
    ) = listOf(
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
            value = (SupportedLayouts.createObjectLayouts + SupportedLayouts.fileLayouts).map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortLinkTo = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
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
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )
    //endregion

    //region ADD OBJECT TO RELATION VALUE
    fun filterAddObjectToRelation(spaces: List<Id>, targetTypes: List<Id>) = buildList {
        addAll(
            listOf(
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
                    condition = DVFilterCondition.IN,
                    value = spaces
                )
            )
        )
        if (targetTypes.isEmpty()) {
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
                    relation = Relations.TYPE,
                    condition = DVFilterCondition.IN,
                    value = targetTypes
                )
            )
        }
    }

    val sortAddObjectToRelation = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC,
            relationFormat = RelationFormat.LONG_TEXT
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
                    relation = Relations.TYPE,
                    condition = DVFilterCondition.IN,
                    value = limitObjectTypes
                )
            )
        }
    }

    fun filterAddObjectToFilterByLayout(space: Id, layouts: List<ObjectType.Layout>) = buildList {
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
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.EQUAL,
                value = space
            )
        )
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = layouts.map { it.code.toDouble() }
            )
        )
    }

    val sortAddObjectToFilter = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC,
            relationFormat = RelationFormat.LONG_TEXT
        )
    )
    //endregion

    //region TAB FAVORITES
    fun filterTabFavorites(spaces: List<Id>) = listOf(
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
            value = SupportedLayouts.createObjectLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
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
        spaces: List<Id>,
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
                value = SupportedLayouts.createObjectLayouts.map { it.code.toDouble() }
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
                condition = DVFilterCondition.IN,
                value = spaces
            )
        )
    }

    val sortTabRecent = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )

    fun filterTabRecentLocal(
        spaces: List<Id>
    ) = listOf(
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
            value = SupportedLayouts.createObjectLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.LAST_OPENED_DATE,
            condition = DVFilterCondition.NOT_EQUAL,
            value = null
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortTabRecentLocal = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )

    const val limitTabRecent = 50

    //endregion

    //region TAB SETS
    fun filterTabSets(spaces: List<Id>) = listOf(
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
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortTabSets = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )
    //endregion

    //region TAB ARCHIVE
    fun filterTabArchive(spaces: List<Id>) = listOf(
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
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortTabArchive = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC,
            relationFormat = RelationFormat.LONG_TEXT
        )
    )
    //endregion

    //region BACK LINK OR ADD TO OBJECT
    fun filtersBackLinkOrAddToObject(ignore: Id?, spaces: List<Id>) = listOf(
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
            value = SupportedLayouts.addAsLinkToLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ignore
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    val sortBackLinkOrAddToObject = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )
    //endregion

    val defaultKeys = listOf(
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
        Relations.BACKLINKS,
        Relations.LAST_USED_DATE
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
        Relations.SPACE_ID,
        Relations.TARGET_SPACE_ID,
        Relations.IDENTITY_PROFILE_LINK,
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
        Relations.FILE_MIME_TYPE,
        Relations.RESTRICTIONS
    )

    val defaultRelationKeys = listOf(
        Relations.ID,
        Relations.SPACE_ID,
        Relations.TARGET_SPACE_ID,
        Relations.UNIQUE_KEY,
        Relations.NAME,
        Relations.DESCRIPTION,
        Relations.ICON_EMOJI,
        Relations.TYPE,
        Relations.LAYOUT,
        Relations.IS_ARCHIVED,
        Relations.IS_DELETED,
        Relations.IS_HIDDEN,
        Relations.SNIPPET,
        Relations.RESTRICTIONS,
        Relations.SOURCE_OBJECT,
        Relations.RELATION_FORMAT,
        Relations.RELATION_KEY,
        Relations.RELATION_OPTION_COLOR,
        Relations.RELATION_DEFAULT_VALUE,
        Relations.RELATION_FORMAT_OBJECT_TYPES,
        Relations.RELATION_READ_ONLY_VALUE,
        Relations.LAST_USED_DATE
    )

    val defaultFilesKeys = defaultKeys + listOf(
        Relations.DESCRIPTION,
        Relations.SIZE_IN_BYTES,
        Relations.FILE_MIME_TYPE,
        Relations.FILE_EXT,
        Relations.FILE_SYNC_STATUS
    )

    //endregion

    //region OBJECT TYPES

    fun filterTypes(
        spaces: List<Id>,
        recommendedLayouts: List<ObjectType.Layout> = emptyList(),
        excludedTypeKeys: List<TypeKey> = emptyList(),
        excludeParticipant: Boolean = true
    ): List<DVFilter> {
        return buildList {
            addAll(
                listOf(
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
                    ),
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.IN,
                        value = spaces
                    ),
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EMPTY
                    )
                )
            )
            if (excludedTypeKeys.isNotEmpty()) {
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_IN,
                        value = excludedTypeKeys.map { it.key }
                    )
                )
            }
            addRecommendedLayoutsFilter(recommendedLayouts, excludeParticipant)
        }
    }

    private fun MutableList<DVFilter>.addRecommendedLayoutsFilter(
        recommendedLayouts: List<ObjectType.Layout>,
        excludeParticipant: Boolean
    ) {
        when {
            recommendedLayouts.isNotEmpty() -> add(
                DVFilter(
                    relation = Relations.RECOMMENDED_LAYOUT,
                    condition = DVFilterCondition.IN,
                    value = getLayoutCodes(recommendedLayouts, excludeParticipant)
                )
            )
            excludeParticipant -> add(
                DVFilter(
                    relation = Relations.RECOMMENDED_LAYOUT,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectType.Layout.PARTICIPANT.code.toDouble())
                )
            )
        }
    }

    private fun getLayoutCodes(layouts: List<ObjectType.Layout>, excludeParticipant: Boolean): List<Double> {
        return if (excludeParticipant) {
            layouts.filterNot { it == ObjectType.Layout.PARTICIPANT }.map { it.code.toDouble() }
        } else {
            layouts.map { it.code.toDouble() }
        }
    }

    fun filterParticipants(spaces: List<Id>) : List<DVFilter> = buildList {
        add(
            DVFilter(
                relation = Relations.IS_ARCHIVED,
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
                relation = Relations.IS_HIDDEN,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.EQUAL,
                value = ObjectType.Layout.PARTICIPANT.code.toDouble()
            )
        )
        add(
            DVFilter(
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.IN,
                value = spaces
            )
        )
    }

    fun filterNewMember(member: Id) : List<DVFilter> = buildList {
        add(
            DVFilter(
                relation = Relations.IS_ARCHIVED,
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
                relation = Relations.ID,
                condition = DVFilterCondition.IN,
                value = listOf(member)
            )
        )
    }

    fun defaultDataViewFilters(spaces: List<Id>) = buildList {
        add(
            DVFilter(
                relation = Relations.SPACE_ID,
                condition = DVFilterCondition.IN,
                value = spaces
            )
        )
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.NOT_IN,
                value = SupportedLayouts.systemLayouts.map { layout ->
                    layout.code.toDouble()
                }
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_HIDDEN,
                condition = Condition.NOT_EQUAL,
                value = true,
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_DELETED,
                condition = Condition.NOT_EQUAL,
                value = true
            )
        )
        add(
            DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = Condition.NOT_EQUAL,
                value = true
            )
        )
    }

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
        Relations.DEFAULT_TEMPLATE_ID,
        Relations.SPACE_ID,
        Relations.RESTRICTIONS,
        Relations.LAST_USED_DATE
    )

    //endregion

    fun defaultObjectSearchSorts() : List<DVSort> = listOf(
        DVSort(
            relationKey = Relations.NAME,
            type = DVSortType.ASC,
            relationFormat = RelationFormat.LONG_TEXT
        )
    )

    fun defaultObjectTypeSearchSorts() : List<DVSort> = buildList {
        add(
            DVSort(
                relationKey = Relations.LAST_USED_DATE,
                type = DVSortType.DESC,
                includeTime = true,
                relationFormat = RelationFormat.DATE
            )
        )
        add(
            DVSort(
                relationKey = Relations.NAME,
                type = DVSortType.ASC,
                relationFormat = RelationFormat.LONG_TEXT
            )
        )
    }

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

    fun collectionFilters(spaces: List<Id>) = listOf(
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.COLLECTION.code.toDouble()
        ),
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
        )
    )

    val collectionsSorts = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )

    fun filesFilters(spaces: List<Id>) = listOf(
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
            relation = Relations.IS_HIDDEN,
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
                ObjectType.Layout.AUDIO.code.toDouble(),
                ObjectType.Layout.PDF.code.toDouble()
            )
        ),
        DVFilter(
            relation = Relations.SPACE_ID,
            condition = DVFilterCondition.IN,
            value = spaces
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
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = ObjectType.Layout.SET.code.toDouble()
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

    fun filterRelationOptions(relationKey: Key, spaces: List<Id>) : List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.RELATION_KEY,
            condition = DVFilterCondition.EQUAL,
            value = relationKey
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.RELATION_OPTION.code.toDouble()
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
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    fun filterObjectsByIds(ids: List<Id>, spaces: List<Id>) = listOf(
        DVFilter(
            relation = Relations.ID,
            condition = DVFilterCondition.IN,
            value = ids
        ),
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
            condition = DVFilterCondition.IN,
            value = spaces
        )
    )

    fun sortByName() : DVSort = DVSort(
        relationKey = Relations.NAME,
        type = DVSortType.ASC,
        relationFormat = RelationFormat.LONG_TEXT
    )

    val keysRelationOptions = listOf(
        Relations.ID,
        Relations.SPACE_ID,
        Relations.NAME,
        Relations.RELATION_OPTION_COLOR,
        Relations.RELATION_KEY
    )

    val spaceMemberKeys = listOf(
        Relations.ID,
        Relations.SPACE_ID,
        Relations.TARGET_SPACE_ID,
        Relations.IDENTITY,
        Relations.NAME,
        Relations.ICON_IMAGE,
        Relations.PARTICIPANT_STATUS,
        Relations.PARTICIPANT_PERMISSIONS,
        Relations.LAYOUT
    )

    val spaceViewKeys = listOf(
        Relations.ID,
        Relations.NAME,
        Relations.TARGET_SPACE_ID,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.ICON_OPTION,
        Relations.CREATED_DATE,
        Relations.SPACE_ACCOUNT_STATUS,
        Relations.SPACE_ACCESS_TYPE,
        Relations.SPACE_LOCAL_STATUS,
        Relations.SHARED_SPACES_LIMIT,
        Relations.READERS_LIMIT,
        Relations.WRITERS_LIMIT,
    )

    //region SPACE VIEW

    fun getSpaceViewSearchParams(subscription: String, targetSpaceId: Id): StoreSearchParams {
        return StoreSearchParams(
            subscription = subscription,
            keys = spaceViewKeys,
            limit = 1,
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.TARGET_SPACE_ID,
                        value = targetSpaceId,
                        condition = DVFilterCondition.EQUAL
                    )
                )
            }
        )
    }

    fun getSpaceMembersSearchParams(subscription: String, spaceId: Id): StoreSearchParams {
        return StoreSearchParams(
            subscription = subscription,
            filters = filterParticipants(
                spaces = listOf(spaceId)
            ),
            sorts = listOf(sortByName()),
            keys = spaceMemberKeys
        )
    }
    //endregion
}