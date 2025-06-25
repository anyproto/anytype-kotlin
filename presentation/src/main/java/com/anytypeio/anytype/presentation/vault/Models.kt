package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

sealed class VaultSpaceView {

    abstract val space: ObjectWrapper.SpaceView
    abstract val icon: SpaceIconView
    abstract val isOwner: Boolean
    abstract val isMuted: Boolean

    // Helper properties to determine unread status and last message date
    val hasUnreadMessages: Boolean
        get() = when (this) {
            is Chat -> unreadMessageCount > 0
            else -> false
        }

    val lastMessageDate: Long?
        get() = when (this) {
            is Chat -> chatPreview?.message?.createdAt
            else -> null
        }

    data class Space(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        val accessType: String,
        override val isOwner: Boolean,
        override val isMuted: Boolean
    ) : VaultSpaceView()

    data class Chat(
        override val space: ObjectWrapper.SpaceView,
        override val icon: SpaceIconView,
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        val chatMessage: com.anytypeio.anytype.core_models.chats.Chat.Message.Content? = null,
        val chatPreview: com.anytypeio.anytype.core_models.chats.Chat.Preview? = null,
        val previewText: String? = null,
        val creatorName: String? = null,
        val messageText: String? = null,
        val messageTime: String? = null,
        val attachmentPreviews: List<AttachmentPreview> = emptyList(),
        override val isOwner: Boolean,
        override val isMuted: Boolean
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

/**
 * Data structure for organizing vault spaces into sections using a two-tier approach:
 * 
 * ## Architecture:
 * 1. **regularOrder** - Single source of truth for user-defined space order (stored in VaultSettings.orderOfSpaces)
 * 2. **unreadSet** - Set of space IDs that currently have unread messages
 * 
 * ## Rendering Logic:
 * - **topSection**: Unread spaces sorted by lastMessageDate (newest first) - Non-draggable
 * - **mainSection**: All other spaces in user-defined order - Draggable
 * 
 * ## Key Principles:
 * - regularOrder changes ONLY when user drags in main section
 * - unreadSet is updated automatically on message arrival/read events
 * - We NEVER touch regularOrder when a chat becomes unread/read
 * - Chats automatically "return" to their saved position when marked as read
 * 
 * @property unreadSpaces List of spaces with unread messages, auto-sorted by lastMessageDate (newest first)
 * @property mainSpaces List of spaces in user-defined order (from VaultSettings.orderOfSpaces)
 */
data class VaultSectionView(
    val unreadSpaces: List<VaultSpaceView> = emptyList(),
    val mainSpaces: List<VaultSpaceView> = emptyList()
) {
    val hasUnreadSpaces: Boolean = unreadSpaces.isNotEmpty()
    
    /**
     * Gets all spaces as a flat list for backward compatibility
     */
    val allSpaces: List<VaultSpaceView> = unreadSpaces + mainSpaces
}

sealed class VaultCommand {
    data class EnterSpaceHomeScreen(val space: Space) : VaultCommand()
    data class EnterSpaceLevelChat(val space: Space, val chat: Id) : VaultCommand()
    data object CreateNewSpace : VaultCommand()
    data object CreateChat : VaultCommand()
    data object OpenProfileSettings : VaultCommand()
    data class ShowDeleteSpaceWarning(val space: Id) : VaultCommand()
    data class ShowLeaveSpaceWarning(val space: Id) : VaultCommand()

    sealed class Deeplink : VaultCommand() {
        data object DeepLinkToObjectNotWorking : Deeplink()
        data class Invite(val link: String) : Deeplink()
        data class GalleryInstallation(
            val deepLinkType: String,
            val deepLinkSource: String
        ) : Deeplink()

        data class MembershipScreen(val tierId: String?) : Deeplink()
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