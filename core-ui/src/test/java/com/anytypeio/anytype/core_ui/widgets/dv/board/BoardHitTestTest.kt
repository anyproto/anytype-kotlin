package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [findCardAt] — the board-level hit-test that maps a long-press point to the
 * card (and its column) under it. This is what lets the drag gesture live on the stable
 * board container instead of inside a disposable LazyColumn item.
 */
class BoardHitTestTest {

    private fun card(id: String) = Viewer.Board.Card(
        objectId = id,
        name = id,
        icon = ObjectIcon.None,
        relations = emptyList(),
        hideIcon = false
    )

    private fun column(id: String, cards: List<Viewer.Board.Card>) =
        Viewer.Board.Column(id = id, label = id, color = null, cards = cards)

    @Test
    fun `returns the card and its column when the point is inside its bounds`() {
        val columns = listOf(
            column("col-1", listOf(card("a"), card("b"))),
            column("col-2", listOf(card("c")))
        )
        val bounds = mapOf(
            "a" to Rect(0f, 0f, 100f, 50f),
            "b" to Rect(0f, 60f, 100f, 110f),
            "c" to Rect(120f, 0f, 220f, 50f)
        )

        val hit = findCardAt(Offset(50f, 80f), columns, bounds)

        assertEquals(BoardCardHit(card = card("b"), columnId = "col-1"), hit)
    }

    @Test
    fun `resolves a card in another column`() {
        val columns = listOf(
            column("col-1", listOf(card("a"))),
            column("col-2", listOf(card("c")))
        )
        val bounds = mapOf(
            "a" to Rect(0f, 0f, 100f, 50f),
            "c" to Rect(120f, 0f, 220f, 50f)
        )

        val hit = findCardAt(Offset(150f, 25f), columns, bounds)

        assertEquals(BoardCardHit(card = card("c"), columnId = "col-2"), hit)
    }

    @Test
    fun `returns null when the point is in empty space`() {
        val columns = listOf(column("col-1", listOf(card("a"))))
        val bounds = mapOf("a" to Rect(0f, 0f, 100f, 50f))

        assertNull(findCardAt(Offset(500f, 500f), columns, bounds))
    }

    @Test
    fun `skips cards that have no laid-out bounds yet`() {
        val columns = listOf(column("col-1", listOf(card("a"))))

        assertNull(findCardAt(Offset(10f, 10f), columns, emptyMap()))
    }
}
