package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetMembershipStatus @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<GetMembershipStatus.Params, Membership?>(dispatchers.io) {

    override suspend fun doWork(params: Params): Membership? {
        val command = Command.Membership.GetStatus(
            noCache = params.noCache
        )
        return repo.membershipStatus(command)
    }

    data class Params(
        val noCache: Boolean
    )
}