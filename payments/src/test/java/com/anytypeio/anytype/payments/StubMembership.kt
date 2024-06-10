package com.anytypeio.anytype.payments

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_models.membership.NameServiceNameType
import kotlin.random.Random
import net.bytebuddy.utility.RandomString

fun StubMembership(
    tier: Int = Random.nextInt(),
    status: Membership.Status = Membership.Status.STATUS_ACTIVE,
    dateEnds: Long = 432331231L,
    paymentMethod: MembershipPaymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
    anyName: String = "anyName-${RandomString.make()}",
    dateStarted: Long = Random.nextLong()
): Membership {
    return Membership(
        tier = tier,
        membershipStatusModel = status,
        dateEnds = dateEnds,
        paymentMethod = paymentMethod,
        userEmail = "",
        nameServiceName = anyName,
        nameServiceType = NameServiceNameType.ANY_NAME,
        subscribeToNewsletter = false,
        isAutoRenew = false,
        dateStarted = dateStarted
    )
}

fun StubMembershipTierData(
    id: Int = Random.nextInt(),
    name: String = "name-${RandomString.make()}",
    description: String = "description-${RandomString.make()}",
    isTest: Boolean = false,
    periodType: MembershipPeriodType = MembershipPeriodType.PERIOD_TYPE_YEARS,
    periodValue: Int = 0,
    priceStripeUsdCents: Int = 0,
    anyNamesCountIncluded: Int = Random.nextInt(),
    anyNameMinLength: Int = Random.nextInt(),
    features: List<String> = emptyList(),
    colorStr: String = "colorStr-${RandomString.make()}",
    stripeProductId: String? = null,
    stripeManageUrl: String? = null,
    iosProductId: String? = null,
    iosManageUrl: String? = null,
    androidProductId: String? = null,
    androidManageUrl: String? = null
): MembershipTierData = MembershipTierData(
    id = id,
    name = name,
    description = description,
    isTest = isTest,
    periodType = periodType,
    periodValue = periodValue,
    priceStripeUsdCents = priceStripeUsdCents,
    anyNamesCountIncluded = anyNamesCountIncluded,
    anyNameMinLength = anyNameMinLength,
    features = features,
    colorStr = colorStr,
    stripeProductId = stripeProductId,
    stripeManageUrl = stripeManageUrl,
    iosProductId = iosProductId,
    iosManageUrl = iosManageUrl,
    androidProductId = androidProductId,
    androidManageUrl = androidManageUrl
)