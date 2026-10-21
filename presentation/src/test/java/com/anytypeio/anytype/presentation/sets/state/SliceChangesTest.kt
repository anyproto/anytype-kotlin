package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Event.Command.DataView.ObjectOrderUpdate.SliceChange
import com.anytypeio.anytype.core_models.Event.Command.DataView.ObjectOrderUpdate.SliceOperation
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for [applySliceChanges] — the algebra that folds an ObjectOrderUpdate's slice
 * changes into an ordered id list (Kanban card order reconciliation).
 */
class SliceChangesTest {

    private fun change(op: SliceOperation, ids: List<String>, afterId: String = "") =
        SliceChange(operation = op, ids = ids, afterId = afterId)

    @Test
    fun `ADD with empty afterId inserts at the top`() {
        val result = listOf("a", "b", "c").applySliceChanges(
            listOf(change(SliceOperation.ADD, listOf("x"), afterId = ""))
        )
        assertEquals(listOf("x", "a", "b", "c"), result)
    }

    @Test
    fun `ADD after a known id inserts right after it`() {
        val result = listOf("a", "b", "c").applySliceChanges(
            listOf(change(SliceOperation.ADD, listOf("x"), afterId = "b"))
        )
        assertEquals(listOf("a", "b", "x", "c"), result)
    }

    @Test
    fun `ADD with an unknown afterId inserts at the top (backend semantics)`() {
        val result = listOf("a", "b", "c").applySliceChanges(
            listOf(change(SliceOperation.ADD, listOf("x"), afterId = "missing"))
        )
        assertEquals(listOf("x", "a", "b", "c"), result)
    }

    @Test
    fun `MOVE relocates an existing id after the anchor`() {
        val result = listOf("a", "b", "c", "d").applySliceChanges(
            listOf(change(SliceOperation.MOVE, listOf("d"), afterId = "a"))
        )
        assertEquals(listOf("a", "d", "b", "c"), result)
    }

    @Test
    fun `MOVE with an unknown afterId relocates to the top`() {
        val result = listOf("a", "b", "c", "d").applySliceChanges(
            listOf(change(SliceOperation.MOVE, listOf("d"), afterId = "ghost"))
        )
        assertEquals(listOf("d", "a", "b", "c"), result)
    }

    @Test
    fun `REMOVE drops the ids`() {
        val result = listOf("a", "b", "c").applySliceChanges(
            listOf(change(SliceOperation.REMOVE, listOf("b")))
        )
        assertEquals(listOf("a", "c"), result)
    }

    @Test
    fun `REPLACE overwrites the whole order`() {
        val result = listOf("a", "b", "c").applySliceChanges(
            listOf(change(SliceOperation.REPLACE, listOf("c", "a")))
        )
        assertEquals(listOf("c", "a"), result)
    }

    @Test
    fun `NONE leaves the list unchanged`() {
        val result = listOf("a", "b").applySliceChanges(
            listOf(change(SliceOperation.NONE, listOf("a")))
        )
        assertEquals(listOf("a", "b"), result)
    }
}
