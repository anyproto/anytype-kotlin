package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for checking whether an object is in the list of favorite objects on dashboard.
 */
class CheckIsFavorite(
    private val repo: BlockRepository
) : BaseUseCase<Boolean, CheckIsFavorite.Params>() {

    override suspend fun run(params: Params) = safe {
        val config = repo.getConfig()
        val info = repo.getObjectInfoWithLinks(config.home)
        info.links.outbound.any { it.id == params.target }
    }

    /**
     * @property [target] id of the object (smart block).
     */
    class Params(val target: Id)
}