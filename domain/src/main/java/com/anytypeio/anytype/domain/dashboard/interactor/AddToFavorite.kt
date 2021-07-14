package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding an object to favorite list.
 */
class AddToFavorite(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Id, Payload>, AddToFavorite.Params>() {

    override suspend fun run(params: Params) = safe {
        val config = repo.getConfig()
        repo.create(
            Command.Create(
                context = config.home,
                prototype = Block.Prototype.Link(target = params.target),
                target = EMPTY_TARGET_ID,
                position = Position.BOTTOM
            )
        )
    }

    /**
     * @property [target] id of the object we need to add to favorites.
     */
    class Params(
        val target: Id
    )

    companion object {
        const val EMPTY_TARGET_ID = ""
    }
}