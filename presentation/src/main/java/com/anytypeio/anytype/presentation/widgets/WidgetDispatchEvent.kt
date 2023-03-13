package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id

sealed class WidgetDispatchEvent {
    sealed class SourcePicked  : WidgetDispatchEvent() {
        data class Default(val source: Id, val sourceLayout: Int) : SourcePicked()

        /**
         * [source] bundled source - one of [BundledWidgetSourceIds]
         */
        data class Bundled(val source: Id) : SourcePicked()
    }
    data class TypePicked(val source: Id, val widgetType: Int) : WidgetDispatchEvent()
    data class SourceChanged(
        val ctx: Id,
        val widget: Id,
        val source: Id,
        val type: Int
    ) : WidgetDispatchEvent()
}