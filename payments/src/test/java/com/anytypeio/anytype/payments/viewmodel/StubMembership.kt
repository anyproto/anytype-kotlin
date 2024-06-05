package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId
import kotlin.random.Random
import net.bytebuddy.utility.RandomString

fun StubMembership(
    activeTier: TierId = TierId(value = Random.nextInt()),
    status: Membership.Status = Membership.Status.STATUS_UNKNOWN,
    dateEnds: Long = Random.nextLong(),
    paymentMethod: MembershipPaymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
    anyName: String = "anyName-${RandomString.make()}",
    tiers: List<MembershipTierData> = emptyList()
): MembershipStatus = MembershipStatus(
    activeTier = activeTier,
    status = status,
    dateEnds = dateEnds,
    paymentMethod = paymentMethod,
    anyName = anyName,
    tiers = tiers
)