package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.RelationFormat
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for the format-aware, non-lossy value computed when a Kanban card is dragged
 * from one column to another (see [computeBoardCardMove]). The key guarantee is that a
 * Tag (multi-select) move is a read-modify-write that only touches the source/target
 * column's options and preserves the card's other tags — never a full replacement.
 */
class BoardCardMoveTest {

    // region Tag (multi-select) — read-modify-write, non-lossy

    @Test
    fun `tag move preserves the card's other tags (client-side fallback, single-tag columns)`() {
        // Card has [A, B], dragged from single-tag column A to single-tag column C.
        // Columns are option ids (groups not loaded yet). Must end up [B, C], not [C].
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "A",
            sourceGroup = null,
            targetColumnId = "C",
            targetGroup = null
        )

        assertEquals(BoardCardMove.Write(listOf("B", "C")), move)
    }

    @Test
    fun `tag move keeps tags untouched by source or target columns`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B", "C"),
            sourceColumnId = "B",
            sourceGroup = null,
            targetColumnId = "D",
            targetGroup = null
        )

        assertEquals(BoardCardMove.Write(listOf("A", "C", "D")), move)
    }

    @Test
    fun `tag move uses the backend group ids for combination columns`() {
        // Group path: a card in the [A, B] combination column dropped on the [C] column.
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "group-ab",
            sourceGroup = DataViewGroup.Value.Tag(ids = listOf("A", "B")),
            targetColumnId = "group-c",
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("C"))
        )

        assertEquals(BoardCardMove.Write(listOf("C")), move)
    }

    @Test
    fun `tag move to the no-value column removes only the source column's tags`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "A",
            sourceGroup = null,
            targetColumnId = BOARD_EMPTY_GROUP_ID,
            targetGroup = DataViewGroup.Value.Empty
        )

        assertEquals(BoardCardMove.Write(listOf("B")), move)
    }

    @Test
    fun `tag move from the no-value column adds the target tag`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = emptyList(),
            sourceColumnId = BOARD_EMPTY_GROUP_ID,
            sourceGroup = DataViewGroup.Value.Empty,
            targetColumnId = "group-c",
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("C"))
        )

        assertEquals(BoardCardMove.Write(listOf("C")), move)
    }

    @Test
    fun `tag move does not duplicate a tag the card already has`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "C"),
            sourceColumnId = "A",
            sourceGroup = null,
            targetColumnId = "C",
            targetGroup = null
        )

        assertEquals(BoardCardMove.Write(listOf("C")), move)
    }

    // endregion

    // region Status (single-select) — full replacement

    @Test
    fun `status move replaces the value with the target option (group path)`() {
        val move = computeBoardCardMove(
            format = RelationFormat.STATUS,
            currentValue = listOf("old"),
            sourceColumnId = "group-old",
            sourceGroup = DataViewGroup.Value.Status(id = "old"),
            targetColumnId = "group-new",
            targetGroup = DataViewGroup.Value.Status(id = "new")
        )

        assertEquals(BoardCardMove.Write(listOf("new")), move)
    }

    @Test
    fun `status move replaces the value with the target option (fallback path)`() {
        val move = computeBoardCardMove(
            format = RelationFormat.STATUS,
            currentValue = listOf("old"),
            sourceColumnId = "old",
            sourceGroup = null,
            targetColumnId = "new",
            targetGroup = null
        )

        assertEquals(BoardCardMove.Write(listOf("new")), move)
    }

    @Test
    fun `status move to the no-value column clears the relation`() {
        val move = computeBoardCardMove(
            format = RelationFormat.STATUS,
            currentValue = listOf("old"),
            sourceColumnId = "group-old",
            sourceGroup = DataViewGroup.Value.Status(id = "old"),
            targetColumnId = BOARD_EMPTY_GROUP_ID,
            targetGroup = DataViewGroup.Value.Empty
        )

        assertEquals(BoardCardMove.Write(null), move)
    }

    // endregion

    // region Checkbox

    @Test
    fun `checkbox move writes the target column's boolean`() {
        val move = computeBoardCardMove(
            format = RelationFormat.CHECKBOX,
            currentValue = emptyList(),
            sourceColumnId = "group-false",
            sourceGroup = DataViewGroup.Value.Checkbox(checked = false),
            targetColumnId = "group-true",
            targetGroup = DataViewGroup.Value.Checkbox(checked = true)
        )

        assertEquals(BoardCardMove.Write(true), move)
    }

    // endregion

    // region Unsupported

    @Test
    fun `date group move is ignored`() {
        val move = computeBoardCardMove(
            format = null,
            currentValue = emptyList(),
            sourceColumnId = "group-a",
            sourceGroup = DataViewGroup.Value.Date,
            targetColumnId = "group-b",
            targetGroup = DataViewGroup.Value.Date
        )

        assertEquals(BoardCardMove.Ignore, move)
    }

    // endregion
}
