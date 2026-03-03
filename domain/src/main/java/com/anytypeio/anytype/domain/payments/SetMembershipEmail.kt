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
        if (params.isFromOnboarding) {
            // Onboarding path always subscribes to newsletter;
            // params.subscribeToNewsletter is intentionally ignored here.
            repo.membershipSubscribeToUpdates(params.email)
        } else {
            val command = Command.Membership.GetVerificationEmail(
                email = params.email,
                subscribeToNewsletter = params.subscribeToNewsletter,
                isFromOnboarding = params.isFromOnboarding
            )
            repo.membershipGetVerificationEmail(command)
        }
    }

    data class Params(
        val email: String,
        val subscribeToNewsletter: Boolean,
        val isFromOnboarding: Boolean = false
    )
}