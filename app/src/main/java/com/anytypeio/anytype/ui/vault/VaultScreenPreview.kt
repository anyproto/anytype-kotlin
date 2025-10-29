package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

@Composable
@DefaultPreviews
fun ChatWithManyAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "File Archive",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "09:30",
        // No message text, so should show "5 Images"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            // These extra attachments should trigger the count text
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithManyMixedAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Mixed Files",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "14:15",
        // No message text, should show "6 Attachments"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            // Additional attachments for count
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Web Resource"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithImageAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Design Team",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Alice",
        messageText = "Check out these designs",
        messageTime = "10:45",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithFileAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Project Discussion",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Bob",
        messageText = "Here are the documents",
        messageTime = "14:22",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithLinkAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Resource Sharing",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Charlie",
        messageText = "Found some useful links",
        messageTime = "11:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Resource Link 1"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Bookmark(
                    image = "",
                    fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                ),
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Project Board"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithMixedAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Development Updates",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Dana",
        messageText = "Latest progress and resources",
        messageTime = "16:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "External Link"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Media Sharing",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "12:15",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Shared Resource"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithManyLinksNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Reference Links",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "15:45",
        // No message text, should show "5 Attachments" for mixed link/file types
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Analytics"
            ),
            // These extra attachments should trigger the count text
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Reference"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleLinkNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Link",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "09:15",
        // Single link should show object name instead of "1 Object"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "API Documentation"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleLinksOnlyNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Links Only",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "10:30",
        // Multiple links only should show "3 Objects"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Analytics"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleImageNoMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Image Demo",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Alice",
        messageTime = "09:00",
        // Single image, no message: "Alice: [] Image"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleLinkWithMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Link With Text",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Bob",
        messageText = "Check this out",
        messageTime = "10:00",
        // Single link with message: "Bob: [] API Documentation Check this out"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "API Documentation"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleImagesNoMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Images Demo",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Charlie",
        messageTime = "11:00",
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
                space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
                isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
        // Multiple images, no message: "Charlie: [][][] 3 Images"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleObjectsWithMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Objects Demo",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        creatorName = "Dana",
        messageText = "Here are some resources",
        messageTime = "12:00",
        // Multiple objects with message: "Dana: [][][] 3 Objects Here are some resources"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "Analytics"
            )
        ),
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Composable
@DefaultPreviews
fun ChatEmpty() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Empty Chat",
        icon = SpaceIconView.ChatSpace.Placeholder(),
        messageTime = "08:00",
        spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
        spaceView = VaultSpaceView.Space(
            space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
            isMuted = false,
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isOwner = true,
            accessType = "Owner"
        ),
    )
}

@Preview(showBackground = true, name = "SpaceActionsDropdownMenu - Muted Owner")
@Composable
fun PreviewSpaceActionsDropdownMenu_MutedOwner() {
    var expanded by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        SpaceActionsDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            isMuted = true,
            isPinned = false,
            onMuteToggle = {},
            onPinToggle = {},
            onSpaceSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "SpaceActionsDropdownMenu - Unmuted Not Owner")
@Composable
fun PreviewSpaceActionsDropdownMenu_UnmutedNotOwner() {
    var expanded by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize()) {
        SpaceActionsDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            isMuted = false,
            isPinned = false,
            onMuteToggle = {},
            onPinToggle = {},
            onSpaceSettings = {}
        )
    }
}

@Composable
@DefaultPreviews
fun ChatWithMention() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultChatCard(
            title = "B&O Museum",
            icon = SpaceIconView.ChatSpace.Placeholder(),
            creatorName = "John Doe",
            messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
            messageTime = "18:32",
            unreadMentionCount = 1,
            isMuted = true,
            chatPreview = Chat.Preview(
                space = SpaceId("space-id"),
                chat = "chat-id",
                message = Chat.Message(
                    id = "message-id",
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = 0L,
                    attachments = emptyList(),
                    reactions = emptyMap(),
                    creator = "creator-id",
                    replyToMessageId = "",
                    content = Chat.Message.Content(
                        text = "Hello, this is a preview message.",
                        marks = emptyList(),
                        style = Block.Content.Text.Style.P
                    ),
                    order = "order-id",
                    synced = false
                )
            ),
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
            spaceView = VaultSpaceView.Space(
                space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
                isMuted = false,
                icon = SpaceIconView.ChatSpace.Placeholder(),
                isOwner = true,
                accessType = "Owner"
            ),
        )
    }
}

@Composable
@DefaultPreviews
fun ChatPreview() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultChatCard(
            title = "B&O Museum",
            icon = SpaceIconView.ChatSpace.Placeholder(),
            creatorName = "John Doe",
            messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
            messageTime = "18:32",
            isMuted = false,
            isPinned = true,
            chatPreview = Chat.Preview(
                space = SpaceId("space-id"),
                chat = "chat-id",
                message = Chat.Message(
                    id = "message-id",
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = 0L,
                    attachments = emptyList(),
                    reactions = emptyMap(),
                    creator = "creator-id",
                    replyToMessageId = "",
                    content = Chat.Message.Content(
                        text = "Hello, this is a preview message.",
                        marks = emptyList(),
                        style = Block.Content.Text.Style.P
                    ),
                    order = "order-id",
                    synced = false
                )
            ),
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
            spaceView = VaultSpaceView.Space(
                space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1")),
                isMuted = false,
                icon = SpaceIconView.ChatSpace.Placeholder(),
                isOwner = true,
                accessType = "Owner"
            ),
        )
    }
}