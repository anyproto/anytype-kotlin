package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult

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

sealed class UiSpaceQrCodeState {
    data object Hidden : UiSpaceQrCodeState()
    data class SpaceInvite(
        val link: String,
        val spaceName: String,
        val icon: SpaceIconView?
    ) : UiSpaceQrCodeState()
}

data class SpaceTechInfo(
    val spaceId: SpaceId,
    val createdBy: String,
    val networkId: Id,
    val creationDateInMillis: Long?,
    val isDebugVisible: Boolean = false,
    val deviceToken: String? = null,
    val isOneToOne: Boolean = false
)

/**
 * Represents a chat with a custom notification state different from the space default.
 */
data class ChatNotificationItem(
    val id: Id,
    val name: String,
    val icon: ObjectIcon,
    val customState: NotificationState
)

sealed class UiSpaceSettingsItem {
    abstract val key: String

    sealed class Section : UiSpaceSettingsItem() {
        data object Collaboration : Section() {
            override val key = "section-collaboration"
        }
        data object ContentModel : Section() {
            override val key = "section-content-model"
        }
        data object Preferences : Section() {
            override val key = "section-preferences"
        }
        data object DataManagement : Section() {
            override val key = "section-data-management"
        }
        data object Misc : Section() {
            override val key = "section-misc"
        }
    }

    data class Spacer(val id: String, val height: Int) : UiSpaceSettingsItem() {
        override val key = "spacer-$id"
    }
    data class Icon(val icon: SpaceIconView) : UiSpaceSettingsItem() {
        override val key = "icon"
    }
    data class Name(val name: String) : UiSpaceSettingsItem() {
        override val key = "name"
    }
    data class ParticipantIdentity(
        val name: String,
        val globalName: String?,
        val identity: String?
    ) : UiSpaceSettingsItem() {
        override val key = "participant-identity"
    }
    data class Description(val description: String) : UiSpaceSettingsItem() {
        override val key = "description"
    }
    data class InviteLink(val link: String) : UiSpaceSettingsItem() {
        override val key = "invite-link"
    }
    data class Members(val count: Int?, val withColor: Boolean = false, val editorLimit: Boolean = false) : UiSpaceSettingsItem() {
        override val key = "members"
    }
    data class MembersSmall(val count: Int) : UiSpaceSettingsItem() {
        override val key = "members-small"
    }
    data object EntrySpace : UiSpaceSettingsItem() {
        override val key = "entry-space"
    }
    data object InviteMembers : UiSpaceSettingsItem() {
        override val key = "invite-members"
    }
    data class Chat(val isOn: Boolean) : UiSpaceSettingsItem() {
        override val key = "chat"
    }
    data object ObjectTypes : UiSpaceSettingsItem() {
        override val key = "object-types"
    }
    data object Fields : UiSpaceSettingsItem() {
        override val key = "fields"
    }
    data class DefaultObjectType(val id: Id?, val name: String, val icon: ObjectIcon) : UiSpaceSettingsItem() {
        override val key = "default-object-type"
    }
    data class Wallpapers(val wallpaper: WallpaperResult, val spaceIconView: SpaceIconView) : UiSpaceSettingsItem() {
        override val key = "wallpapers"
    }
    data object RemoteStorage : UiSpaceSettingsItem() {
        override val key = "remote-storage"
    }
    data object Bin : UiSpaceSettingsItem() {
        override val key = "bin"
    }
    data object SpaceInfo : UiSpaceSettingsItem() {
        override val key = "space-info"
    }
    data object DeleteSpace : UiSpaceSettingsItem() {
        override val key = "delete-space"
    }
    data object LeaveSpace : UiSpaceSettingsItem() {
        override val key = "leave-space"
    }
    data object Notifications : UiSpaceSettingsItem() {
        override val key = "notifications"
    }

    sealed class ChangeType : UiSpaceSettingsItem() {
        abstract val isEnabled: Boolean
        data class Data(override val isEnabled: Boolean = false) : ChangeType() {
            override val key = "change-type-data"
        }
        data class Chat(override val isEnabled: Boolean = false) : ChangeType() {
            override val key = "change-type-chat"
        }
    }
}