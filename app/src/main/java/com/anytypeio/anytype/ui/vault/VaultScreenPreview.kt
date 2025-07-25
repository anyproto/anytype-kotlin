package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
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
        icon = SpaceIconView.Placeholder(),
        messageTime = "09:30",
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        creatorName = "Alice",
        messageText = "Check out these designs",
        messageTime = "10:45",
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        creatorName = "Bob",
        messageText = "Here are the documents",
        messageTime = "14:22",
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        creatorName = "Charlie",
        messageText = "Found some useful links",
        messageTime = "11:30",
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        creatorName = "Dana",
        messageText = "Latest progress and resources",
        messageTime = "16:30",
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        maxPinnedSpaces = 6,
        showPinButton = true,
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
        messageTime = "08:00",
        showPinButton = true,
        maxPinnedSpaces = 6
    )
}