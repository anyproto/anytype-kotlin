package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.presentation.templates.TemplateView

class TemplatesWidgetUiState(
    val items: List<TemplateView>,
    val showWidget: Boolean
) {
    companion object {
        fun empty() = TemplatesWidgetUiState(
            items = emptyList(),
            showWidget = false
        )
    }
}
