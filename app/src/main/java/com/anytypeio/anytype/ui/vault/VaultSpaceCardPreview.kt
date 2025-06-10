package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

@Composable
@DefaultPreviews
fun ChatWithImageAttachments() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
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
                imageUrl = "https://example.com/image1.jpg"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/image2.jpg"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/image3.jpg"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithFileAttachments() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
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
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-excel",
                fileExtension = "xlsx"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMixedAttachments() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Development Updates",
        icon = SpaceIconView.Placeholder(),
        previewText = "Charlie: Latest progress and mockups",
        creatorName = "Charlie",
        messageText = "Latest progress and mockups",
        messageTime = "16:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/mockup.png"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/zip",
                fileExtension = "zip"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithAttachmentsNoText() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Media Sharing",
        icon = SpaceIconView.Placeholder(),
        messageTime = "12:15",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/photo1.jpg"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/photo2.jpg"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithManyAttachments() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "File Archive",
        icon = SpaceIconView.Placeholder(),
        previewText = "Dana: Backup files from last quarter",
        creatorName = "Dana",
        messageText = "Backup files from last quarter",
        messageTime = "09:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-powerpoint",
                fileExtension = "pptx"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-word",
                fileExtension = "docx"
            ),
            // These shouldn't show due to max 3 limit
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = "https://example.com/image4.jpg"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "text/plain",
                fileExtension = "txt"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithAttachmentsOnlyText() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Document Review",
        icon = SpaceIconView.Placeholder(),
        messageTime = "11:20",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-excel",
                fileExtension = "xlsx"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatEmpty() {
    VaultChatCard(
        Modifier
            .padding(top = 52.dp)
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Empty Chat",
        icon = SpaceIconView.Placeholder(),
        messageTime = "08:00"
    )
}