package com.anytypeio.anytype.core_ui.widgets.dv.board

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for [reorderInsertIndex] — the within-column drop position. The key guarantee is
 * that cards scrolled above the viewport (no measured midpoint) still count toward the
 * index, so reordering in a column taller than the screen doesn't corrupt the order.
 */
class BoardReorderTest {

    @Test
    fun `counts off-screen-above cards via the full-list index`() {
        // Column [a,b,c,d,e,f]; e is being dragged → remaining [a,b,c,d,f].
        // Only d and f are on-screen (a,b,c scrolled above → no midpoint). Pointer sits
        // between d and f, so the card must land at index 4 (…d, e, f), not near the top.
        val remaining = listOf("a", "b", "c", "d", "f")
        val mid = mapOf("d" to 100f, "f" to 200f)

        val index = reorderInsertIndex(remaining, pointerY = 150f) { mid[it] }

        assertEquals(4, index)
    }

    @Test
    fun `pointer above all visible cards inserts before the first visible card`() {
        val remaining = listOf("a", "b", "c", "d", "f")
        val mid = mapOf("d" to 100f, "f" to 200f)

        val index = reorderInsertIndex(remaining, pointerY = 50f) { mid[it] }

        assertEquals(3, index) // before d (index 3), after the off-screen a,b,c
    }

    @Test
    fun `pointer below all visible cards appends`() {
        val remaining = listOf("a", "b", "c", "d", "f")
        val mid = mapOf("d" to 100f, "f" to 200f)

        val index = reorderInsertIndex(remaining, pointerY = 300f) { mid[it] }

        assertEquals(5, index)
    }

    @Test
    fun `fully visible column inserts between the two cards under the pointer`() {
        val remaining = listOf("a", "b", "c")
        val mid = mapOf("a" to 10f, "b" to 30f, "c" to 50f)

        val index = reorderInsertIndex(remaining, pointerY = 20f) { mid[it] }

        assertEquals(1, index) // between a and b
    }

    @Test
    fun `order is persisted only when the column is fully loaded`() {
        // All records loaded → persisting the client order is safe.
        assertEquals(true, isColumnFullyLoaded(loadedCards = 12, count = 12))
        // Only a page loaded of a larger column → persisting would truncate the backend order.
        assertEquals(false, isColumnFullyLoaded(loadedCards = 50, count = 120))
    }
}
