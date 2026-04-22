package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface PersonalFavoritesRepository {

    /**
     * Add [target] as a personal favorite in [space]. Inserts at the top of the list
     * (matches desktop `BlockPosition.InnerFirst`).
     */
    suspend fun add(space: SpaceId, target: Id)

    /** Remove [target] from the user's personal favorites in [space]. No-op if absent. */
    suspend fun remove(space: SpaceId, target: Id)

    /**
     * Reorder the full favorites list for [space] to [orderedTargets].
     * Caller owns the complete new order; repository does not diff.
     */
    suspend fun reorder(space: SpaceId, orderedTargets: List<Id>)
}
