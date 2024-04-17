package com.anytypeio.anytype.domain.payments

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.membership.GetPaymentUrlResponse
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetMembershipPaymentUrl @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<GetMembershipPaymentUrl.Params, GetPaymentUrlResponse>(dispatchers.io) {

    override suspend fun doWork(params: Params): GetPaymentUrlResponse {
        val command = Command.Membership.GetPaymentUrl(
            tier = params.tierId,
            name = params.name,
            nameType = params.nameType,
            paymentMethod = params.paymentMethod
        )
        return repo.membershipGetPaymentUrl(command)
    }

    data class Params(
        val tierId: Int,
        val name: String,
        val nameType: NameServiceNameType = NameServiceNameType.ANY_NAME,
        val paymentMethod: MembershipPaymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
    )
}