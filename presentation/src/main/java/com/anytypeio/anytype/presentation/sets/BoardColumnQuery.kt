package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key

/**
 * A single Kanban column's record query: its [columnId] (the backend group id, or the
 * synthesized [BOARD_EMPTY_GROUP_ID] for the "No value" column) and the [filter] that selects
 * the records belonging to it. Each query drives one paged record subscription.
 */
data class BoardColumnQuery(
    val columnId: Id,
    val filter: DVFilter
)

/**
 * Builds the per-column record queries for a board grouped by [groupRelationKey]. Each backend
 * group becomes a filter on [groupRelationKey] (mirroring `matchGroupId`): Status → contains the
 * option id, Tag → the exact option-id combination, Checkbox → the boolean. Non-checkbox boards
 * also get a synthesized "No value" column (filter: relation is empty) unless the backend already
 * returned an empty group. Date groups are skipped.
 */
fun boardColumnQueries(
    groups: List<DataViewGroup>,
    groupRelationKey: Key
): List<BoardColumnQuery> {
    val emptyFilter = DVFilter(relation = groupRelationKey, condition = DVFilterCondition.EMPTY)
    val queries = groups.mapNotNull { group ->
        // The backend's "No value" group arrives with id == BOARD_EMPTY_GROUP_ID; its value may be
        // encoded as an empty-id Status/Tag rather than Value.Empty. Always query it as "relation is
        // empty" so it matches unset records — and so it isn't also synthesized below as a duplicate
        // column sharing the same subscription id (which double-subscribed the "No value" column).
        if (group.id == BOARD_EMPTY_GROUP_ID) {
            BoardColumnQuery(columnId = group.id, filter = emptyFilter)
        } else {
            groupValueFilter(groupRelationKey, group.value)?.let { filter ->
                BoardColumnQuery(columnId = group.id, filter = filter)
            }
        }
    }
    // Synthesize the "No value" column for status/tag boards when the backend didn't return an
    // empty group; checkbox boards are exhaustively true/false and have no "No value" state.
    val isCheckbox = groups.any { it.value is DataViewGroup.Value.Checkbox }
    val hasEmpty = groups.any { it.value is DataViewGroup.Value.Empty || it.id == BOARD_EMPTY_GROUP_ID }
    return if (!isCheckbox && !hasEmpty) {
        queries + BoardColumnQuery(columnId = BOARD_EMPTY_GROUP_ID, filter = emptyFilter)
    } else {
        queries
    }
}

private fun groupValueFilter(key: Key, value: DataViewGroup.Value): DVFilter? = when (value) {
    is DataViewGroup.Value.Status ->
        DVFilter(relation = key, condition = DVFilterCondition.IN, value = listOf(value.id))
    is DataViewGroup.Value.Tag ->
        DVFilter(relation = key, condition = DVFilterCondition.EXACT_IN, value = value.ids)
    is DataViewGroup.Value.Checkbox ->
        DVFilter(relation = key, condition = DVFilterCondition.EQUAL, value = value.checked)
    is DataViewGroup.Value.Empty ->
        DVFilter(relation = key, condition = DVFilterCondition.EMPTY)
    is DataViewGroup.Value.Date -> null
}
