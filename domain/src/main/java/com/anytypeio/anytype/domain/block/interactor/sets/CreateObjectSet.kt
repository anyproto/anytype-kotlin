package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.Payload

class CreateObjectSet(private val repo: BlockRepository) :
    BaseUseCase<CreateObjectSet.Response, CreateObjectSet.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createSet(
            context = params.context,
            target = params.target,
            position = params.position,
            objectType = params.objectType
        )
    }

    data class Params(
        val context: Id,
        val target: Id? = null,
        val position: Position = Position.BOTTOM,
        val objectType: Url? = null
    )

    data class Response(
        val block: Id,
        val target: Id,
        val payload: Payload
    )
}