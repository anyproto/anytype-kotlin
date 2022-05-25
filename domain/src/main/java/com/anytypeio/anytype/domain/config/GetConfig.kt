package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetConfig(
    private val repo: BlockRepository
) : BaseUseCase<Config, Unit>() {

    override suspend fun run(params: Unit) = try {
        repo.getConfig().let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }
}