package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.flow.Flow

interface PersonalFavoritesRepository {

    suspend fun add(space: SpaceId, target: Id)

    suspend fun remove(space: SpaceId, target: Id)

    /**
     * Reorder the full favorites list for [space] to [orderedTargets].
     * Caller owns the complete new order; repository does not diff.
     */
    suspend fun reorder(space: SpaceId, orderedTargets: List<Id>)

    /**
     * Emits the current user's favorites in [space] as an ordered list of object IDs.
     * Must survive add/remove/reorder emitted by the middleware.
     */
    fun observe(space: SpaceId): Flow<List<Id>>
}
