package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetTiers @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<GetTiers.Params, List<MembershipTierData>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<MembershipTierData> {
        val command = Command.Membership.GetTiers(
            locale = params.locale,
            noCache = params.noCache
        )
        return repo.membershipGetTiers(command)
    }

    data class Params(
        val locale: String,
        val noCache: Boolean
    )
}