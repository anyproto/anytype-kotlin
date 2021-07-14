package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

@Deprecated("Currently does not work.")
class RemoveFromFavorite(
    private val repo: BlockRepository
) : BaseUseCase<Payload, RemoveFromFavorite.Params>() {

    override suspend fun run(params: Params) = safe {
        val config = repo.getConfig()
        val info = repo.getObjectInfoWithLinks(config.home)
        val links = info.links.outbound.filter { it.id == params.target }
        repo.unlink(
            Command.Unlink(
                context = config.home,
                targets = links.map { it.id }
            )
        )
    }

    class Params(val target: Id)
}