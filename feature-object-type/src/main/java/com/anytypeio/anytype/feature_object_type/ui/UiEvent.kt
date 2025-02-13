package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
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

    //region Sets
    data object OnCreateSetClick : TypeEvent()
    data class OnOpenSetClick(val setId: Id) : TypeEvent()
    //endregion

    //region Objects Header
    data class OnSortClick(val sort: ObjectsListSort) : TypeEvent()
    data object OnCreateObjectIconClick : TypeEvent()
    //endregion

    //region Objects list
    data class OnObjectItemClick(val item: UiObjectsListItem) : TypeEvent()
    //endregion

    //region Templates
    data object OnTemplatesAddIconClick : TypeEvent()
    data class OnTemplateItemClick(val item: TemplateView) : TypeEvent()
    data class OnTemplateMenuDuplicateClick(val item: TemplateView) : TypeEvent()
    data class OnTemplateMenuDeleteClick(val item: TemplateView) : TypeEvent()
    //endregion

    //region Layout type
    data object OnLayoutTypeDismiss : TypeEvent()
    data class OnLayoutTypeItemClick(val item: Layout) : TypeEvent()

    //endregion

    data object OnLayoutButtonClick : TypeEvent()
    data object OnFieldsButtonClick : TypeEvent()

    data object OnObjectsSettingsIconClick : TypeEvent()

}