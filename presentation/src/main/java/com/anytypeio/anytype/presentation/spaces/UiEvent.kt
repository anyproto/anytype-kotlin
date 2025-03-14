package com.anytypeio.anytype.presentation.spaces

sealed class UiEvent {
    data object OnBackPressed : UiEvent()

    data class OnSaveDescriptionClicked(val description: String) : UiEvent()
    data class OnSaveTitleClicked(val title: String) : UiEvent()
    data class OnSpaceImagePicked(val uri: String) : UiEvent()

    data object OnSpaceMembersClicked : UiEvent()
    data object OnDefaultObjectTypeClicked : UiEvent()

    data object OnDeleteSpaceClicked : UiEvent()
    data object OnFileStorageClick : UiEvent()
    data object OnPersonalizationClicked : UiEvent()
    data object OnSpaceIdClicked : UiEvent()
    data object OnInviteClicked : UiEvent()
    data object OnQrCodeClicked : UiEvent()
    data object OnWallpaperClicked : UiEvent()

    sealed class IconMenu : UiEvent() {
        data object OnRemoveIconClicked : IconMenu()
    }

}