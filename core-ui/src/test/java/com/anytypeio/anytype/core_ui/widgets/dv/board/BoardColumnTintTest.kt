package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.ui.geometry.Rect
import org.junit.Test
import kotlin.test.assertEquals

/**
 * A column list may be as tall as the board, so a long column runs edge to edge under the
 * floating controls. The tinted background, though, ends under the last item, so a short
 * column does not paint a tall empty strip.
 */
class BoardColumnTintTest {
    @Test
    fun `tint ends one gap under the last item of a short column`() {
        assertEquals(212f, boardColumnTintHeight(
            listHeight = 600f, lastVisibleIndex = 3, totalItemsCount = 4, lastVisibleEnd = 204f, gap = 8f
        ))
    }

    @Test
    fun `tint fills the list while items remain below the viewport`() {
        assertEquals(600f, boardColumnTintHeight(
            listHeight = 600f, lastVisibleIndex = 5, totalItemsCount = 40, lastVisibleEnd = 580f, gap = 8f
        ))
    }

    @Test
    fun `tint never exceeds the list`() {
        assertEquals(600f, boardColumnTintHeight(
            listHeight = 600f, lastVisibleIndex = 9, totalItemsCount = 10, lastVisibleEnd = 598f, gap = 8f
        ))
    }

    @Test
    fun `empty layout tints the whole list`() {
        assertEquals(600f, boardColumnTintHeight(
            listHeight = 600f, lastVisibleIndex = -1, totalItemsCount = 0, lastVisibleEnd = 0f, gap = 8f
        ))
    }

    @Test
    fun `card viewport stops above the floating controls only where the list reaches them`() {
        val tall = Rect(0f, 0f, 280f, 1000f)
        assertEquals(
            Rect(0f, 40f, 280f, 900f),
            boardCardViewport(tall, labelBottom = 40f, boardBottom = 1000f, clearance = 100f)
        )
        val short = Rect(0f, 0f, 280f, 300f)
        assertEquals(
            Rect(0f, 40f, 280f, 300f),
            boardCardViewport(short, labelBottom = 40f, boardBottom = 1000f, clearance = 100f)
        )
    }

    @Test
    fun `card viewport is never inverted`() {
        assertEquals(
            Rect(0f, 950f, 280f, 950f),
            boardCardViewport(Rect(0f, 900f, 280f, 1000f), labelBottom = 950f, boardBottom = 1000f, clearance = 100f)
        )
        assertEquals(
            Rect(0f, 0f, 280f, 0f),
            boardCardViewport(Rect(0f, 0f, 280f, 0f), labelBottom = null, boardBottom = 1000f, clearance = 100f)
        )
    }
}
