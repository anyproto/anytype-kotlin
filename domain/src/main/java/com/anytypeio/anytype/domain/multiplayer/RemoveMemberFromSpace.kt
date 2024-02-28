package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class RemoveMemberFromSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<RemoveMemberFromSpace.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.removeMembersFromSpace(
            space = params.space,
            identities = params.identities
        )
    }

    /**
     * @property [identities] identities of members of a given space
     */
    data class Params(val space: SpaceId, val identities: List<Id>)
}