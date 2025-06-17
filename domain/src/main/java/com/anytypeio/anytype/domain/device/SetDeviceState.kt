package com.anytypeio.anytype.domain.device

import com.anytypeio.anytype.core_models.DeviceState
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetDeviceState @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<SetDeviceState.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.setDeviceState(params.deviceState)
    }

    data class Params(val deviceState: DeviceState)
} 