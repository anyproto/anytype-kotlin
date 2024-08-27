package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DebugAccountSelectTrace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DebugAccountSelectTrace.Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        return repo.debugAccountSelectTrace(params.dir)
    }

    data class Params(val dir: String)
}