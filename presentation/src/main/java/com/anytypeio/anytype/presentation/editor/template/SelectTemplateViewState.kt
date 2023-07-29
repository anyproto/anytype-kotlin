package com.anytypeio.anytype.presentation.editor.template

sealed class SelectTemplateViewState {
    object Idle : SelectTemplateViewState()
    data class Active(val count: Int, val typeName: String) : SelectTemplateViewState()
}