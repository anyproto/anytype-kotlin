package com.anytypeio.anytype.core_models

/**
 * Per-view saved ordering of data-view groups (e.g. Kanban columns).
 */
data class GroupOrder(
    val viewId: Id,
    val viewGroups: List<ViewGroup>
)

data class ViewGroup(
    val groupId: Id,
    val index: Int,
    val isHidden: Boolean,
    val backgroundColor: String
)
