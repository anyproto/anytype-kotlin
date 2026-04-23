package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class ReorderPersonalFavorites @Inject constructor(
    private val repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ReorderPersonalFavorites.Params, Unit>(dispatchers.io) {

    data class Params(
        val space: SpaceId,
        val order: List<Id>
    )

    override suspend fun doWork(params: Params) =
        repo.reorder(params.space, params.order)
}
