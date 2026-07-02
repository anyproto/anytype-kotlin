package com.anytypeio.anytype.core_models

/**
 * A data-view group (Kanban column) as returned by the backend group subscription.
 * [id] is the canonical group id; [value] describes how records belong to it and
 * what to write when a card is moved into it.
 */
data class DataViewGroup(
    val id: Id,
    val value: Value
) {
    sealed class Value {
        data class Status(val id: Id) : Value()
        data class Tag(val ids: List<Id>) : Value()
        data class Checkbox(val checked: Boolean) : Value()
        object Date : Value()
        object Empty : Value()
    }
}
