package com.anytypeio.anytype.presentation.navigation.backstack

import kotlin.test.assertEquals
import org.junit.Test

class BackHistoryMenuBuilderTest {

    private fun entry(
        entryId: String,
        objectId: String,
        space: String = SPACE
    ) = BackStackObjectEntry(
        entryId = entryId,
        objectId = objectId,
        space = space
    )

    @Test
    fun `should return empty list when back stack is empty`() {
        assertEquals(
            expected = emptyList(),
            actual = buildBackHistoryCandidates(entries = emptyList())
        )
    }

    @Test
    fun `should return empty list when back stack contains only the current screen`() {
        assertEquals(
            expected = emptyList(),
            actual = buildBackHistoryCandidates(
                entries = listOf(entry(entryId = "e1", objectId = "obj1"))
            )
        )
    }

    @Test
    fun `should drop the current screen entry and return previous ones oldest-first`() {
        val a = entry(entryId = "e1", objectId = "objA")
        val b = entry(entryId = "e2", objectId = "objB")
        val current = entry(entryId = "e3", objectId = "objC")

        assertEquals(
            expected = listOf(a, b),
            actual = buildBackHistoryCandidates(entries = listOf(a, b, current))
        )
    }

    @Test
    fun `should exclude entries sharing the current screen's object id at any depth`() {
        val sameAsCurrentDeep = entry(entryId = "e1", objectId = "objC")
        val a = entry(entryId = "e2", objectId = "objA")
        val sameAsCurrent = entry(entryId = "e3", objectId = "objC")
        val current = entry(entryId = "e4", objectId = "objC")

        assertEquals(
            expected = listOf(a),
            actual = buildBackHistoryCandidates(
                entries = listOf(sameAsCurrentDeep, a, sameAsCurrent, current)
            )
        )
    }

    @Test
    fun `should dedupe by object id keeping the most recent occurrence`() {
        val aOld = entry(entryId = "e1", objectId = "objA")
        val b = entry(entryId = "e2", objectId = "objB")
        val aRecent = entry(entryId = "e3", objectId = "objA")
        val current = entry(entryId = "e4", objectId = "objC")

        assertEquals(
            expected = listOf(b, aRecent),
            actual = buildBackHistoryCandidates(
                entries = listOf(aOld, b, aRecent, current)
            )
        )
    }

    @Test
    fun `should cap the result at the given limit`() {
        val entries = (1..8).map { idx ->
            entry(entryId = "e$idx", objectId = "obj$idx")
        }
        val current = entry(entryId = "e9", objectId = "obj9")

        val result = buildBackHistoryCandidates(entries = entries + current)

        assertEquals(expected = 5, actual = result.size)
        // keeps the 5 most-recent (obj4..obj8), displayed oldest-first
        assertEquals(
            expected = listOf("obj4", "obj5", "obj6", "obj7", "obj8"),
            actual = result.map { it.objectId }
        )
    }

    @Test
    fun `should collapse consecutive entries of the same object into one row`() {
        val aFirst = entry(entryId = "e1", objectId = "objA")
        val aSecond = entry(entryId = "e2", objectId = "objA")
        val current = entry(entryId = "e3", objectId = "objB")

        assertEquals(
            expected = listOf(aSecond),
            actual = buildBackHistoryCandidates(entries = listOf(aFirst, aSecond, current))
        )
    }

    @Test
    fun `should return empty list when all previous entries share the current object id`() {
        val a1 = entry(entryId = "e1", objectId = "objA")
        val a2 = entry(entryId = "e2", objectId = "objA")
        val current = entry(entryId = "e3", objectId = "objA")

        assertEquals(
            expected = emptyList(),
            actual = buildBackHistoryCandidates(entries = listOf(a1, a2, current))
        )
    }

    @Test
    fun `should exclude the current screen by entry id even when it is not the last entry`() {
        val a = entry(entryId = "e1", objectId = "objA")
        val b = entry(entryId = "e2", objectId = "objB")
        val current = entry(entryId = "e3", objectId = "objC")
        val trailing = entry(entryId = "e4", objectId = "objD")

        // current screen is "e3" but a stray entry sits after it in the back-stack list
        assertEquals(
            expected = listOf(a, b, trailing),
            actual = buildBackHistoryCandidates(
                entries = listOf(a, b, current, trailing),
                currentEntryId = "e3"
            )
        )
    }

    @Test
    fun `should fall back to the last entry as current when the current entry id is unknown`() {
        val a = entry(entryId = "e1", objectId = "objA")
        val b = entry(entryId = "e2", objectId = "objB")
        val current = entry(entryId = "e3", objectId = "objC")

        assertEquals(
            expected = listOf(a, b),
            actual = buildBackHistoryCandidates(
                entries = listOf(a, b, current),
                currentEntryId = "missing"
            )
        )
    }

    companion object {
        const val SPACE = "space-id"
    }
}
