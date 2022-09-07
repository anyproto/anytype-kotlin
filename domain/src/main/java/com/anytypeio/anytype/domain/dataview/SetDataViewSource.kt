package com.anytypeio.anytype.domain.dataview

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDataViewSource(private val repo: BlockRepository) :
    BaseUseCase<Payload, SetDataViewSource.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.blockDataViewSetSource(
            ctx = params.ctx,
            block = DEFAULT_DATA_VIEW_BLOCK_ID,
            sources = params.sources
        )
    }

    companion object {
        const val DEFAULT_DATA_VIEW_BLOCK_ID = "dataview"
    }

    data class Params(
        val ctx: Id,
        val sources: List<Id>
    )
}