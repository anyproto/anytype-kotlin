package com.anytypeio.anytype.data.auth.repo.unsplash

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UnsplashImage
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface UnsplashRemote {
    fun search(query: String, limit: Int) : List<UnsplashImage>
    fun download(space: SpaceId, id: Id) : Hash
}