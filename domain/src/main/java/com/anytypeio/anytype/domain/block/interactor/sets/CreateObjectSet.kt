package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateObjectSet(
    private val repo: BlockRepository
) : BaseUseCase<CreateObjectSet.Response, CreateObjectSet.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createSet(space = params.space, objectType = params.type)
    }

    data class Params(
        val space: Id,
        val type: Id? = null
    )

    /**
     * @property [target] id of the new set
     */
    data class Response(
        val target: Id,
        val payload: Payload
    )
}