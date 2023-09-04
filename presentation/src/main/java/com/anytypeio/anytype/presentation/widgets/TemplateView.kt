package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView

data class TemplatesWidgetUiState(
    val items: List<TemplateView>,
    val showWidget: Boolean,
    val isEditing: Boolean,
    val isMoreMenuVisible: Boolean,
    val moreMenuTemplate: TemplateView.Template?,
    val isDefaultStateEnabled: Boolean = false,
    val defaultObjectType: ObjectWrapper.Type? = null,
    val objectTypes: List<TemplateObjectTypeView> = listOf(TemplateObjectTypeView.Search)
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
            isDefaultStateEnabled = true,
            objectTypes = emptyList(),
            defaultObjectType = null
        )
    }
}
