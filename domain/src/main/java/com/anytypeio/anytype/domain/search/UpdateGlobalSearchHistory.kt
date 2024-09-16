package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class UpdateGlobalSearchHistory @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<UpdateGlobalSearchHistory.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        settings.setGlobalSearchHistory(
            search = GlobalSearchHistory(
                query = params.query,
                relatedObject = params.relatedObjectId
            ),
            space = params.spaceId
        )
    }

    data class Params(
        val spaceId: SpaceId,
        val query: String,
        val relatedObjectId: Id?
    )
}