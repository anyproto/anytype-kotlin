package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetFileDownloadLimit @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, FileDownloadLimit>(dispatchers.io) {

    override suspend fun doWork(params: Unit): FileDownloadLimit {
        return settings.getFileDownloadLimit()
    }
}
