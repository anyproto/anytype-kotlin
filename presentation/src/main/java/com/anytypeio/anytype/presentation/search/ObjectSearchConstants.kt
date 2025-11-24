package com.anytypeio.anytype.presentation.search

import com.anytypeio.anytype.core_models.Condition
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.globalSearchLayouts

/**
 * This class contains all filters and sorts for different use cases using Rpc.Object.Search command
 */
object ObjectSearchConstants {

    //region SEARCH OBJECTS
    fun filterSearchObjects(
        excludeTypes: Boolean = false,
        spaceUxType: SpaceUxType? = null
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
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
        // Exclude chat types in chat spaces
        if (spaceUxType == SpaceUxType.CHAT) {
            // Filter out objects whose type is chat
            add(
                DVFilter(
                    relation = Relations.TYPE_UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectTypeIds.CHAT_DERIVED, ObjectTypeIds.CHAT)
                )
            )
            // Filter out ObjectType objects (the type definitions themselves) with chat uniqueKey
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectTypeIds.CHAT_DERIVED, ObjectTypeIds.CHAT)
                )
            )
        }
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = if (excludeTypes) {
                    SupportedLayouts.getObjectSearchLayouts(spaceUxType)
                        .filter { it != ObjectType.Layout.OBJECT_TYPE }
                        .map { it.code.toDouble() }
                } else {
                    SupportedLayouts.getObjectSearchLayouts(spaceUxType)
                        .map { it.code.toDouble() }
                }
            )
        )
    }

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
        ignore: Id?,
        spaceUxType: SpaceUxType? = null
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
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
        // Exclude chat types in chat spaces
        if (spaceUxType == SpaceUxType.CHAT) {
            // Filter out objects whose type is chat
            add(
                DVFilter(
                    relation = Relations.TYPE_UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectTypeIds.CHAT_DERIVED, ObjectTypeIds.CHAT)
                )
            )
            // Filter out ObjectType objects (the type definitions themselves) with chat uniqueKey
            add(
                DVFilter(
                    relation = Relations.UNIQUE_KEY,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectTypeIds.CHAT_DERIVED, ObjectTypeIds.CHAT)
                )
            )
        }
        add(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = SupportedLayouts.getObjectSearchLayouts(spaceUxType)
                    .map { it.code.toDouble() }
            )
        )
        add(
            DVFilter(
                relation = Relations.ID,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ignore
            )
        )
    }

    val sortLinkTo = listOf(
        DVSort(
            relationKey = Relations.LAST_OPENED_DATE,
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
    fun filterAddObjectToRelation(
        space: Id, 
        targetTypes: List<Id>,
        spaceUxType: SpaceUxType? = null
    ) = buildList {
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
                    relation = Relations.IS_HIDDEN_DISCOVERY,
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
        )
        if (targetTypes.isEmpty()) {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.IN,
                    value = SupportedLayouts.getLayouts(spaceUxType).map { it.code.toDouble() }
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
    //endregion

    //region ADD OBJECT TO FILTER
    fun filterAddObjectToFilter(
        limitObjectTypes: List<Key>,
        spaceUxType: SpaceUxType? = null
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
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
        if (limitObjectTypes.isEmpty()) {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.IN,
                    value = SupportedLayouts.getLayouts(spaceUxType).map { it.code.toDouble() }
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
            add(
                DVFilter(
                    relation = Relations.RECOMMENDED_LAYOUT,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectType.Layout.CHAT_DERIVED.code.toDouble())
                )
            )
            add(
                DVFilter(
                    relation = Relations.RECOMMENDED_LAYOUT,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(ObjectType.Layout.CHAT.code.toDouble())
                )
            )
        }
    }

    fun filterAddObjectToFilterByLayout(layouts: List<ObjectType.Layout>) = buildList {
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
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
    fun filterTabFavorites() = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
            condition = DVFilterCondition.NOT_IN,
            value = SupportedLayouts.systemLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = ObjectTypeUniqueKeys.TEMPLATE
        ),
        DVFilter(
            relation = Relations.IS_FAVORITE,
            condition = DVFilterCondition.EQUAL,
            value = true
        )
    )
    //endregion

    //region TAB RECENT
    fun filterTabRecent(
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
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
                condition = DVFilterCondition.NOT_IN,
                value = SupportedLayouts.systemLayouts.map { it.code.toDouble() }
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
    }

    val sortTabRecent = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            includeTime = true,
            relationFormat = RelationFormat.DATE
        )
    )

    fun filterTabRecentLocal() = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
            condition = DVFilterCondition.NOT_IN,
            value = SupportedLayouts.systemLayouts.map { it.code.toDouble() }
        ),
        DVFilter(
            relation = Relations.LAST_OPENED_DATE,
            condition = DVFilterCondition.GREATER,
            value = 0.0
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
    fun filterTabSets() = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.SET.code.toDouble()
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
    fun filterTabArchive() = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    val sortTabArchive = listOf(
        DVSort(
            relationKey = Relations.LAST_MODIFIED_DATE,
            type = DVSortType.DESC,
            relationFormat = RelationFormat.LONG_TEXT
        )
    )
    //endregion

    //region BACK LINK OR ADD TO OBJECT
    fun filtersBackLinkOrAddToObject(ignore: Id?) = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
        Relations.PLURAL_NAME,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.ICON_NAME,
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
        Relations.COVER_TYPE,
        Relations.COVER_ID,
        Relations.PAGE_COVER,
        Relations.LINKS,
        Relations.BACKLINKS,
        Relations.LAST_USED_DATE,
        Relations.DESCRIPTION,
        Relations.TIMESTAMP,
        Relations.SOURCE
    )

    val defaultDataViewKeys = listOf(
        Relations.ID,
        Relations.SPACE_ID,
        Relations.TARGET_SPACE_ID,
        Relations.IDENTITY_PROFILE_LINK,
        Relations.NAME,
        Relations.PLURAL_NAME,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.ICON_NAME,
        Relations.ICON_OPTION,
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
        Relations.RESTRICTIONS,
        Relations.TARGET_OBJECT_TYPE,
        Relations.SOURCE,
        Relations.CREATED_DATE
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
        recommendedLayouts: List<ObjectType.Layout> = emptyList(),
        excludedTypeKeys: List<TypeKey> = emptyList(),
        excludeParticipant: Boolean = true,
        excludeTemplates: Boolean = true,
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
                        relation = Relations.IS_HIDDEN_DISCOVERY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                    ),
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EMPTY
                    )
                )
            )
            if (excludeTemplates) {
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeUniqueKeys.TEMPLATE
                    )
                )
            }
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

    fun filterParticipants(
        space: SpaceId,
        hiddenDiscovery: Boolean = true
    ) : List<DVFilter> = buildList {
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
        if (hiddenDiscovery) {
            add(
                DVFilter(
                    relation = Relations.IS_HIDDEN_DISCOVERY,
                    condition = DVFilterCondition.NOT_EQUAL,
                    value = true
                )
            )
        }
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
                condition = DVFilterCondition.EQUAL,
                value = space.id
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

    fun defaultDataViewFilters() = buildList {
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
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
        Relations.PLURAL_NAME,
        Relations.DESCRIPTION,
        Relations.ICON_IMAGE,
        Relations.ICON_EMOJI,
        Relations.ICON_NAME,
        Relations.ICON_OPTION,
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
        Relations.LAST_USED_DATE,
        Relations.SOURCE
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
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    fun collectionFilters() = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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

    fun filesFilters(space: Id, hiddenDiscovery: Boolean = true) = buildList {
        addAll(
            listOf(
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
                    condition = DVFilterCondition.EQUAL,
                    value = space
                )
            )
        )
        if (hiddenDiscovery) {
            add(
                DVFilter(
                    relation = Relations.IS_HIDDEN_DISCOVERY,
                    condition = DVFilterCondition.NOT_EQUAL,
                    value = true
                )
            )
        }
    }

    fun setsByObjectTypeFilters(types: List<Id>) = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.IN,
            value = ObjectType.Layout.SET.code.toDouble()
        ),
        DVFilter(
            relation = Relations.SET_OF,
            condition = DVFilterCondition.IN,
            value = types
        )
    )

    fun filterRelationOptions(relationKey: Key) : List<DVFilter> = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )

    fun filterObjectsByIds(ids: List<Id>, space: Id) = listOf(
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
            relation = Relations.IS_HIDDEN_DISCOVERY,
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
        Relations.IDENTITY_PROFILE_LINK,
        Relations.NAME,
        Relations.PLURAL_NAME,
        Relations.DESCRIPTION,
        Relations.ICON_IMAGE,
        Relations.PARTICIPANT_STATUS,
        Relations.PARTICIPANT_PERMISSIONS,
        Relations.LAYOUT
    )

    //region SPACE VIEW
    fun getSpaceMembersSearchParams(
        subscription: String,
        space: SpaceId,
        includeRequests: Boolean = true
    ): StoreSearchParams {
        return StoreSearchParams(
            space = space,
            subscription = subscription,
            sorts = listOf(sortByName()),
            keys = spaceMemberKeys,
            filters = filterParticipants(
                space = space,
                hiddenDiscovery = !includeRequests
            ),
        )
    }
    //endregion
}

fun buildLayoutFilter(layouts: List<ObjectType.Layout>): DVFilter = DVFilter(
    relation = Relations.LAYOUT,
    condition = DVFilterCondition.IN,
    value = layouts.map { it.code.toDouble() }
)

fun buildTemplateFilter(): DVFilter = DVFilter(
    relation = Relations.TYPE_UNIQUE_KEY,
    condition = DVFilterCondition.NOT_EQUAL,
    value = ObjectTypeUniqueKeys.TEMPLATE
)

fun buildChatsFilter(): DVFilter = DVFilter(
    relation = Relations.RECOMMENDED_LAYOUT,
    condition = DVFilterCondition.NOT_IN,
    value = listOf(
        ObjectType.Layout.CHAT_DERIVED.code.toDouble(),
        ObjectType.Layout.CHAT.code.toDouble()
    )
)

fun buildSpaceIdFilter(spaces: List<Id>): DVFilter = DVFilter(
    relation = Relations.SPACE_ID,
    condition = DVFilterCondition.IN,
    value = spaces
)

fun buildUnlinkedObjectFilter(): List<DVFilter> = listOf(
    DVFilter(
        relation = Relations.LINKS,
        condition = DVFilterCondition.EMPTY
    ),
    DVFilter(
        relation = Relations.BACKLINKS,
        condition = DVFilterCondition.EMPTY
    )
)

fun buildLimitedObjectIdsFilter(limitedObjectIds: List<Id>): DVFilter = DVFilter(
    relation = Relations.ID,
    condition = DVFilterCondition.IN,
    value = limitedObjectIds
)

fun buildDeletedFilter(): List<DVFilter> {
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
        ),
        DVFilter(
            relation = Relations.IS_HIDDEN_DISCOVERY,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        )
    )
}