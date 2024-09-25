package com.anytypeio.anytype.domain.all_content

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class RestoreAllContentState @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RestoreAllContentState.Params, RestoreAllContentState.Response>(
    dispatchers.io
) {

    override suspend fun doWork(params: Params): Response {
        //todo: implement
        return Response(null)
    }

    data class Params(
        val spaceId: SpaceId
    )

    data class Response(
        val state: DVSort?
    )
}