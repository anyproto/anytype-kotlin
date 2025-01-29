package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem

sealed class TypeEvent {

    //region TopBar
    data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TypeEvent()
    data object OnSyncStatusDismiss : TypeEvent()
    data object OnMenuItemDeleteClick: TypeEvent()
    data object OnAlertDeleteDismiss: TypeEvent()
    data object OnAlertDeleteConfirm: TypeEvent()
    //endregion

    //region Object Type Header
    data object OnObjectTypeIconClick : TypeEvent()
    data class OnObjectTypeTitleUpdate(val title: String) : TypeEvent()
    //endregion

    //region Sets
    data object OnCreateSetClick : TypeEvent()
    data class OnOpenSetClick(val setId: Id) : TypeEvent()
    //endregion

    //region Objects list
    data object OnCreateObjectIconClick : TypeEvent()
    data class OnObjectItemClick(val item: UiObjectsListItem) : TypeEvent()
    //endregion

    data object OnSettingsClick : TypeEvent()

    data object OnLayoutButtonClick : TypeEvent()
    data object OnFieldsButtonClick : TypeEvent()
    data object OnTemplatesAddIconClick : TypeEvent()
    data class OnSortClick(val sort: ObjectsListSort) : TypeEvent()


    data object OnObjectsSettingsIconClick: TypeEvent()

}