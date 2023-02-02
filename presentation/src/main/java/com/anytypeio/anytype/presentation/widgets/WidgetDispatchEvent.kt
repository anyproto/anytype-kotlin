package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id

sealed class WidgetDispatchEvent {
    data class SourcePicked(val source: Id) : WidgetDispatchEvent()
}