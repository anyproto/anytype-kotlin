package com.anytypeio.anytype.presentation.membership.models

import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.Membership.Status
import com.anytypeio.anytype.core_models.membership.MembershipTierData

data class MembershipStatus(
    val activeTier: TierId?,
    val status: Status,
    val dateEnds: Long,
    val paymentMethod: MembershipPaymentMethod,
    val anyName: String,
    val tiers: List<MembershipTierData>
)

@JvmInline
value class TierId(val value: Int)
