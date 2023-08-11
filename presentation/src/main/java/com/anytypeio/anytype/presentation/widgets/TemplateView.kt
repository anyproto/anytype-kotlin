package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.presentation.templates.TemplateView

data class TemplatesWidgetUiState(
    val items: List<TemplateView>,
    val showWidget: Boolean,
    val isEditing: Boolean,
    val isMoreMenuVisible: Boolean,
    val moreMenuTemplate: TemplateView.Template?
) {
    companion object {
        fun empty() = TemplatesWidgetUiState(
            items = emptyList(),
            showWidget = false,
            isEditing = false,
            isMoreMenuVisible = false,
            moreMenuTemplate = null
        )
    }
}
