package com.agileburo.anytype.domain.image

import com.agileburo.anytype.domain.auth.model.Image


interface ImageLoader {
    /**
     * @param id id of the image
     * @param size requested image size
     */
    suspend fun load(id: String, size: Image.Size): ByteArray
}