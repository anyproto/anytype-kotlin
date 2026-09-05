package com.anytypeio.anytype.feature_search.presentation

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts

/**
 * Filter/sort construction for the unified search — a direct port of the
 * desktop client's loaders.
 */
object SearchFilters {

    /** No constant in [Relations] for this one. */
    internal const val ADDED_DATE = "addedDate"

    /**
     * Base filters for object loaders. Chat containers are deliberately kept
     * in the allow-list: excluding chat layouts vault-wide would hide every
     * chat from global search and empty the Chats bucket (a shipped desktop
     * regression — do not re-introduce a blanket chat exclusion here).
     */
    fun base(): List<DVFilter> {
        val layouts = buildList {
            addAll(SupportedLayouts.globalSearchLayouts)
            add(ObjectType.Layout.CHAT)
        }
        return listOf(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = layouts.map { it.code.toDouble() }
            ),
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
                relation = Relations.IS_HIDDEN_DISCOVERY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ),
            DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            )
        )
    }

    fun kindBucket(bucket: KindBucket): List<DVFilter> {
        val layouts = bucket.layouts()
        if (layouts.isEmpty()) return emptyList()
        return listOf(
            DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.IN,
                value = layouts.map { it.code.toDouble() }
            )
        )
    }

    /** uniqueKey survives scope changes — the same type matches in every space. */
    fun typeToken(token: SearchToken.TypeFilter): List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.TYPE_UNIQUE_KEY,
            condition = DVFilterCondition.EQUAL,
            value = token.uniqueKey
        )
    )

    /**
     * Creator filter: the raw identity (legacy records) plus every per-space
     * participant id of that identity, resolved from the vault-wide
     * participants store — never queried per space.
     *
     * Chat containers all carry the space creator, so they are noise for any
     * "who" filter — excluded, EXCEPT when the what token is the Chats bucket
     * itself (the two filters would contradict and empty every result).
     */
    fun creatorToken(
        identity: Id,
        participantIds: List<Id>,
        whatIsChatsBucket: Boolean
    ): List<DVFilter> = buildList {
        add(
            DVFilter(
                relation = Relations.CREATOR,
                condition = DVFilterCondition.IN,
                value = (participantIds + identity).distinct()
            )
        )
        if (!whatIsChatsBucket) {
            add(
                DVFilter(
                    relation = Relations.LAYOUT,
                    condition = DVFilterCondition.NOT_IN,
                    value = listOf(
                        ObjectType.Layout.CHAT.code.toDouble(),
                        ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                    )
                )
            )
        }
    }

    /** Picker mode: objects already attached must not be offered again. */
    fun excludeIds(ids: List<Id>): DVFilter = DVFilter(
        relation = Relations.ID,
        condition = DVFilterCondition.NOT_IN,
        value = ids
    )

    fun spaceScope(space: Id): DVFilter = DVFilter(
        relation = Relations.SPACE_ID,
        condition = DVFilterCondition.EQUAL,
        value = space
    )

    /**
     * Chat objects are not in the fulltext index — a text query on the Chats
     * bucket filters by name instead (and the caller must clear fullText).
     */
    fun chatNameQuery(query: String): DVFilter = DVFilter(
        relation = Relations.NAME,
        condition = DVFilterCondition.LIKE,
        value = query
    )

    /** Type objects are noise in the generic empty browse. */
    fun excludeTypeObjects(): DVFilter = DVFilter(
        relation = Relations.LAYOUT,
        condition = DVFilterCondition.NOT_IN,
        value = listOf(ObjectType.Layout.OBJECT_TYPE.code.toDouble())
    )

    /**
     * Sort for the empty-query browse — the SAME sort the client groups on
     * (day groups for the date orders, first-letter groups for Name), so the
     * server's order always matches the visible grouping (iOS §8.5). Text
     * queries pass no sorts — the backend defaults to relevance with a
     * deterministic tiebreak, which is the right cross-space merge order.
     * The Chats bucket always sorts by last message and stays ungrouped.
     */
    fun browseSorts(bucket: KindBucket?, sort: BrowseSort): List<DVSort> {
        if (bucket == KindBucket.CHAT) {
            return listOf(
                DVSort(
                    relationKey = Relations.LAST_MESSAGE_DATE,
                    type = DVSortType.DESC,
                    includeTime = true,
                    relationFormat = RelationFormat.DATE
                )
            )
        }
        return when (sort) {
            BrowseSort.NAME -> listOf(
                DVSort(
                    relationKey = Relations.NAME,
                    type = DVSortType.ASC,
                    includeTime = false,
                    relationFormat = RelationFormat.LONG_TEXT
                )
            )
            BrowseSort.CREATED -> listOf(
                DVSort(
                    relationKey = Relations.CREATED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true,
                    relationFormat = RelationFormat.DATE
                )
            )
            BrowseSort.EDITED -> listOf(
                DVSort(
                    relationKey = if (bucket == KindBucket.MEDIA) ADDED_DATE else Relations.LAST_MODIFIED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true,
                    relationFormat = RelationFormat.DATE
                )
            )
        }
    }

    /** The relation key the browse is grouped on, for a given sort. */
    fun browseGroupKey(bucket: KindBucket?, sort: BrowseSort): String = when (sort) {
        BrowseSort.NAME -> Relations.NAME
        BrowseSort.CREATED -> Relations.CREATED_DATE
        BrowseSort.EDITED -> if (bucket == KindBucket.MEDIA) ADDED_DATE else Relations.LAST_MODIFIED_DATE
    }
}
