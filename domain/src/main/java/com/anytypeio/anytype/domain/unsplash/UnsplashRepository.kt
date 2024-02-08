package com.anytypeio.anytype.domain.unsplash

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface UnsplashRepository {
    fun search(query: String, limit: Int) : List<UnsplashImage>
    fun download(id: Id, space: SpaceId) : Hash
}