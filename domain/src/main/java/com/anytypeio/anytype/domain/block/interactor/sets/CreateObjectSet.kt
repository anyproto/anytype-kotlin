package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateObjectSet(
    private val repo: BlockRepository
) : BaseUseCase<CreateObjectSet.Response, CreateObjectSet.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createSet(
            context = params.ctx,
            target = params.target,
            position = params.position,
            objectType = params.type
        )
    }

    data class Params(
        val ctx: Id,
        val target: Id? = null,
        val position: Position? = null,
        val type: Id? = null
    )

    /**
     * @property [target] id of the new set
     * @property [block] optional id of the link block pointing to the new set.
     */
    data class Response(
        val target: Id,
        val block: Id?,
        val payload: Payload
    )
}