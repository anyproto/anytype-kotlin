package com.anytypeio.anytype.presentation.editor.editor.table

sealed class SimpleTableWidgetState {

    object Idle : SimpleTableWidgetState()

    data class UpdateItems(
        val cellItems: List<SimpleTableWidgetItem>,
        val columnItems: List<SimpleTableWidgetItem>,
        val rowItems: List<SimpleTableWidgetItem>
    ) : SimpleTableWidgetState() {
        companion object {
            fun empty() = UpdateItems(
                cellItems = emptyList(),
                columnItems = emptyList(),
                rowItems = emptyList()
            )
        }
    }

    companion object {
        fun init(): SimpleTableWidgetState = Idle
    }
}