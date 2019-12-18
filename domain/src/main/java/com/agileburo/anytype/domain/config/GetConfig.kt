package com.agileburo.anytype.domain.config

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository

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