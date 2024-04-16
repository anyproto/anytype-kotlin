package com.anytypeio.anytype.presentation.membership.models

import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel
import com.anytypeio.anytype.core_models.membership.MembershipTierData

sealed class MembershipStatus {
    object Unknown : MembershipStatus()
    object Pending : MembershipStatus()
    object Finalization : MembershipStatus()
    data class Active(
        val tier: MembershipTierData,
        val status: MembershipStatusModel,
        val dateEnds: Long,
        val paymentMethod: MembershipPaymentMethod,
        val anyName: String
    ) : MembershipStatus()
}

@JvmInline
value class TierId(val value: Int)
