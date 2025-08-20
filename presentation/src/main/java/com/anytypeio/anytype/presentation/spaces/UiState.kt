package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class UiSpaceSettingsState {
    data object Initial : UiSpaceSettingsState()
    data class SpaceSettings(
        val spaceTechInfo: SpaceTechInfo,
        val items: List<UiSpaceSettingsItem>,
        val isEditEnabled: Boolean,
        val notificationState: NotificationState,
        val targetSpaceId: Id?
    ) : UiSpaceSettingsState() {
        val name: String = items.filterIsInstance<UiSpaceSettingsItem.Name>()
            .firstOrNull()?.name ?: EMPTY_STRING_VALUE
        val description = items.filterIsInstance<UiSpaceSettingsItem.Description>()
            .firstOrNull()?.description ?: EMPTY_STRING_VALUE
    }
    data class SpaceSettingsError(val message: String) : UiSpaceSettingsState()
}

data class SpaceTechInfo(
    val spaceId: SpaceId,
    val createdBy: String,
    val networkId: Id,
    val creationDateInMillis: Long?,
    val isDebugVisible: Boolean = false,
    val deviceToken: String? = null
)

sealed class UiSpaceSettingsItem {
    sealed class Section : UiSpaceSettingsItem() {
        data object Collaboration : Section()
        data object ContentModel : Section()
        data object Preferences : Section()
        data object DataManagement : Section()
        data object Misc : Section()
    }

    data class Spacer(val height: Int) : UiSpaceSettingsItem()
    data class Icon(val icon: SpaceIconView) : UiSpaceSettingsItem()
    data class Name(val name: String) : UiSpaceSettingsItem()
    data class Description(val description: String) : UiSpaceSettingsItem()
    data class InviteLink(val link: String) : UiSpaceSettingsItem()
    data class Members(val count: Int, val withColor: Boolean = false) : UiSpaceSettingsItem()
    data class MembersSmall(val count: Int) : UiSpaceSettingsItem()
    data object EntrySpace : UiSpaceSettingsItem()
    data object InviteMembers : UiSpaceSettingsItem()
    data class Chat(val isOn: Boolean) : UiSpaceSettingsItem()
    data object ObjectTypes : UiSpaceSettingsItem()
    data object Fields : UiSpaceSettingsItem()
    data class DefaultObjectType(val id: Id?, val name: String, val icon: ObjectIcon) : UiSpaceSettingsItem()
    data class Wallpapers(val current: Wallpaper?) : UiSpaceSettingsItem()
    data class AutoCreateWidgets(
        val widget: Id,
        val isAutoCreateEnabled: Boolean
    ) : UiSpaceSettingsItem()
    data object RemoteStorage : UiSpaceSettingsItem()
    data object Bin : UiSpaceSettingsItem()
    data object SpaceInfo : UiSpaceSettingsItem()
    data object DeleteSpace : UiSpaceSettingsItem()
    data object Notifications : UiSpaceSettingsItem()
}