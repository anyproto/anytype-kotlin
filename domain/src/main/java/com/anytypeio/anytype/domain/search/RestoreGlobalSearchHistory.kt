package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class RestoreGlobalSearchHistory @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RestoreGlobalSearchHistory.Params, RestoreGlobalSearchHistory.Response>(
    dispatchers.io
) {

    override suspend fun doWork(params: Params): Response {
        return Response(
            globalSearchHistory = settings.setGlobalSearchHistory(
                space = params.spaceId
            )
        )
    }

    data class Params(
        val spaceId: SpaceId
    )

    data class Response(
        val globalSearchHistory: GlobalSearchHistory?
    )
}