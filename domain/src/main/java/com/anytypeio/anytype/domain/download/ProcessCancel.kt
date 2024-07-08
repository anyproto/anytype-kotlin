package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ProcessCancel @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ProcessCancel.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.ProcessCancel(
            processId = params.processId
        )
        return repository.processCancel(command)
    }

    data class Params(
        val processId: String
    )
}