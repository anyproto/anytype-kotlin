package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDocCoverColor(private val repo: BlockRepository) : BaseUseCase<Payload, SetDocCoverColor.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setDocumentCoverColor(
            ctx = params.ctx,
            color = params.color
        )
    }

    class Params(
        val ctx: Id,
        val color: String
    )
}