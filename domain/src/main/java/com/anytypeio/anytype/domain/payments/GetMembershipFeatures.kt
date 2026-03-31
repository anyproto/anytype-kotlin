package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.membership.MembershipFeatures
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetMembershipFeatures @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, MembershipFeatures>(dispatchers.io) {

    override suspend fun doWork(params: Unit): MembershipFeatures {
        return repo.membershipV2GetFeatures()
    }
}
