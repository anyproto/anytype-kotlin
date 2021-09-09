package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding an object to favorite list.
 */
class AddToFavorite(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddToFavorite.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setIsFavorite(ctx = params.target, isFavorite = true)
    }

    /**
     * @property [target] id of the object we need to add to favorites.
     */
    class Params(val target: Id)
}