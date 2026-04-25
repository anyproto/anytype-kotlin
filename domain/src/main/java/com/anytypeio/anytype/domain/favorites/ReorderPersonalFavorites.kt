package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class ReorderPersonalFavorites @Inject constructor(
    private val repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ReorderPersonalFavorites.Params, List<Payload>>(dispatchers.io) {

    data class Params(
        val space: SpaceId,
        val order: List<Id>
    )

    override suspend fun doWork(params: Params): List<Payload> =
        repo.reorder(params.space, params.order)
}
