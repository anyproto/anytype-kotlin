package com.anytypeio.anytype.core_models

data class ObjectOrder(
    val view: Id,
    val group: Id,
    val ids: List<Id>
)

data class GroupOrder(
    val viewId: Id,
    val viewGroups: List<ViewGroup>
)

data class ViewGroup(
    val groupId: Id,
    val index: Int,
    val hidden: Boolean,
    val backgroundColor: String
)
