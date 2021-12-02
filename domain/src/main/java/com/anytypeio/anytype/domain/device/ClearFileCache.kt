package com.anytypeio.anytype.domain.device

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ClearFileCache(
    private val repo: BlockRepository
): Interactor<BaseUseCase.None>() {
    override suspend fun run(params: BaseUseCase.None) { repo.clearFileCache() }
}