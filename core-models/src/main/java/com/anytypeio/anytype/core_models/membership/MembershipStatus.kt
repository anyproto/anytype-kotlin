package com.anytypeio.anytype.core_models.membership

import com.anytypeio.anytype.core_models.membership.Membership.Status

data class MembershipStatus(
    val activeTier: TierId,
    val status: Status,
    val paymentMethod: MembershipPaymentMethod,
    val anyName: String,
    val tiers: List<MembershipTierData>,
    val dateEnds: Long,
    val formattedDateEnds: String,
    val userEmail: String = ""
)

@JvmInline
value class TierId(val value: Int)
