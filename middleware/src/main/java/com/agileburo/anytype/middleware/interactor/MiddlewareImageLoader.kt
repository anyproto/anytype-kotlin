package com.agileburo.anytype.middleware.interactor

import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.data.auth.other.ImageLoaderRemote

class MiddlewareImageLoader(private val middleware: Middleware) : ImageLoaderRemote {

    override suspend fun load(
        id: String, size: ImageEntity.Size
    ): ByteArray {
        TODO()
        //middleware.loadImage(id, size.toMiddleware())
    }
}