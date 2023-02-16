package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface CollapsedWidgetStateHolder {

    fun onToggleCollapsedWidgetState(widget: Id)
    fun isCollapsed(widget: Id): Flow<Boolean>

    class Impl @Inject constructor(): CollapsedWidgetStateHolder {
        private val collapsedWidgets = MutableStateFlow<List<Id>>(emptyList())

        override fun onToggleCollapsedWidgetState(widget: Id) {
            val curr = collapsedWidgets.value
            if (curr.contains(widget)) {
                collapsedWidgets.value = curr.filter { it != widget }
            } else {
                collapsedWidgets.value = buildList {
                    addAll(curr)
                    add(widget)
                }
            }
        }

        override fun isCollapsed(widget: Id) = collapsedWidgets.map { collapsed ->
            collapsed.contains(widget)
        }
    }
}