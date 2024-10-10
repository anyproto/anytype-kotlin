package com.anytypeio.anytype.domain.all_content

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class UpdateAllContentState @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<UpdateAllContentState.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        settings.setAllContentSort(
            space = params.spaceId,
            sort = params.sort,
            isAsc = params.isAsc
        )
    }

    data class Params(
        val spaceId: SpaceId,
        val sort: Id,
        val isAsc: Boolean
    )
}