package com.anytypeio.anytype.feature_search.presentation

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchFiltersTest {

    @Test
    fun `base filters never exclude chat layouts - global search must find chats`() {
        val allowList = SearchFilters.base()
            .first { it.relation == Relations.LAYOUT && it.condition == DVFilterCondition.IN }
        val values = allowList.value as List<*>
        assertTrue(values.any { it == ObjectType.Layout.CHAT_DERIVED.code.toDouble() })
        assertTrue(values.any { it == ObjectType.Layout.CHAT.code.toDouble() })
    }

    @Test
    fun `creator filter includes identity and participant ids`() {
        val filters = SearchFilters.creatorToken(
            identity = "identity-1",
            participantIds = listOf("participant-a", "participant-b"),
            whatIsChatsBucket = false
        )
        val creator = filters.first { it.relation == Relations.CREATOR }
        assertEquals(
            listOf("participant-a", "participant-b", "identity-1"),
            creator.value
        )
    }

    @Test
    fun `creator filter excludes chat containers except for the chats bucket`() {
        val default = SearchFilters.creatorToken(
            identity = "identity-1",
            participantIds = emptyList(),
            whatIsChatsBucket = false
        )
        assertTrue(default.any { it.relation == Relations.LAYOUT && it.condition == DVFilterCondition.NOT_IN })

        val chatsBucket = SearchFilters.creatorToken(
            identity = "identity-1",
            participantIds = emptyList(),
            whatIsChatsBucket = true
        )
        assertTrue(chatsBucket.none { it.relation == Relations.LAYOUT })
    }

    @Test
    fun `type token filters by nested uniqueKey relation`() {
        val filters = SearchFilters.typeToken(SearchToken.TypeFilter(uniqueKey = "ot-task"))
        assertEquals(Relations.TYPE_UNIQUE_KEY, filters.single().relation)
        assertEquals("ot-task", filters.single().value)
    }

    @Test
    fun `chats bucket browse always sorts by last message regardless of the selected order`() {
        BrowseSort.entries.forEach { sort ->
            val sorts = SearchFilters.browseSorts(KindBucket.CHAT, sort)
            assertEquals(Relations.LAST_MESSAGE_DATE, sorts.single().relationKey)
        }
    }

    @Test
    fun `media bucket sorts and groups on addedDate under the edited order`() {
        assertEquals(
            SearchFilters.ADDED_DATE,
            SearchFilters.browseSorts(KindBucket.MEDIA, BrowseSort.EDITED).single().relationKey
        )
        assertEquals(
            SearchFilters.ADDED_DATE,
            SearchFilters.browseGroupKey(bucket = KindBucket.MEDIA, sort = BrowseSort.EDITED)
        )
        // Only the EDITED order remaps — created/name stay generic.
        assertEquals(
            Relations.CREATED_DATE,
            SearchFilters.browseGroupKey(bucket = KindBucket.MEDIA, sort = BrowseSort.CREATED)
        )
    }

    @Test
    fun `browse group key matches the sort field`() {
        assertEquals(
            Relations.LAST_MODIFIED_DATE,
            SearchFilters.browseGroupKey(bucket = null, sort = BrowseSort.EDITED)
        )
        assertEquals(
            Relations.CREATED_DATE,
            SearchFilters.browseGroupKey(bucket = null, sort = BrowseSort.CREATED)
        )
        assertEquals(
            Relations.NAME,
            SearchFilters.browseGroupKey(bucket = null, sort = BrowseSort.NAME)
        )
    }
}
