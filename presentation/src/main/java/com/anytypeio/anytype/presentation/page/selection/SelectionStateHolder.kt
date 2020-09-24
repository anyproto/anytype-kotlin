package com.anytypeio.anytype.presentation.page.selection

import com.anytypeio.anytype.domain.common.Id

interface SelectionStateHolder {

    fun select(targets: List<Id>)
    fun isSelected(target: Id): Boolean
    fun toggleSelection(target: Id)
    fun select(target: Id)
    fun unselect(target: Id)
    fun clearSelections()
    fun currentSelection(): Set<Id>

    class Default : SelectionStateHolder {

        private val memory = mutableMapOf<Id, Boolean>()

        override fun select(targets: List<Id>) {
            targets.forEach { target ->
                memory[target] = true
            }
        }

        override fun isSelected(target: Id) = memory[target] ?: false

        override fun toggleSelection(target: Id) {
            val value = memory[target]
            if (value != null)
                memory[target] = !value
            else
                memory[target] = true
        }

        override fun select(target: Id) {
            memory[target] = true
        }

        override fun unselect(target: Id) {
            memory[target] = false
        }

        override fun clearSelections() {
            memory.clear()
        }

        override fun currentSelection() = memory.filter { it.value }.keys
    }
}