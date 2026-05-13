package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class AddPersonalFavorite @Inject constructor(
    private val repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddPersonalFavorite.Params, Payload>(dispatchers.io) {

    data class Params(
        val space: SpaceId,
        val target: Id
    )

    override suspend fun doWork(params: Params): Payload =
        repo.add(params.space, params.target)
}
