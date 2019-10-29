package com.agileburo.anytype.middleware.interactor

import com.agileburo.anytype.data.auth.model.ImageEntity
import com.agileburo.anytype.data.auth.other.ImageLoaderRemote
import com.agileburo.anytype.middleware.toMiddleware

class MiddlewareImageLoader(private val middleware: Middleware) : ImageLoaderRemote {

    override suspend fun load(
        id: String, size: ImageEntity.Size
    ): ByteArray = middleware.loadImage(id, size.toMiddleware())
}