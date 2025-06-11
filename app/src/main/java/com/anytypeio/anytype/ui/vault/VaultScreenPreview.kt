package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView
import com.anytypeio.anytype.ui.settings.typography

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "VaultScreen - Light Mode - With Spaces"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "VaultScreen - Dark Mode - With Spaces"
)
@Composable
fun VaultScreenWithSpacesPreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "John Doe",
                icon = ProfileIconView.Placeholder(name = "John Doe")
            ),
            spaces = listOf(
                VaultSpaceView.Space(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Work Space",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble(),
                            Relations.ID to "space-1"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.BLUE,
                        name = "Work Space"
                    ),
                    accessType = "Private"
                ),
                VaultSpaceView.Chat(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Team Chat",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble(),
                            Relations.ID to "space-2"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.GREEN,
                        name = "Team Chat"
                    ),
                    previewText = "Alice: Let's discuss the project timeline",
                    creatorName = "Alice",
                    messageText = "Let's discuss the project timeline",
                    messageTime = "14:30",
                    chatPreview = Chat.Preview(
                        space = SpaceId("space-2"),
                        chat = "chat-1",
                        message = Chat.Message(
                            id = "msg-1",
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = 0L,
                            attachments = emptyList(),
                            reactions = emptyMap(),
                            creator = "alice-id",
                            replyToMessageId = "",
                            content = Chat.Message.Content(
                                text = "Let's discuss the project timeline",
                                marks = emptyList(),
                                style = com.anytypeio.anytype.core_models.Block.Content.Text.Style.P
                            ),
                            order = "order-1"
                        )
                    )
                ),
                VaultSpaceView.Space(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Personal Notes",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble(),
                            Relations.ID to "space-3"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.PURPLE,
                        name = "Personal Notes"
                    ),
                    accessType = "Private"
                ),
                VaultSpaceView.Loading(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Loading Space",
                            Relations.ID to "space-4"
                        )
                    ),
                    icon = SpaceIconView.Loading
                )
            ),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "VaultScreen - Light Mode - Empty State"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "VaultScreen - Dark Mode - Empty State"
)
@Composable
fun VaultScreenEmptyStatePreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "Jane Smith",
                icon = ProfileIconView.Placeholder(name = "Jane Smith")
            ),
            spaces = emptyList(),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "VaultScreen - Light Mode - Chat Only"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "VaultScreen - Dark Mode - Chat Only"
)
@Composable
fun VaultScreenChatOnlyPreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "Bob Wilson",
                icon = ProfileIconView.Image(
                    url = "https://example.com/avatar.jpg",
                )
            ),
            spaces = listOf(
                VaultSpaceView.Chat(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Development Team",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble(),
                            Relations.ID to "dev-chat"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.RED,
                        name = "Development Team"
                    ),
                    previewText = "Sarah: The new feature is ready for testing",
                    creatorName = "Sarah",
                    messageText = "The new feature is ready for testing",
                    messageTime = "2 hours ago",
                    unreadMessageCount = 5,
                    unreadMentionCount = 1
                ),
                VaultSpaceView.Chat(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Design Discussion",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble(),
                            Relations.ID to "design-chat"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.YELLOW,
                        name = "Design Discussion"
                    ),
                    previewText = "Mike: What do you think about this color scheme?",
                    creatorName = "Mike",
                    messageText = "What do you think about this color scheme?",
                    messageTime = "Yesterday"
                )
            ),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "VaultScreen - Light Mode - Mixed Content"
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "VaultScreen - Dark Mode - Mixed Content"
)
@Composable
fun VaultScreenMixedContentPreview() {
    MaterialTheme(typography = typography) {
        VaultScreen(
            profile = AccountProfile.Data(
                name = "Alex Turner",
                icon = ProfileIconView.Placeholder(name = "AT")
            ),
            spaces = listOf(
                VaultSpaceView.Space(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Marketing Materials",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.SHARED.code.toDouble(),
                            Relations.ID to "marketing-space"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.PINK,
                        name = "Marketing Materials"
                    ),
                    accessType = "Shared"
                ),
                VaultSpaceView.Chat(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Quick Updates",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble(),
                            Relations.ID to "updates-chat"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.TEAL,
                        name = "Quick Updates"
                    ),
                    previewText = "System: Meeting scheduled for tomorrow at 2 PM",
                    creatorName = "System",
                    messageText = "Meeting scheduled for tomorrow at 2 PM",
                    messageTime = "1 hour ago"
                ),
                VaultSpaceView.Space(
                    space = ObjectWrapper.SpaceView(
                        mapOf(
                            Relations.NAME to "Research Documents",
                            Relations.SPACE_ACCESS_TYPE to SpaceAccessType.PRIVATE.code.toDouble(),
                            Relations.ID to "research-space"
                        )
                    ),
                    icon = SpaceIconView.Placeholder(
                        color = SystemColor.SKY,
                        name = "Research Documents"
                    ),
                    accessType = "Private"
                )
            ),
            onSpaceClicked = {},
            onCreateSpaceClicked = {},
            onSettingsClicked = {},
            onOrderChanged = {}
        )
    }
}

@Composable
@DefaultPreviews
fun ChatWithManyAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "File Archive",
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
        previewText = "Alice: Check out these designs",
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
        )
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
        icon = SpaceIconView.Placeholder(),
        previewText = "Bob: Here are the documents",
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
        )
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
        icon = SpaceIconView.Placeholder(),
        previewText = "Charlie: Found some useful links",
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
        )
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
        icon = SpaceIconView.Placeholder(),
        previewText = "Dana: Latest progress and resources",
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
        )
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
        messageTime = "09:15",
        // Single link should show object name instead of "1 Object"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
                title = "API Documentation"
            )
        )
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
        creatorName = "Alice",
        messageTime = "09:00",
        // Single image, no message: "Alice: [] Image"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                objectIcon = ObjectIcon.TypeIcon.Fallback.DEFAULT
            )
        )
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
        creatorName = "Charlie",
        messageTime = "11:00",
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
        icon = SpaceIconView.Placeholder(),
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
        )
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
        icon = SpaceIconView.Placeholder(),
        messageTime = "08:00"
    )
}