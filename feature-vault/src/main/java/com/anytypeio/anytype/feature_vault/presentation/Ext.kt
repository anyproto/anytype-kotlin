package com.anytypeio.anytype.feature_vault.presentation

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.ui.AttachmentPreview
import com.anytypeio.anytype.core_models.ui.AttachmentType
import com.anytypeio.anytype.core_models.ui.MimeCategory
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.core_models.ui.objectIcon
import timber.log.Timber

// Helper to produce the "default" fallback icon when dependency is missing or invalid
fun defaultIconFor(type: Chat.Message.Attachment.Type): ObjectIcon = when (type) {
    Chat.Message.Attachment.Type.Image ->
        ObjectIcon.FileDefault(mime = MimeCategory.IMAGE)

    Chat.Message.Attachment.Type.File ->
        ObjectIcon.FileDefault(mime = MimeCategory.OTHER)

    Chat.Message.Attachment.Type.Link ->
        ObjectIcon.TypeIcon.Default.DEFAULT
}

suspend fun mapToAttachmentPreview(
    attachment: Chat.Message.Attachment,
    dependency: ObjectWrapper.Basic?,
    urlBuilder: UrlBuilder,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): AttachmentPreview {

    // Check if we have a valid dependency with ID
    val hasValidDependency = dependency != null && dependency.isValid

    // Determine the actual type based on MIME type if available
    val effectiveType = if (hasValidDependency && attachment.type == Chat.Message.Attachment.Type.File) {
        val mimeType = dependency.getSingleValue<String>(Relations.FILE_MIME_TYPE)
        when {
            mimeType?.startsWith("image/") == true -> Chat.Message.Attachment.Type.Image
            else -> attachment.type
        }
    } else {
        attachment.type
    }

    // Helper to pick the preview‐type enum based on effective type
    val previewType = when (effectiveType) {
        Chat.Message.Attachment.Type.Image -> AttachmentType.IMAGE
        Chat.Message.Attachment.Type.File -> AttachmentType.FILE
        Chat.Message.Attachment.Type.Link -> AttachmentType.LINK
    }

    // Build the icon based on whether we have valid dependency data
    val icon = if (hasValidDependency) {
        try {
            when (effectiveType) {
                Chat.Message.Attachment.Type.Image -> {
                    // For image attachments, create an ObjectIcon.Basic.Image with the actual image URL
                    val imageHash = dependency.id
                    val imageUrl = urlBuilder.thumbnail(imageHash)
                    ObjectIcon.Basic.Image(
                        hash = imageUrl,
                        fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                    )
                }

                Chat.Message.Attachment.Type.File -> {
                    val mime = dependency.getSingleValue<String>(Relations.FILE_MIME_TYPE)
                    val ext = dependency.getSingleValue<String>(Relations.FILE_EXT)
                    ObjectIcon.File(mime = mime, extensions = ext)
                }

                Chat.Message.Attachment.Type.Link ->
                    dependency.objectIcon(
                        builder = urlBuilder,
                        objType = storeOfObjectTypes.getTypeOfObject(dependency)
                    )
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to create icon for attachment ${attachment.target}")
            defaultIconFor(type = effectiveType)
        }
    } else {
        // No dependency yet or invalid - use default icon as placeholder
        Timber.d("Using default icon for attachment ${attachment.target} (dependency: ${dependency?.id})")
        defaultIconFor(type = effectiveType)
    }

    // Only link‐types get a title when we have valid dependency
    val title = if (hasValidDependency && effectiveType == Chat.Message.Attachment.Type.Link) {
        fieldParser.getObjectName(objectWrapper = dependency)
    } else null

    return AttachmentPreview(
        type = previewType,
        objectIcon = icon,
        title = title
    )
}