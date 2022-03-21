package com.anytypeio.anytype.middleware

import anytype.Rpc
import anytype.Rpc.UnsplashSearch
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.data.auth.repo.unsplash.UnsplashRemote
import com.anytypeio.anytype.middleware.log.logRequest
import com.anytypeio.anytype.middleware.log.logResponse
import com.anytypeio.anytype.middleware.mappers.core
import com.anytypeio.anytype.middleware.service.MiddlewareService

class UnsplashMiddleware(
    private val service: MiddlewareService
) : UnsplashRemote {

    override fun search(query: String, limit: Int): List<UnsplashImage> {
        val request = UnsplashSearch.Request(
            query = query,
            limit = limit
        ).also { it.logRequest() }
        val response = service.unsplashSearch(request = request).also { it.logResponse() }
        return response.pictures.map { p -> p.core() }
    }

    override fun download(id: Id): Hash {
        val request = Rpc.UnsplashDownload.Request(pictureId = id).also { it.logRequest() }
        val response = service.unsplashDownload(request = request).also { it.logResponse() }
        return response.hash
    }
}