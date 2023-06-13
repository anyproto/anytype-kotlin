package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetupMobileUseCaseSkip(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Unit, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Unit) {
        repo.importUseCaseSkip()
    }

}