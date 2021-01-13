package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload

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