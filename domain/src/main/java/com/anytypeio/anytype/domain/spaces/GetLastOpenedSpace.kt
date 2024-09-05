package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetLastOpenedSpace @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Unit, SpaceId?>(dispatchers.io) {

    override suspend fun doWork(params: Unit): SpaceId? {
        return repo.getCurrentSpace()
    }
}