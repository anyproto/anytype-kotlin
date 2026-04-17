package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePersonalFavorites @Inject constructor(
    private val repo: PersonalFavoritesRepository
) {
    data class Params(val space: SpaceId)

    fun build(params: Params): Flow<List<Id>> = repo.observe(params.space)
}
