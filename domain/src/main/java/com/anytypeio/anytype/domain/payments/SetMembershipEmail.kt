package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetMembershipEmail @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<SetMembershipEmail.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.Membership.GetVerificationEmail(
            email = params.email,
            subscribeToNewsletter = params.subscribeToNewsletter
        )
        repo.membershipGetVerificationEmail(command)
    }

    data class Params(
        val email: String,
        val subscribeToNewsletter: Boolean
    )
}