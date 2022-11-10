package com.anytypeio.anytype.middleware

import anytype.Rpc.Unsplash.Download
import anytype.Rpc.Unsplash.Search
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.data.auth.repo.unsplash.UnsplashRemote
import com.anytypeio.anytype.middleware.interactor.MiddlewareProtobufLogger
import com.anytypeio.anytype.middleware.mappers.core
import com.anytypeio.anytype.middleware.service.MiddlewareService
import javax.inject.Inject

class UnsplashMiddleware @Inject constructor(
    private val service: MiddlewareService,
    private val logger: MiddlewareProtobufLogger
) : UnsplashRemote {

    override fun search(query: String, limit: Int): List<UnsplashImage> {
        val request = Search.Request(
            query = query,
            limit = limit
        ).also { logger.logRequest(it) }
        val response = service.unsplashSearch(request = request).also { logger.logResponse(it) }
        return response.pictures.map { p -> p.core() }
    }

    override fun download(id: Id): Hash {
        val request = Download.Request(pictureId = id).also { logger.logRequest(it) }
        val response = service.unsplashDownload(request = request).also { logger.logResponse(it) }
        return response.hash
    }
}