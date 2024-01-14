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
    val fileContent = fileBlock.content as? Block.Content.File
    val targetObjectId = fileContent?.targetObjectId
    return if (
        fileContent != null
        && fileContent.state == Block.Content.File.State.DONE
        && !targetObjectId.isNullOrBlank()
    ) {
        when (fileContent.type) {
            Block.Content.File.Type.IMAGE -> {
                if (isOriginalImage) {
                    original(targetObjectId)
                } else if (isThumbnail) {
                    thumbnail(targetObjectId)
                } else {
                    image(targetObjectId)
                }
            }
            Block.Content.File.Type.VIDEO -> video(targetObjectId)
            Block.Content.File.Type.FILE,
            Block.Content.File.Type.PDF,
            Block.Content.File.Type.AUDIO -> file(targetObjectId)
            Block.Content.File.Type.NONE -> null
            null -> null
        }
    } else {
        null
    }
}