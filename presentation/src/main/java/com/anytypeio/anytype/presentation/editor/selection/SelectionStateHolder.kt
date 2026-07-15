package com.anytypeio.anytype.presentation.editor.selection

import com.anytypeio.anytype.core_models.Id

interface SelectionStateHolder {

    fun select(targets: List<Id>)
    fun isSelected(target: Id): Boolean
    fun toggleSelection(target: Id)
    fun select(target: Id)
    fun unselect(target: Id)
    fun unselect(targets: List<Id>)
    fun clearSelections()
    fun currentSelection(): Set<Id>

    class Default : SelectionStateHolder {

        // Read during rendering on a background thread while being mutated on the main
        // thread. Selection order is meaningful (table cell operations depend on it), so
        // an insertion-ordered map guarded by a lock is used instead of ConcurrentHashMap.
        private val memory = linkedMapOf<Id, Boolean>()

        @Synchronized
        override fun select(targets: List<Id>) {
            targets.forEach { target ->
                memory[target] = true
            }
        }

        @Synchronized
        override fun isSelected(target: Id) = memory[target] ?: false

        @Synchronized
        override fun toggleSelection(target: Id) {
            val value = memory[target]
            if (value != null)
                memory[target] = !value
            else
                memory[target] = true
        }

        @Synchronized
        override fun select(target: Id) {
            memory[target] = true
        }

        @Synchronized
        override fun unselect(target: Id) {
            memory[target] = false
        }

        @Synchronized
        override fun unselect(targets: List<Id>) {
            targets.forEach { target ->
                memory[target] = false
            }
        }

        @Synchronized
        override fun clearSelections() {
            memory.clear()
        }

        @Synchronized
        override fun currentSelection(): Set<Id> =
            memory.filterTo(LinkedHashMap()) { it.value }.keys
    }
}
