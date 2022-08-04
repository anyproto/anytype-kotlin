package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DebugSync(private val repo: BlockRepository) : BaseUseCase<String, Unit>() {

    override suspend fun run(params: Unit): Either<Throwable, String> = safe {
        repo.debugSync()
    }
}