package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DebugSync(private val repo: BlockRepository) : ResultInteractor<Unit, String>() {

    override suspend fun doWork(params: Unit): String = repo.debugSync()
}