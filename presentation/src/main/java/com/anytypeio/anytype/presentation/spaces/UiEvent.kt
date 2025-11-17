package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.wallpaper.WallpaperView

sealed class UiEvent {
    data object OnBackPressed : UiEvent()

    data class OnSaveDescriptionClicked(val description: String) : UiEvent()
    data class OnSaveTitleClicked(val title: String) : UiEvent()
    data class OnSpaceImagePicked(val uri: String) : UiEvent()

    data object OnSpaceMembersClicked : UiEvent()
    data class OnDefaultObjectTypeClicked(val currentDefaultObjectTypeId: Id?) : UiEvent()

    data object OnObjectTypesClicked : UiEvent()
    data object OnPropertiesClicked : UiEvent()

    data object OnDeleteSpaceClicked : UiEvent()
    data object OnLeaveSpaceClicked : UiEvent()
    data object OnRemoteStorageClick : UiEvent()
    data object OnBinClick : UiEvent()
    data object OnPersonalizationClicked : UiEvent()
    data object OnInviteClicked : UiEvent()
    data class OnCopyLinkClicked(val link: String) : UiEvent()
    data class OnShareLinkClicked(val link: String) : UiEvent()
    data class OnQrCodeClicked(val link: String) : UiEvent()
    data object OnDebugClicked : UiEvent()
    data object OnSpaceInfoTitleClicked : UiEvent()

    data class OnUpdateWallpaperClicked(val wallpaperView: WallpaperView) : UiEvent()

    sealed class IconMenu : UiEvent() {
        data object OnRemoveIconClicked : IconMenu()
        data object OnChangeIconColorClicked : IconMenu()
    }

    sealed class OnNotificationsSetting : UiEvent() {
        abstract val targetSpaceId: Id?
        data class All(override val targetSpaceId: Id?) : OnNotificationsSetting()
        data class Mentions(override val targetSpaceId: Id?) : OnNotificationsSetting()
        data class None(override val targetSpaceId: Id?) : OnNotificationsSetting()
    }

    data class OnResetChatNotification(val chatId: Id) : UiEvent()

    data object OnAddMoreSpacesClicked : UiEvent()

    data object OnChangeTypeClicked : UiEvent()

    sealed class OnChangeSpaceType : UiEvent() {
        data object ToChat : OnChangeSpaceType()
        data object ToSpace : OnChangeSpaceType()
    }
}