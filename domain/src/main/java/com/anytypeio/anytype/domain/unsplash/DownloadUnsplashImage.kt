package com.anytypeio.anytype.domain.unsplash

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase

class DownloadUnsplashImage(
    private val repo: UnsplashRepository
) : BaseUseCase<Hash, DownloadUnsplashImage.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.download(
            id = params.picture,
            space = params.space
        )
    }

    class Params(val picture: Id, val space: SpaceId)
}