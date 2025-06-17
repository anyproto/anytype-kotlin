package com.anytypeio.anytype.domain.device

import com.anytypeio.anytype.core_models.AppState
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetAppState @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<SetAppState.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.setAppState(params.state)
    }

    data class Params(val state: AppState)
} 