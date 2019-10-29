package com.agileburo.anytype.middleware

import anytype.Models
import com.agileburo.anytype.data.auth.model.ImageEntity

fun Models.Image.toEntity(): ImageEntity? {
    return if (id.isNullOrBlank())
        null
    else
        ImageEntity(
            id = id,
            sizes = sizesList.map { size -> size.toEntity() }
        )
}

fun ImageEntity.Size.toMiddleware(): Models.ImageSize = when (this) {
    ImageEntity.Size.SMALL -> Models.ImageSize.SMALL
    ImageEntity.Size.LARGE -> Models.ImageSize.LARGE
    ImageEntity.Size.THUMB -> Models.ImageSize.THUMB
}

fun Models.ImageSize.toEntity(): ImageEntity.Size = when (this) {
    Models.ImageSize.SMALL -> ImageEntity.Size.SMALL
    Models.ImageSize.LARGE -> ImageEntity.Size.LARGE
    Models.ImageSize.THUMB -> ImageEntity.Size.THUMB
    else -> throw IllegalStateException("Unexpected image size from middleware")
}