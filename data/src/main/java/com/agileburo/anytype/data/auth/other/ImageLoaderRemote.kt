package com.agileburo.anytype.data.auth.other

import com.agileburo.anytype.data.auth.model.ImageEntity

interface ImageLoaderRemote {
    suspend fun load(id: String, size: ImageEntity.Size): ByteArray
}