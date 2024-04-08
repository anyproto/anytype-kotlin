package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteView
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSpaceInviteView @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetSpaceInviteView.Params, SpaceInviteView>(dispatchers.io) {

    override suspend fun doWork(params: Params): SpaceInviteView {
        return repo.getSpaceInviteView(
            inviteFileKey = params.inviteFileKey,
            inviteContentId = params.inviteContentId
        )
    }

    data class Params(
        val inviteContentId: String,
        val inviteFileKey: String
    )
}