package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult

sealed class VaultSpaceView {

    abstract val space: ObjectWrapper.SpaceView
    abstract val icon: SpaceIconView
    abstract val isOwner: Boolean
    abstract val wallpaper: WallpaperResult
    val isPinned: Boolean get() = !space.spaceOrder.isNullOrEmpty()

    val lastMessageDate: Long?
        get() = when (this) {
            is ChatSpace -> chatPreview?.message?.createdAt
            is DataSpaceWithChat -> chatPreview?.message?.createdAt
            is OneToOneSpace -> chatPreview?.message?.createdAt
            else -> null
        }

    data class DataSpace(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        val accessType: String,
        override val isOwner: Boolean,
        override val wallpaper: WallpaperResult = WallpaperResult.None
    ) : VaultSpaceView()

    data class DataSpaceWithChat(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        override val isOwner: Boolean,
        override val wallpaper: WallpaperResult = WallpaperResult.None,
        val chatName: String = "",
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        val chatPreview: com.anytypeio.anytype.core_models.chats.Chat.Preview? = null,
        val creatorName: String? = null,
        val messageText: String? = null,
        val messageTime: String? = null,
        val attachmentPreviews: List<AttachmentPreview> = emptyList(),
        val chatNotificationState: NotificationState,
        val spaceNotificationState: NotificationState = NotificationState.ALL
    ) : VaultSpaceView()

    data class ChatSpace(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        override val isOwner: Boolean,
        override val wallpaper: WallpaperResult = WallpaperResult.None,
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        val chatMessage: com.anytypeio.anytype.core_models.chats.Chat.Message.Content? = null,
        val chatPreview: com.anytypeio.anytype.core_models.chats.Chat.Preview? = null,
        val creatorName: String? = null,
        val messageText: String? = null,
        val messageTime: String? = null,
        val attachmentPreviews: List<AttachmentPreview> = emptyList(),
        val spaceNotificationState: NotificationState = NotificationState.ALL
    ) : VaultSpaceView()

    data class OneToOneSpace(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        override val isOwner: Boolean,
        override val wallpaper: WallpaperResult = WallpaperResult.None,
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        val chatPreview: com.anytypeio.anytype.core_models.chats.Chat.Preview? = null,
        val messageText: String? = null,
        val messageTime: String? = null,
        val attachmentPreviews: List<AttachmentPreview> = emptyList(),
        val spaceNotificationState: NotificationState = NotificationState.ALL
    ) : VaultSpaceView()

    data class AttachmentPreview(
        val type: AttachmentType,
        val objectIcon: ObjectIcon,
        val title: String? = null
    )

    enum class AttachmentType {
        IMAGE, FILE, LINK
    }
}

sealed class VaultUiState {
    data object Loading : VaultUiState()
    data class Sections(
        val pinnedSpaces: List<VaultSpaceView> = emptyList(),
        val mainSpaces: List<VaultSpaceView> = emptyList()
    ) : VaultUiState()
}

sealed class VaultCommand {
    data class EnterSpaceHomeScreen(val space: Space) : VaultCommand()
    data class EnterSpaceLevelChat(val space: Space, val chat: Id) : VaultCommand()
    data class CreateNewSpace(val spaceUxType: SpaceUxType) : VaultCommand()
    data object OpenProfileSettings : VaultCommand()
    data class ShowDeleteSpaceWarning(val space: Id) : VaultCommand()
    data class ShowLeaveSpaceWarning(val space: Id) : VaultCommand()
    data class OpenSpaceSettings(val space: SpaceId) : VaultCommand()
    data object ScanQrCode : VaultCommand()
    data class NavigateToRequestJoinSpace(val link: String) : VaultCommand()

    sealed class Deeplink : VaultCommand() {
        data object DeepLinkToObjectNotWorking : Deeplink()
        data class Invite(val link: String) : Deeplink()
        data class GalleryInstallation(
            val deepLinkType: String,
            val deepLinkSource: String
        ) : Deeplink()

        data class MembershipScreen(val tierId: String?) : Deeplink()
        data class InitiateOneToOneChat(
            val identity: Id,
            val metadataKey: String
        ) : Deeplink()
    }
}

sealed class VaultNavigation {
    data class OpenChat(val ctx: Id, val space: Id) : VaultNavigation()
    data class OpenObject(val ctx: Id, val space: Id) : VaultNavigation()
    data class OpenSet(val ctx: Id, val space: Id, val view: Id?) : VaultNavigation()
    data class OpenDateObject(val ctx: Id, val space: Id) : VaultNavigation()
    data class OpenParticipant(val ctx: Id, val space: Id) : VaultNavigation()
    data class OpenType(val target: Id, val space: Id) : VaultNavigation()
    data class OpenUrl(val url: String) : VaultNavigation()
    data class ShowError(val message: String) : VaultNavigation()
}

sealed class VaultErrors {
    data object Hidden : VaultErrors()
    data object QrScannerError : VaultErrors()
    data object QrCodeIsNotValid : VaultErrors()
    data object CameraPermissionDenied : VaultErrors()
}