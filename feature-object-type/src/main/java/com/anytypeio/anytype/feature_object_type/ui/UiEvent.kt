package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.presentation.templates.TemplateView

sealed class TypeEvent {

    //region TopBar
    data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TypeEvent()
    data object OnSyncStatusDismiss : TypeEvent()
    data object OnMenuItemDeleteClick : TypeEvent()
    data object OnAlertDeleteDismiss : TypeEvent()
    data object OnAlertDeleteConfirm : TypeEvent()
    data object OnBackClick : TypeEvent()
    //endregion

    //region Object Type Header
    data object OnObjectTypeIconClick : TypeEvent()
    data class OnObjectTypeTitleUpdate(val title: String) : TypeEvent()
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
    data object OnFieldsButtonClick : TypeEvent()
    data object OnTemplatesButtonClick : TypeEvent()
}