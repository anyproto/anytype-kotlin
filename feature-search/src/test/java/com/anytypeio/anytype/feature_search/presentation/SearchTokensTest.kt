package com.anytypeio.anytype.feature_search.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SearchTokensTest {

    @Test
    fun `adding a token of a filled group replaces it in place`() {
        val tokens = listOf(
            SearchToken.SpaceScope("space-1"),
            SearchToken.Kind(KindBucket.PAGE),
            SearchToken.Creator("identity-1")
        )
        val result = tokens.plusToken(SearchToken.Kind(KindBucket.MEDIA))
        assertEquals(
            listOf(
                SearchToken.SpaceScope("space-1"),
                SearchToken.Kind(KindBucket.MEDIA),
                SearchToken.Creator("identity-1")
            ),
            result
        )
    }

    @Test
    fun `a specific type replaces a bucket - same what slot`() {
        val tokens = listOf(SearchToken.Kind(KindBucket.PAGE))
        val result = tokens.plusToken(SearchToken.TypeFilter(uniqueKey = "ot-task"))
        assertEquals(listOf<SearchToken>(SearchToken.TypeFilter(uniqueKey = "ot-task")), result)
    }

    @Test
    fun `adding a token of an empty group appends`() {
        val tokens = listOf(SearchToken.Kind(KindBucket.PAGE))
        val result = tokens.plusToken(SearchToken.Creator("identity-1"))
        assertEquals(2, result.size)
    }

    @Test
    fun `focus tokens live in the what slot - a person focus evicts a bucket`() {
        // Non-obvious on purpose: a PERSON focus is a WHAT (the list becomes
        // their memberships), so it replaces the kind bucket, not the creator.
        val tokens = listOf(
            SearchToken.Kind(KindBucket.PAGE),
            SearchToken.Creator("identity-1")
        )
        val result = tokens.plusToken(SearchToken.PersonFocus("identity-2"))
        assertEquals(
            listOf(
                SearchToken.PersonFocus("identity-2"),
                SearchToken.Creator("identity-1")
            ),
            result
        )
    }

    @Test
    fun `a type focus evicts a chat filter - both are what tokens`() {
        val tokens = listOf(SearchToken.ChatFilter(chat = "chat-1", space = "space-1"))
        val result = tokens.plusToken(SearchToken.TypeFocus(uniqueKey = "ot-task"))
        assertEquals(listOf<SearchToken>(SearchToken.TypeFocus(uniqueKey = "ot-task")), result)
    }

    @Test
    fun `token ids are stable and unique per group value`() {
        assertEquals("space:s1", SearchToken.SpaceScope("s1").id)
        assertEquals("kind:MEDIA", SearchToken.Kind(KindBucket.MEDIA).id)
        assertEquals("type:ot-task", SearchToken.TypeFilter("ot-task").id)
        assertEquals("creator:i1", SearchToken.Creator("i1").id)
        assertEquals("chat:c1", SearchToken.ChatFilter(chat = "c1", space = "s1").id)
        // Pill selection keys on ids — a focus must never collide with the
        // plain filter of the same key/identity.
        assertNotEquals(
            SearchToken.TypeFilter("ot-task").id,
            SearchToken.TypeFocus("ot-task").id
        )
        assertNotEquals(
            SearchToken.Creator("i1").id,
            SearchToken.PersonFocus("i1").id
        )
    }

    @Test
    fun `type tokens are kept verbatim across the boundary in both directions`() {
        // type.uniqueKey Equal matches vault-wide — no bucket mapping, no
        // re-pointing (iOS B17).
        val tokens = listOf(SearchToken.TypeFilter(uniqueKey = "ot-task"))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = null))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = "space-1"))
    }

    @Test
    fun `entering a space drops global-only buckets but keeps messages and media`() {
        assertEquals(
            emptyList<SearchToken>(),
            listOf<SearchToken>(SearchToken.Kind(KindBucket.CHANNEL))
                .mapAcrossBoundary(newScope = "space-1")
        )
        assertEquals(
            listOf<SearchToken>(SearchToken.Kind(KindBucket.MESSAGE)),
            listOf<SearchToken>(SearchToken.Kind(KindBucket.MESSAGE))
                .mapAcrossBoundary(newScope = "space-1")
        )
        assertEquals(
            listOf<SearchToken>(SearchToken.Kind(KindBucket.MEDIA)),
            listOf<SearchToken>(SearchToken.Kind(KindBucket.MEDIA))
                .mapAcrossBoundary(newScope = "space-1")
        )
    }

    @Test
    fun `every listing-only bucket is global-only`() {
        assertEquals(
            setOf(
                KindBucket.CHANNEL,
                KindBucket.PAGE,
                KindBucket.BOOKMARK,
                KindBucket.COLLECTION,
                KindBucket.QUERY,
                KindBucket.CHAT,
                KindBucket.TYPE
            ),
            GLOBAL_ONLY_BUCKETS
        )
    }

    @Test
    fun `creator tokens always carry across the boundary`() {
        val tokens = listOf<SearchToken>(SearchToken.Creator("identity-1"))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = null))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = "space-1"))
    }

    @Test
    fun `a chat filter carries into its own space and going global`() {
        val tokens = listOf<SearchToken>(SearchToken.ChatFilter(chat = "c1", space = "space-1"))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = "space-1"))
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = null))
    }

    @Test
    fun `a chat filter widens to the messages bucket in a foreign space`() {
        val tokens = listOf<SearchToken>(SearchToken.ChatFilter(chat = "c1", space = "space-1"))
        assertEquals(
            listOf<SearchToken>(SearchToken.Kind(KindBucket.MESSAGE)),
            tokens.mapAcrossBoundary(newScope = "space-2")
        )
    }

    @Test
    fun `entering a space converts a type focus to the plain type filter`() {
        val tokens = listOf<SearchToken>(
            SearchToken.TypeFocus(uniqueKey = "ot-task", typeId = "type-id-1")
        )
        assertEquals(
            listOf<SearchToken>(
                SearchToken.TypeFilter(uniqueKey = "ot-task", typeId = "type-id-1")
            ),
            tokens.mapAcrossBoundary(newScope = "space-1")
        )
        // Going global keeps the focus.
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = null))
    }

    @Test
    fun `entering a space drops a person focus`() {
        val tokens = listOf<SearchToken>(SearchToken.PersonFocus("identity-1"))
        assertTrue(tokens.mapAcrossBoundary(newScope = "space-1").isEmpty())
        // Going global keeps the focus.
        assertEquals(tokens, tokens.mapAcrossBoundary(newScope = null))
    }
}
