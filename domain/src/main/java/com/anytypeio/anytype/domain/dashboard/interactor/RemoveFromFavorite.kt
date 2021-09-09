package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class RemoveFromFavorite(
    private val repo: BlockRepository
) : BaseUseCase<Payload, RemoveFromFavorite.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setIsFavorite(ctx = params.target, isFavorite = false)
    }

    class Params(val target: Id)
}