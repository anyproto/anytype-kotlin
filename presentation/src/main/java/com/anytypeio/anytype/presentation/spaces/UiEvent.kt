package com.anytypeio.anytype.presentation.spaces

sealed class UiEvent {
    data object OnBackPressed : UiEvent()
    data class OnSavedClicked(val name: String, val description: String) : UiEvent()
    data object OnDeleteSpaceClicked : UiEvent()
    data object OnFileStorageClick : UiEvent()
    data object OnPersonalizationClicked : UiEvent()
    data object OnSpaceIdClicked : UiEvent()
    data class OnSpaceImagePicked(val uri: String) : UiEvent()
    data object OnInviteClicked : UiEvent()
    data object OnQrCodeClicked : UiEvent()

    sealed class IconMenu : UiEvent() {
        data object OnRemoveIconClicked : IconMenu()
    }

}