package com.anytypeio.anytype.core_ui.widgets.dv.board

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for [edgeAutoScroll] — the drag auto-scroll direction near a container edge (shared by
 * the horizontal row and the vertical per-column scroll).
 */
class BoardAutoScrollTest {

    private val edge = 56f
    private val size = 1000

    @Test
    fun `scrolls back near the near edge`() {
        assertEquals(-1, edgeAutoScroll(position = 10f, size = size, edge = edge))
    }

    @Test
    fun `scrolls forward near the far edge`() {
        assertEquals(1, edgeAutoScroll(position = 990f, size = size, edge = edge))
    }

    @Test
    fun `does not scroll in the middle`() {
        assertEquals(0, edgeAutoScroll(position = 500f, size = size, edge = edge))
    }

    @Test
    fun `does not scroll when the container is unmeasured`() {
        assertEquals(0, edgeAutoScroll(position = 10f, size = 0, edge = edge))
    }
}
