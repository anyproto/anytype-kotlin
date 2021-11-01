package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DebugLocalStore(private val repo: BlockRepository) : BaseUseCase<String, DebugLocalStore.Params>() {

    override suspend fun run(params: Params): Either<Throwable, String> = safe {
        repo.debugLocalStore(path = params.path)
    }

    class Params(val path: String)
}