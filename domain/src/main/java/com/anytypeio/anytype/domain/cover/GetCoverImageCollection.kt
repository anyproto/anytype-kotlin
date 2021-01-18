package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.domain.base.BaseUseCase

class GetCoverImageCollection(private val provider: CoverCollectionProvider) : BaseUseCase<List<CoverImage>, Unit>() {
    override suspend fun run(params: Unit) = safe {
        provider.provide()
    }
}