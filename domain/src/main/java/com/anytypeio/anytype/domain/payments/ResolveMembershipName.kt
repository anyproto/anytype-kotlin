package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ResolveMembershipName @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<ResolveMembershipName.Params, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Params): Boolean {
        val command = Command.Membership.ResolveName(
            name = params.name,
            nameType = params.nameType
        )
        return repo.membershipResolveName(command)
    }

    data class Params(
        val name: String,
        val nameType: NameServiceNameType = NameServiceNameType.ANY_NAME
    )
}