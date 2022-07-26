package com.anytypeio.anytype.presentation.editor.editor.table

sealed class SimpleTableWidgetViewState {
    object Idle : SimpleTableWidgetViewState()
    data class Active(val state: SimpleTableWidgetState) : SimpleTableWidgetViewState()
}