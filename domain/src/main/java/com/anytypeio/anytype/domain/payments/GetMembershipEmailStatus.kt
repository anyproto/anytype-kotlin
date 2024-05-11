package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.membership.EmailVerificationStatus
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetMembershipEmailStatus @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<Unit, EmailVerificationStatus>(dispatchers.io) {

    override suspend fun doWork(params: Unit): EmailVerificationStatus {
        return repo.membershipGetVerificationEmailStatus()
    }
}