package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ImportGetStartedUseCase(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<ImportGetStartedUseCase.Params, Command.ImportUseCase.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Command.ImportUseCase.Result {
        return repo.importGetStartedUseCase(params.space)
    }

    data class Params(val space: Id)
}