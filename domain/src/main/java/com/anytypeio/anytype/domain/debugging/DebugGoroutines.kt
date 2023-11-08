package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DebugGoroutines @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DebugGoroutines.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.debugStackGoroutines(params.path)
    }

    data class Params(val path: String)
}