package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat

/**
 * The relation formats the backend can build board columns from — the same three [groupValueFilter]
 * below knows how to turn back into record filters.
 *
 * Grouping by anything else fails `ObjectGroupsSubscribe` outright ("get grouper: unsupported
 * relation format"), so a view carrying such a key — a board configured on another client, or one
 * whose relation later changed format — has to be caught before the request is issued (DROID-4555).
 */
val BOARD_GROUP_BY_FORMATS = setOf(
    RelationFormat.STATUS,
    RelationFormat.TAG,
    RelationFormat.CHECKBOX
)

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
    // The backend's "No value" group is normalized to Value.Empty by the middleware group mapper
    // (ToCoreModelMappers.toCoreModelsGroup), so it maps to an "relation is empty" filter here like
    // any other value — no need to special-case the magic group id.
    val queries = groups.mapNotNull { group ->
        groupValueFilter(groupRelationKey, group.value)?.let { filter ->
            BoardColumnQuery(columnId = group.id, filter = filter)
        }
    }
    // Synthesize the "No value" column for status/tag boards when the backend didn't return an
    // empty group; checkbox boards are exhaustively true/false and have no "No value" state.
    val isCheckbox = groups.any { it.value is DataViewGroup.Value.Checkbox }
    val hasEmpty = groups.any { it.value is DataViewGroup.Value.Empty }
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
