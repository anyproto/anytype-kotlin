package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.presentation.objects.ObjectsListSort

sealed class TypeEvent {

    data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TypeEvent()
    data object OnSettingsClick : TypeEvent()
    data object OnSyncStatusDismiss : TypeEvent()
    data class OnTitleUpdate(val title: String) : TypeEvent()
    data object OnLayoutButtonClick : TypeEvent()
    data object OnFieldsButtonClick : TypeEvent()
    data object OnTemplatesAddIconClick : TypeEvent()
    data class OnSortClick(val sort: ObjectsListSort) : TypeEvent()
    data object OnCreateSetClick : TypeEvent()
    data object OnOpenSetClick : TypeEvent()
    data object OnObjectsSettingsIconClick: TypeEvent()

}