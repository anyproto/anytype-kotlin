package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat

/**
 * The outcome of dragging a Kanban card from one column to another.
 */
sealed interface BoardCardMove {
    /**
     * Write [value] to the card's group relation. A null [value] clears the relation
     * (the "No value" column); a list is the new (possibly empty) set of option ids;
     * a boolean is the new checkbox state.
     */
    data class Write(val value: Any?) : BoardCardMove

    /**
     * The move can't be resolved (e.g. a Date group) — the caller should skip the write.
     */
    data object Ignore : BoardCardMove
}

/**
 * Computes the value to write to the board's group relation when a card is dragged from
 * [sourceColumnId] to [targetColumnId]. The write is format-aware:
 *
 * - **Status** (single-select): a full replacement with the target option, cleared to
 *   null for the "No value" column.
 * - **Tag** (multi-select): a *read-modify-write* over the card's [currentValue] — it
 *   removes only the source column's option ids and adds the target's, preserving every
 *   other tag the card has. This is the key fix: writing the target column's value
 *   wholesale silently deletes a card's other tags.
 * - **Checkbox**: the target column's boolean.
 *
 * Column ids are resolved against the backend group ([sourceGroup] / [targetGroup]) when
 * available; otherwise (groups not loaded yet) the column id is treated as the option id,
 * which is the client-side fallback grouping.
 */
fun computeBoardCardMove(
    format: RelationFormat?,
    currentValue: List<Id>,
    sourceColumnId: Id,
    sourceGroup: DataViewGroup.Value?,
    targetColumnId: Id,
    targetGroup: DataViewGroup.Value?
): BoardCardMove {
    if (targetGroup is DataViewGroup.Value.Date) return BoardCardMove.Ignore

    val emptyTarget = targetColumnId == BOARD_EMPTY_GROUP_ID || targetGroup is DataViewGroup.Value.Empty

    // The backend group value is the most reliable signal of the relation format; fall
    // back to the relation's own format on the client-side (groups-not-loaded) path.
    return when (targetGroup.toFormat() ?: sourceGroup.toFormat() ?: format) {
        RelationFormat.STATUS ->
            BoardCardMove.Write(if (emptyTarget) null else listOf(statusId(targetColumnId, targetGroup)))

        RelationFormat.TAG -> {
            val sourceIds = tagIds(sourceColumnId, sourceGroup)
            val targetIds = if (emptyTarget) emptyList() else tagIds(targetColumnId, targetGroup)
            BoardCardMove.Write((currentValue - sourceIds.toSet() + targetIds).distinct())
        }

        RelationFormat.CHECKBOX -> when {
            targetGroup is DataViewGroup.Value.Checkbox -> BoardCardMove.Write(targetGroup.checked)
            emptyTarget -> BoardCardMove.Write(false)
            else -> BoardCardMove.Ignore
        }

        else -> BoardCardMove.Ignore
    }
}

private fun DataViewGroup.Value?.toFormat(): RelationFormat? = when (this) {
    is DataViewGroup.Value.Status -> RelationFormat.STATUS
    is DataViewGroup.Value.Tag -> RelationFormat.TAG
    is DataViewGroup.Value.Checkbox -> RelationFormat.CHECKBOX
    else -> null
}

private fun statusId(columnId: Id, group: DataViewGroup.Value?): Id =
    (group as? DataViewGroup.Value.Status)?.id ?: columnId

private fun tagIds(columnId: Id, group: DataViewGroup.Value?): List<Id> = when (group) {
    is DataViewGroup.Value.Tag -> group.ids
    is DataViewGroup.Value.Empty -> emptyList()
    null -> if (columnId == BOARD_EMPTY_GROUP_ID) emptyList() else listOf(columnId)
    else -> emptyList()
}
