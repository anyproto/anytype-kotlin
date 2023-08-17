package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.presentation.templates.TemplateView

data class TemplatesWidgetUiState(
    val items: List<TemplateView>,
    val showWidget: Boolean,
    val isEditing: Boolean,
    val isMoreMenuVisible: Boolean,
    val moreMenuTemplate: TemplateView.Template?,
    val isDefaultStateEnabled: Boolean = false
) {
    fun dismiss() = copy(
        showWidget = false,
        isEditing = false,
        isMoreMenuVisible = false,
        moreMenuTemplate = null,
        isDefaultStateEnabled = false
    )

    companion object {
        fun init() = TemplatesWidgetUiState(
            items = emptyList(),
            showWidget = false,
            isEditing = false,
            isMoreMenuVisible = false,
            moreMenuTemplate = null,
            isDefaultStateEnabled = false
        )
    }
}
