package com.anytypeio.anytype.payments

import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import kotlin.random.Random
import net.bytebuddy.utility.RandomString


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