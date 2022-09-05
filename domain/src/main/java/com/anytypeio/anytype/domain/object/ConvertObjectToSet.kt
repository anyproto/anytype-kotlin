package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ConvertObjectToSet(
    private val repo: BlockRepository
) : BaseUseCase<String, ConvertObjectToSet.Params>() {

    override suspend fun run(params: Params): Either<Throwable, String> = safe {
        repo.objectToSet(
            ctx = params.ctx,
            source = params.sources
        )
    }

    data class Params(
        val ctx: Id,
        val sources: List<String>
    )
}