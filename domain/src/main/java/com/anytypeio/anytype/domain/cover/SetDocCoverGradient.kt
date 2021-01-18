package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload

class SetDocCoverGradient(private val repo: BlockRepository) : BaseUseCase<Payload, SetDocCoverGradient.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setDocumentCoverGradient(
            ctx = params.ctx,
            gradient = params.gradient
        )
    }

    class Params(
        val ctx: Id,
        val gradient: String
    )
}