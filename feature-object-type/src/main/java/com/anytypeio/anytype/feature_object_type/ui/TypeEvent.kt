package com.anytypeio.anytype.feature_object_type.ui

import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState

sealed class TypeEvent {

    data class OnSyncStatusClick(val status: SpaceSyncAndP2PStatusState) : TypeEvent()
    data object OnSettingsClick : TypeEvent()
    data object OnSyncStatusDismiss : TypeEvent()
    data class OnTitleUpdate(val title: String) : TypeEvent()
    data object OnLayoutButtonClick : TypeEvent()
    data object OnFieldsButtonClick : TypeEvent()
    data object OnTemplatesAddIconClick : TypeEvent()

}