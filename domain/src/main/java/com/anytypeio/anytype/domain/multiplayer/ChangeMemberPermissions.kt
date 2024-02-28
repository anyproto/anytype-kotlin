package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.ParticipantPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ChangeMemberPermissions @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ChangeMemberPermissions.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.changeMemberPermissions(
            space = params.space,
            identity = params.identity,
            permission = params.permission
        )
    }

    data class Params(
        val space: SpaceId,
        val identity: Id,
        val permission: ParticipantPermissions
    )
}