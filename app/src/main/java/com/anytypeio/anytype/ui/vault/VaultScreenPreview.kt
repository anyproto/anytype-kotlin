package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
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