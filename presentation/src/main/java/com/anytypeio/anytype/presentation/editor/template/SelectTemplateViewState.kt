package com.anytypeio.anytype.presentation.editor.template

import com.anytypeio.anytype.core_models.Id

sealed class SelectTemplateViewState {
    object Idle : SelectTemplateViewState()
    data class Active(val count: Int, val typeId: Id) : SelectTemplateViewState()
}