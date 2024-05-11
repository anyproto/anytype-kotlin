package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class VerifyMembershipEmailCode @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<VerifyMembershipEmailCode.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.Membership.VerifyEmailCode(
            code = params.code
        )
        repo.membershipVerifyEmailCode(command)
    }

    data class Params(
        val code: String
    )
}