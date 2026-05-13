package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface PersonalFavoritesRepository {

    /**
     * Add [target] as a personal favorite in [space]. Inserts at the top of the list
     * (matches desktop `BlockPosition.InnerFirst`).
     *
     * Returns the [Payload] from the underlying RPC response so the caller can
     * dispatch it into [com.anytypeio.anytype.presentation.common.PayloadDelegator] —
     * the personal-widgets observer subscribes there to keep local state in
     * sync with locally-initiated mutations (the middleware does not push a
     * separate event for changes it has already returned in the response).
     */
    suspend fun add(space: SpaceId, target: Id): Payload

    /**
     * Remove [target] from the user's personal favorites in [space].
     * Returns `null` when [target] is not present (no RPC fired); otherwise the
     * [Payload] from the RPC response (see [add] for why this matters).
     */
    suspend fun remove(space: SpaceId, target: Id): Payload?

    /**
     * Reorder the full favorites list for [space] to [orderedTargets].
     * Caller owns the complete new order; repository does not diff.
     * Returns one [Payload] per move RPC (zero when the list is too short to
     * reorder). Caller should dispatch each Payload (see [add]).
     */
    suspend fun reorder(space: SpaceId, orderedTargets: List<Id>): List<Payload>
}
