package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id

sealed class WidgetDispatchEvent {
    data class SourcePicked(val source: Id, val sourceLayout: Int) : WidgetDispatchEvent()
    data class TypePicked(val source: Id, val widgetType: Int) : WidgetDispatchEvent()
    data class SourceChanged(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int
    ) : WidgetDispatchEvent()
}