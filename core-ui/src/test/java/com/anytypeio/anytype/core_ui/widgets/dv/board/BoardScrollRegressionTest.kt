package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardScrollRegressionTest {
    private fun column(count: Int = 3) = Viewer.Board.Column(
        id = "column", label = "Column", count = count,
        cards = listOf("a", "b", "c").map {
            Viewer.Board.Card(it, it, ObjectIcon.None, emptyList(), false)
        }
    )
    private val viewport = Rect(0f, 40f, 100f, 300f)
    private val visible = mapOf(
        "a" to Rect(0f, 40f, 100f, 80f),
        "b" to Rect(0f, 90f, 100f, 140f),
        "c" to Rect(0f, 150f, 100f, 200f)
    )

    @Test
    fun `sticky label occludes hit bounds without shrinking overlay bounds`() {
        val full = Rect(0f, 10f, 100f, 80f)
        assertEquals(Rect(0f, 40f, 100f, 80f), clippedBoardCardBounds(full, viewport))
        assertEquals(70f, full.height)
        assertNull(clippedBoardCardBounds(Rect(0f, 0f, 100f, 30f), viewport))
        assertNull(findCardAt(Offset(50f, 20f), listOf(column()), visible))
    }

    @Test
    fun `header and background do not imply same column append`() {
        assertNull(boardInsertionIndex(column(), "a", Offset(50f, 20f), visible, viewport, null))
        assertNull(boardInsertionIndex(column(), "a", Offset(50f, 270f), visible, viewport, null))
        assertEquals(1, boardInsertionIndex(column(), "a", Offset(50f, 145f), visible, viewport, null))
    }

    @Test
    fun `append requires explicit visible end item and fully loaded target`() {
        val end = Rect(0f, 210f, 100f, 234f)
        assertEquals(2, boardInsertionIndex(column(), "a", Offset(50f, 220f), visible, viewport, end))
        assertNull(boardInsertionIndex(column(count = 50), "a", Offset(50f, 220f), visible, viewport, end))
        assertNull(boardInsertionIndex(column(), "a", Offset(50f, 320f), visible, viewport,
            Rect(0f, 310f, 100f, 340f)))
    }

    @Test
    fun `explicit insertion retains indices of offscreen cards`() {
        assertEquals(2, boardInsertionIndex(column(), "new", Offset(50f, 160f),
            mapOf("c" to visible.getValue("c")), viewport, null))
    }

    @Test
    fun `lower half of last visible card does not append beyond unseen cards`() {
        val base = column()
        val longer = base.copy(cards = base.cards + base.cards.map { it.copy(objectId = "other-${it.objectId}") }, count = 6)
        assertEquals(3, boardInsertionIndex(longer, "new", Offset(50f, 190f), visible, viewport, null))
    }

    @Test
    fun `anchor resolves stable id after insertion and surviving neighbor after deletion`() {
        val anchor = BoardScrollAnchor("b", "c", "a", 1, 17)
        assertEquals(2, resolveBoardAnchor(anchor, listOf("new", "a", "b", "c")))
        assertEquals(1, resolveBoardAnchor(anchor, listOf("a", "c")))
        assertEquals(0, resolveBoardAnchor(anchor, listOf("a")))
        assertEquals(0, resolveBoardAnchor(anchor, emptyList()))
    }

    @Test
    fun `group loading and initial partial page cannot complete a deep anchor restore`() {
        val anchor = BoardScrollAnchor("c", null, "b", 9, 17)
        val loading = column().copy(cards = emptyList(), count = 0, hasLoadedRecords = false)
        assertFalse(boardAnchorResolved(anchor, loading))
        val firstPage = loading.copy(cards = column().cards.take(1), count = 10, hasLoadedRecords = true)
        assertFalse(boardAnchorResolved(anchor, firstPage))
        assertTrue(boardAnchorResolved(anchor, firstPage.copy(cards = column().cards)))
        // A committed empty result, unlike a loading placeholder, can finish via extent clamping.
        assertTrue(boardAnchorResolved(anchor, loading.copy(hasLoadedRecords = true)))
    }

    @Test
    fun `topology and grouping changes cancel drag but steady card updates do not`() {
        val board = Viewer.Board("viewer", "Board", listOf(column(), column().copy(id = "other")), "status")
        assertTrue(boardStructureChanged(board, board.copy(columns = board.columns.reversed())))
        assertTrue(boardStructureChanged(board, board.copy(columns = board.columns.take(1))))
        assertTrue(boardStructureChanged(board, board.copy(groupingKey = "assignee")))
        assertTrue(boardStructureChanged(board, board.copy(id = "another-viewer")))
        assertFalse(boardStructureChanged(board, board.copy(columns = board.columns.map {
            it.copy(cards = it.cards + it.cards.first().copy(objectId = "arriving-card"), count = it.count + 1)
        })))
    }

    @Test
    fun `row capture between data change and layout uses surviving measured column id`() {
        val reordered = rowScrollAnchor(1, 23, listOf("b" to -23, "c" to 300), listOf("b", "a", "c"))
        assertEquals("b", reordered.id)
        assertEquals(0, reordered.index)
        assertEquals(23, reordered.offset)
        val removed = rowScrollAnchor(1, 23, listOf("b" to -23, "c" to 300), listOf("a", "c"))
        assertEquals("c", removed.id)
        assertEquals(-300, removed.offset)
    }

    @Test
    fun `autoscroll distance is the same at 60 and 120 hz and clamps paused frames`() {
        val sixty = (1..60).sumOf { boardAutoScrollDistance(1, 2f, 16_666_667L).toDouble() }
        val oneTwenty = (1..120).sumOf { boardAutoScrollDistance(1, 2f, 8_333_333L).toDouble() }
        assertEquals(sixty, oneTwenty, 0.001)
        assertEquals(960.0, sixty, 0.001)
        assertEquals(48f, boardAutoScrollDistance(1, 2f, 5_000_000_000L))
        assertEquals(-48f, boardAutoScrollDistance(-1, 2f, 5_000_000_000L))
    }
}
