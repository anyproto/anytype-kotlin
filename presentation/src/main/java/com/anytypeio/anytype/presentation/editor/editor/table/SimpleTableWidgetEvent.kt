package com.anytypeio.anytype.presentation.editor.editor.table

import com.anytypeio.anytype.core_models.Id

sealed interface SimpleTableWidgetEvent {
    data class onStart(val id: Id) : SimpleTableWidgetEvent
}