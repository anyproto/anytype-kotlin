package com.agileburo.anytype.domain.image

/**
 * @param id account id
 * @param blob image data represented as an array of bytes
 */
class AvatarBlob(
    val id: String,
    val blob: ByteArray
)