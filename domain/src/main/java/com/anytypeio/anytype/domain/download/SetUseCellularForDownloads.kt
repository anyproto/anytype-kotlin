package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetUseCellularForDownloads @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Boolean, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Boolean) {
        settings.setUseCellularForDownloads(params)
    }
}
