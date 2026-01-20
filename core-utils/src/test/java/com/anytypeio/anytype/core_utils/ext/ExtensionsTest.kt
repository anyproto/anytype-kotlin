package com.anytypeio.anytype.core_utils.ext

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse

class ExtensionsTest {

    @Test
    fun shouldUpdateAllItemsInMutableList() {

        val mutableList = mutableListOf(10, 12, 14, 16, 20)

        mutableList.mapInPlace { num -> num + 3 }

        val expected = mutableListOf(13, 15, 17, 19, 23)

        mutableList.forEachIndexed { index, i ->
            assertEquals(expected[index], i)
        }
    }

    @Test
    fun shouldNotUpdateItemsInEmptyMutableList() {

        val mutableList = mutableListOf<Int>()

        mutableList.mapInPlace { num -> num + 3 }

        mutableList.forEachIndexed { index, i ->
            assertFalse(true)
        }
    }

    // moveAfterIndexInLine tests

    @Test
    fun `moveAfterIndexInLine should move item after specified item`() {
        // Given: [A, B, C, D], move C after A
        val list = mutableListOf("A", "B", "C", "D")

        // When
        list.moveAfterIndexInLine(
            predicateIndex = { it == "A" },
            predicateMove = { it == "C" }
        )

        // Then: [A, C, B, D]
        assertEquals(listOf("A", "C", "B", "D"), list)
    }

    @Test
    fun `moveAfterIndexInLine should move item to beginning when afterId not found`() {
        // Given: [A, B, C, D], move D to beginning (afterId doesn't match anything)
        val list = mutableListOf("A", "B", "C", "D")

        // When: predicateIndex never matches (simulates empty afterId)
        list.moveAfterIndexInLine(
            predicateIndex = { false },
            predicateMove = { it == "D" }
        )

        // Then: [D, A, B, C] - D moved to beginning
        assertEquals(listOf("D", "A", "B", "C"), list)
    }

    @Test
    fun `moveAfterIndexInLine should move multiple items after specified item`() {
        // Given: [A, B, C, D, E], move B and D after C
        val list = mutableListOf("A", "B", "C", "D", "E")

        // When
        list.moveAfterIndexInLine(
            predicateIndex = { it == "C" },
            predicateMove = { it == "B" || it == "D" }
        )

        // Then: [A, C, B, D, E]
        assertEquals(listOf("A", "C", "B", "D", "E"), list)
    }

    @Test
    fun `moveAfterIndexInLine should move item to end when afterId is last item`() {
        // Given: [A, B, C, D], move A after D
        val list = mutableListOf("A", "B", "C", "D")

        // When
        list.moveAfterIndexInLine(
            predicateIndex = { it == "D" },
            predicateMove = { it == "A" }
        )

        // Then: [B, C, D, A]
        assertEquals(listOf("B", "C", "D", "A"), list)
    }

    @Test
    fun `moveAfterIndexInLine should handle empty list`() {
        // Given: empty list
        val list = mutableListOf<String>()

        // When
        list.moveAfterIndexInLine(
            predicateIndex = { it == "A" },
            predicateMove = { it == "B" }
        )

        // Then: still empty
        assertEquals(emptyList<String>(), list)
    }

    @Test
    fun `moveAfterIndexInLine should do nothing when no items match predicateMove`() {
        // Given: [A, B, C], move X (doesn't exist)
        val list = mutableListOf("A", "B", "C")

        // When
        list.moveAfterIndexInLine(
            predicateIndex = { it == "B" },
            predicateMove = { it == "X" }
        )

        // Then: [A, B, C] - unchanged
        assertEquals(listOf("A", "B", "C"), list)
    }

    @Test
    fun `moveAfterIndexInLine should handle moving first item to beginning (idempotent)`() {
        // Given: [A, B, C], move A to beginning
        val list = mutableListOf("A", "B", "C")

        // When: afterId not found, A should go to beginning (where it already is)
        list.moveAfterIndexInLine(
            predicateIndex = { false },
            predicateMove = { it == "A" }
        )

        // Then: [A, B, C] - A stays at beginning
        assertEquals(listOf("A", "B", "C"), list)
    }

    @Test
    fun `moveAfterIndexInLine should handle real filter scenario - move name after tag`() {
        // Real scenario from bug: [name, createdDate, tag] -> move name after tag
        data class Filter(val id: String, val relation: String)

        val list = mutableListOf(
            Filter("id1", "name"),
            Filter("id2", "createdDate"),
            Filter("id3", "tag")
        )

        // When: move name (id1) after tag (id3)
        list.moveAfterIndexInLine(
            predicateIndex = { it.id == "id3" },
            predicateMove = { it.id == "id1" }
        )

        // Then: [createdDate, tag, name]
        assertEquals(
            listOf(
                Filter("id2", "createdDate"),
                Filter("id3", "tag"),
                Filter("id1", "name")
            ),
            list
        )
    }

    @Test
    fun `moveAfterIndexInLine should handle real filter scenario - move name to beginning`() {
        // Real bug scenario: [createdDate, backlinks, tag, name] -> move name to top (afterId="")
        data class Filter(val id: String, val relation: String)

        val list = mutableListOf(
            Filter("id2", "createdDate"),
            Filter("id4", "backlinks"),
            Filter("id3", "tag"),
            Filter("id1", "name")
        )

        // When: move name (id1) to beginning (afterId="" -> predicateIndex returns false)
        list.moveAfterIndexInLine(
            predicateIndex = { false }, // Simulates afterId=""
            predicateMove = { it.id == "id1" }
        )

        // Then: [name, createdDate, backlinks, tag]
        assertEquals(
            listOf(
                Filter("id1", "name"),
                Filter("id2", "createdDate"),
                Filter("id4", "backlinks"),
                Filter("id3", "tag")
            ),
            list
        )
    }
}