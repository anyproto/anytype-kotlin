package com.anytypeio.anytype.data.auth.repo.unsplash

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository

class UnsplashDataRepository(
    private val remote: UnsplashRemote
) : UnsplashRepository {
    override fun search(
        query: String,
        limit: Int
    ): List<UnsplashImage> = remote.search(
        query = query,
        limit = limit
    )
    override fun download(id: Id) : Hash = remote.download(id = id)
}