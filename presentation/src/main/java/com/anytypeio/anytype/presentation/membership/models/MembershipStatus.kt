package com.anytypeio.anytype.presentation.membership.models

import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipStatusModel
import com.anytypeio.anytype.core_models.membership.MembershipTierData

sealed class MembershipStatus {
    abstract val tiers: List<MembershipTierData>
    data class Unknown(
        override val tiers: List<MembershipTierData>
    ) : MembershipStatus()
    data class Pending(
        override val tiers: List<MembershipTierData>
    ) : MembershipStatus()
    data class Finalization(
        override val tiers: List<MembershipTierData>
    ) : MembershipStatus()
    data class Active(
        val activeTier: MembershipTierData,
        val status: MembershipStatusModel,
        val dateEnds: Long,
        val paymentMethod: MembershipPaymentMethod,
        val anyName: String,
        override val tiers: List<MembershipTierData>
    ) : MembershipStatus()
}

@JvmInline
value class TierId(val value: Int)
