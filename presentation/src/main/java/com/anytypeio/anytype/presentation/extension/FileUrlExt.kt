package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

fun UrlBuilder.getUrlForFileBlock(
    fileBlock: Block?,
    isOriginalImage: Boolean = false,
    isThumbnail: Boolean = false
): Url? {
    if (fileBlock == null) return null
    val fileContent = fileBlock.content as? Block.Content.File ?: return null
    return getUrlForFileContent(fileContent, isOriginalImage, isThumbnail)
}

fun UrlBuilder.getUrlForFileContent(
    fileContent: Block.Content.File,
    isOriginalImage: Boolean = false,
    isThumbnail: Boolean = false
): Url? {
    val targetObjectId = fileContent.targetObjectId
    if (fileContent.state != Block.Content.File.State.DONE || targetObjectId.isNullOrBlank()) {
        return null
    }
    return getUrlBasedOnType(fileContent.type, targetObjectId, isOriginalImage, isThumbnail)
}

private fun UrlBuilder.getUrlBasedOnType(
    fileType: Block.Content.File.Type?,
    targetObjectId: String,
    isOriginalImage: Boolean,
    isThumbnail: Boolean
): Url? {
    return when (fileType) {
        Block.Content.File.Type.IMAGE -> {
            when {
                isOriginalImage -> original(targetObjectId)
                isThumbnail -> thumbnail(targetObjectId)
                else -> image(targetObjectId)
            }
        }

        Block.Content.File.Type.VIDEO -> video(targetObjectId)
        Block.Content.File.Type.FILE,
        Block.Content.File.Type.PDF,
        Block.Content.File.Type.AUDIO -> file(targetObjectId)

        Block.Content.File.Type.NONE, null -> null
    }
}
