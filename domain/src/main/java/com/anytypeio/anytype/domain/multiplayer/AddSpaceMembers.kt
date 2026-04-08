package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class AddSpaceMembers @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddSpaceMembers.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.addSpaceMembers(
            space = params.space,
            identities = params.identities,
            permissions = params.permissions
        )
    }

    /**
     * @property [identities] identities of members to add to a given space
     * @property [permissions] permissions to assign to the added members
     */
    data class Params(
        val space: SpaceId,
        val identities: List<Id>,
        val permissions: SpaceMemberPermissions = SpaceMemberPermissions.READER
    )
}
