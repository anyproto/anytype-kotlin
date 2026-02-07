package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.presentation.templates.TemplateView

sealed class TypeEvent {

    //region TopBar
    data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TypeEvent()
    data object OnSyncStatusDismiss : TypeEvent()
    data object OnMenuClick : TypeEvent()
    data object OnMenuItemDeleteClick : TypeEvent()
    data object OnAlertDeleteDismiss : TypeEvent()
    data object OnAlertDeleteConfirm : TypeEvent()
    data object OnBackClick : TypeEvent()
    //endregion

    //region Delete Type Alert (Move to Bin confirmation)
    data object OnDeleteTypeAlertDismiss : TypeEvent()
    data object OnDeleteTypeAlertConfirm : TypeEvent()
    data class OnDeleteTypeAlertSelectAll(val isSelected: Boolean) : TypeEvent()
    data class OnDeleteTypeAlertToggleObject(val objectId: Id) : TypeEvent()
    //endregion

    //region Object Type Header
    data object OnObjectTypeIconClick : TypeEvent()
    data class OnObjectTypeTitleUpdate(val title: String) : TypeEvent()
    data object OnObjectTypeTitleClick : TypeEvent()
    data class OnDescriptionChanged(val text: String) : TypeEvent()
    //endregion

    //region Templates
    data object OnTemplatesModalListDismiss : TypeEvent()
    data object OnTemplatesAddIconClick : TypeEvent()
    data class OnTemplateItemClick(val item: TemplateView) : TypeEvent()
    sealed class OnTemplateMenuClick : TypeEvent() {
        data class SetAsDefault(val item: TemplateView) : OnTemplateMenuClick()
        data class Edit(val item: TemplateView) : OnTemplateMenuClick()
        data class Duplicate(val item: TemplateView) : OnTemplateMenuClick()
        data class Delete(val item: TemplateView) : OnTemplateMenuClick()
    }

    //endregion

    //region Layout type
    data object OnLayoutTypeDismiss : TypeEvent()
    data class OnLayoutTypeItemClick(val item: Layout) : TypeEvent()

    //endregion

    data object OnLayoutButtonClick : TypeEvent()
    data object OnPropertiesButtonClick : TypeEvent()
    data object OnTemplatesButtonClick : TypeEvent()

    //region Icon picker
    data class OnIconPickerItemClick(val iconName: String, val color: CustomIconColor?) : TypeEvent()
    data object OnIconPickerRemovedClick : TypeEvent()
    data object OnIconPickerDismiss : TypeEvent()
    //endregion
}