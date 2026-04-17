package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class RemovePersonalFavorite @Inject constructor(
    private val repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RemovePersonalFavorite.Params, Unit>(dispatchers.io) {

    data class Params(
        val space: SpaceId,
        val target: Id
    )

    override suspend fun doWork(params: Params) =
        repo.remove(params.space, params.target)
}
