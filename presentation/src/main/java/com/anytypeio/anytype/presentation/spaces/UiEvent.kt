package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Id

sealed class UiEvent {
    data object OnBackPressed : UiEvent()

    data class OnSaveDescriptionClicked(val description: String) : UiEvent()
    data class OnSaveTitleClicked(val title: String) : UiEvent()
    data class OnSpaceImagePicked(val uri: String) : UiEvent()
    data object OnSelectWallpaperClicked : UiEvent()

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

    data class OnAutoCreateWidgetSwitchChanged(
        val widget: Id,
        val isAutoCreateEnabled: Boolean
    ) : UiEvent()

    sealed class IconMenu : UiEvent() {
        data object OnRemoveIconClicked : IconMenu()
    }

    sealed class OnNotificationsSetting : UiEvent() {
        abstract val targetSpaceId: Id?
        data class All(override val targetSpaceId: Id?) : OnNotificationsSetting()
        data class Mentions(override val targetSpaceId: Id?) : OnNotificationsSetting()
        data class None(override val targetSpaceId: Id?) : OnNotificationsSetting()
    }
}