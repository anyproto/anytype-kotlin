package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.RelationFormat
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests for the format-aware, non-lossy value computed when a Kanban card is dragged
 * from one column to another (see [computeBoardCardMove]). The key guarantees are that a
 * Tag (multi-select) move is a read-modify-write that only touches the source/target
 * column's options and preserves the card's other tags — never a full replacement — and
 * that, once the backend groups are loaded, an unresolved column never has its (hashed)
 * column id written as if it were an option id.
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
            targetGroup = null,
            groupsLoaded = false
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
            targetGroup = null,
            groupsLoaded = false
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
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("C")),
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Write(listOf("C")), move)
    }

    @Test
    fun `tag move collapses a multi-tag card to the target on a combination board`() {
        // Pins the intended on-device behavior (verified via logcat): when the card's full
        // tag set IS the source combination column, there is nothing outside the source to
        // keep, so the card adopts the target column's tags. This is deliberate for the
        // backend combination-column model — not the old "write the target wholesale" bug,
        // which the read-modify-write still prevents whenever the card has tags outside its
        // source column (see the single-tag fallback tests above).
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("tag1", "tag3", "tag6"),
            sourceColumnId = "group-tag1-tag3-tag6",
            sourceGroup = DataViewGroup.Value.Tag(ids = listOf("tag1", "tag3", "tag6")),
            targetColumnId = "group-tag2",
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("tag2")),
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Write(listOf("tag2")), move)
    }

    @Test
    fun `tag move to the no-value column removes only the source column's tags`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "A",
            sourceGroup = null,
            targetColumnId = BOARD_EMPTY_GROUP_ID,
            targetGroup = DataViewGroup.Value.Empty,
            groupsLoaded = false
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
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("C")),
            groupsLoaded = true
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
            targetGroup = null,
            groupsLoaded = false
        )

        assertEquals(BoardCardMove.Write(listOf("C")), move)
    }

    // endregion

    // region Unresolved-group guard (M3) — never write a hashed column id as an option id

    @Test
    fun `tag move is ignored when groups are loaded but the target column is unresolved`() {
        // A real Tag column id is a group hash, not an option id. If it doesn't resolve to a
        // loaded group, refuse the write rather than persisting the hash into the relation.
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "group-a",
            sourceGroup = DataViewGroup.Value.Tag(ids = listOf("A")),
            targetColumnId = "5f4d3c2b1a",
            targetGroup = null,
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Ignore, move)
    }

    @Test
    fun `tag move is ignored when groups are loaded but the source column is unresolved`() {
        val move = computeBoardCardMove(
            format = RelationFormat.TAG,
            currentValue = listOf("A", "B"),
            sourceColumnId = "1a2b3c4d5e",
            sourceGroup = null,
            targetColumnId = "group-c",
            targetGroup = DataViewGroup.Value.Tag(ids = listOf("C")),
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Ignore, move)
    }

    @Test
    fun `checkbox move is ignored when groups are loaded but the target column is unresolved`() {
        val move = computeBoardCardMove(
            format = RelationFormat.CHECKBOX,
            currentValue = emptyList(),
            sourceColumnId = "group-false",
            sourceGroup = DataViewGroup.Value.Checkbox(checked = false),
            targetColumnId = "deadbeef",
            targetGroup = null,
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Ignore, move)
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
            targetGroup = DataViewGroup.Value.Status(id = "new"),
            groupsLoaded = true
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
            targetGroup = null,
            groupsLoaded = false
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
            targetGroup = DataViewGroup.Value.Empty,
            groupsLoaded = true
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
            targetGroup = DataViewGroup.Value.Checkbox(checked = true),
            groupsLoaded = true
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
            targetGroup = DataViewGroup.Value.Date,
            groupsLoaded = true
        )

        assertEquals(BoardCardMove.Ignore, move)
    }

    // endregion
}
