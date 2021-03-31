package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class RemoveDocCover(private val repo: BlockRepository) : BaseUseCase<Payload, RemoveDocCover.Params>() {
    override suspend fun run(params: Params) = safe {
        repo.removeDocumentCover(ctx = params.ctx)
    }

    class Params(val ctx: Id)
}