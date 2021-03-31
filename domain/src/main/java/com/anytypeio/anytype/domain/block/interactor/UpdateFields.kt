package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

class UpdateFields(private val repo: BlockRepository) :
    BaseUseCase<Payload, UpdateFields.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setFields(
            command = Command.SetFields(
                context = params.context,
                fields = params.fields
            )
        )
    }

    data class Params(
        val context: Id,
        val fields: List<Pair<Id, Block.Fields>>
    )
}