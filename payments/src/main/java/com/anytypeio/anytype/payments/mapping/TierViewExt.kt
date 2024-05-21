package com.anytypeio.anytype.payments.mapping

import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.payments.constants.TiersConstants.ERROR_PRODUCT_NOT_FOUND
import com.anytypeio.anytype.payments.constants.TiersConstants.ERROR_PRODUCT_PRICE
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId

private fun MembershipStatus.isTierActive(tierId: Int): Boolean {
    return when (this.status) {
        Membership.Status.STATUS_ACTIVE -> activeTier.value == tierId
        else -> false
    }
}

private fun MembershipTierData.isActiveTierPurchasedOnAndroid(activePaymentMethod: MembershipPaymentMethod): Boolean {
    val androidProductId = this.androidProductId
    return when (activePaymentMethod) {
        MembershipPaymentMethod.METHOD_INAPP_GOOGLE -> return !androidProductId.isNullOrBlank()
        else -> false
    }
}

fun MembershipTierData.toView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState
): TierView {
    val tierId = TierId(id)
    val isActive = membershipStatus.isTierActive(id)
    val emailState = getTierEmail(isActive, membershipStatus.userEmail)
    val tierName = name
    val tierDescription = description
    return TierView(
        id = tierId,
        title = tierName,
        subtitle = tierDescription,
        conditionInfo = getConditionInfo(
            isActive = isActive,
            billingClientState = billingClientState,
            membershipStatus = membershipStatus
        ),
        isActive = isActive,
        features = features,
        membershipAnyName = getAnyName(
            isActive = isActive,
            billingClientState = billingClientState,
            membershipStatus = membershipStatus
        ),
        buttonState = toButtonView(
            isActive = isActive,
            billingPurchaseState = billingPurchaseState,
            membershipStatus = membershipStatus
        ),
        email = emailState,
        color = colorStr,
        urlInfo = androidManageUrl
    )
}

fun MembershipTierData.toPreviewView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
): TierPreviewView {
    val tierId = TierId(id)
    val isActive = membershipStatus.isTierActive(id)
    val tierName = name
    val tierDescription = description
    return TierPreviewView(
        id = tierId,
        title = tierName,
        subtitle = tierDescription,
        conditionInfo = getConditionInfo(
            isActive = isActive,
            billingClientState = billingClientState,
            membershipStatus = membershipStatus
        ),
        isActive = isActive,
        color = colorStr
    )
}

private fun MembershipTierData.toButtonView(
    isActive: Boolean,
    billingPurchaseState: BillingPurchaseState,
    membershipStatus: MembershipStatus
): TierButton {
    val androidProductId = this.androidProductId
    val androidInfoUrl = this.androidManageUrl
    return if (isActive) {
        val wasPurchasedOnAndroid = isActiveTierPurchasedOnAndroid(membershipStatus.paymentMethod)
        if (!wasPurchasedOnAndroid) {
            if (id == EXPLORER_ID) {
                if (membershipStatus.userEmail.isBlank()) {
                    TierButton.Submit.Enabled
                } else {
                    TierButton.ChangeEmail
                }
            } else {
                when (membershipStatus.paymentMethod) {
                    MembershipPaymentMethod.METHOD_NONE,
                    MembershipPaymentMethod.METHOD_CRYPTO -> TierButton.Manage.External.Disabled
                    MembershipPaymentMethod.METHOD_STRIPE -> TierButton.Manage.External.Enabled(
                        stripeManageUrl
                    )
                    MembershipPaymentMethod.METHOD_INAPP_APPLE -> TierButton.Manage.External.Enabled(
                        iosManageUrl
                    )
                    MembershipPaymentMethod.METHOD_INAPP_GOOGLE -> TierButton.Manage.External.Enabled(
                        androidInfoUrl
                    )
                }
            }
        } else {
            if (billingPurchaseState is BillingPurchaseState.HasPurchases) {
                TierButton.Manage.Android.Enabled(androidProductId)
            } else {
                TierButton.Manage.Android.Disabled
            }
        }
    } else {
        if (androidProductId == null) {
            if (androidInfoUrl == null) {
                TierButton.Info.Disabled
            } else {
                TierButton.Info.Enabled(androidInfoUrl)
            }
        } else {
            if (membershipStatus.anyName.isBlank()) {
                TierButton.Pay.Disabled
            } else {
                TierButton.Pay.Enabled
            }
        }
    }
}

private fun MembershipTierData.getAnyName(
    isActive: Boolean,
    billingClientState: BillingClientState,
    membershipStatus: MembershipStatus
): TierAnyName {
    if (isActive) {
        return TierAnyName.Hidden
    } else {
        if (androidProductId == null) {
            return TierAnyName.Hidden
        } else {
            if (membershipStatus.status == Membership.Status.STATUS_PENDING ||
                membershipStatus.status == Membership.Status.STATUS_PENDING_FINALIZATION
            ) {
                return TierAnyName.Hidden
            }

            if (billingClientState is BillingClientState.Connected) {
                val product =
                    billingClientState.productDetails.find { it.productId == androidProductId }
                if (product == null) {
                    return TierAnyName.Visible.Disabled
                } else {
                    if (product.billingPriceInfo() == null) {
                        return TierAnyName.Visible.Disabled
                    } else {
                        if (membershipStatus.anyName.isBlank()) {
                            return TierAnyName.Visible.Enter
                        } else {
                            return TierAnyName.Visible.Purchased(membershipStatus.anyName)
                        }
                    }
                }
            } else {
                return TierAnyName.Visible.Disabled
            }
        }
    }
}

private fun MembershipTierData.getConditionInfo(
    isActive: Boolean,
    billingClientState: BillingClientState,
    membershipStatus: MembershipStatus
): TierConditionInfo {
    return if (isActive) {
        createConditionInfoForCurrentTier(
            membershipValidUntil = membershipStatus.dateEnds,
            paymentMethod = membershipStatus.paymentMethod
        )
    } else {
        if (androidProductId == null) {
            createConditionInfoForNonBillingTier()
        } else {
            createConditionInfoForBillingTier(billingClientState, membershipStatus)
        }
    }
}

private fun MembershipTierData.createConditionInfoForCurrentTier(
    membershipValidUntil: Long,
    paymentMethod: MembershipPaymentMethod
): TierConditionInfo {
    return TierConditionInfo.Visible.Valid(
        dateEnds = membershipValidUntil,
        payedBy = paymentMethod,
        period = convertToTierViewPeriod(this)
    )
}

private fun MembershipTierData.createConditionInfoForNonBillingTier(): TierConditionInfo {
    return if (priceStripeUsdCents == 0) {
        TierConditionInfo.Visible.Free(
            period = convertToTierViewPeriod(this)
        )
    } else {
        TierConditionInfo.Visible.Price(
            price = formatPriceInCents(priceStripeUsdCents),
            period = convertToTierViewPeriod(this)
        )
    }
}

private fun formatPriceInCents(priceInCents: Int): String {
    val dollars = priceInCents / 100
    return if (priceInCents % 100 == 0) {
        "$$dollars"
    } else {
        "$%.2f".format(dollars + (priceInCents % 100) / 100.0)
    }
}

private fun MembershipTierData.createConditionInfoForBillingTier(
    billingClientState: BillingClientState,
    membershipStatus: MembershipStatus
): TierConditionInfo {
    if (
        membershipStatus.status == Membership.Status.STATUS_PENDING ||
        membershipStatus.status == Membership.Status.STATUS_PENDING_FINALIZATION
    ) {
        return TierConditionInfo.Visible.Pending
    }
    return when (billingClientState) {
        BillingClientState.Loading -> {
            TierConditionInfo.Visible.LoadingBillingClient
        }

        is BillingClientState.Error -> {
            TierConditionInfo.Visible.Error(billingClientState.message)
        }

        is BillingClientState.Connected -> {
            val product =
                billingClientState.productDetails.find { it.productId == androidProductId }
            if (product == null) {
                TierConditionInfo.Visible.Error(ERROR_PRODUCT_NOT_FOUND)
            } else {
                val billingPriceInfo = product.billingPriceInfo()
                if (billingPriceInfo == null) {
                    TierConditionInfo.Visible.Error(ERROR_PRODUCT_PRICE)
                } else {
                    TierConditionInfo.Visible.PriceBilling(
                        price = billingPriceInfo
                    )
                }
            }
        }
    }
}

private fun ProductDetails.billingPriceInfo(): BillingPriceInfo? {
    val pricingPhase = subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)
    val formattedPrice = pricingPhase?.formattedPrice
    val periodType = pricingPhase?.billingPeriod?.parsePeriod()
    if (formattedPrice == null || periodType == null || formattedPrice.isBlank()) {
        return null
    }
    return BillingPriceInfo(
        formattedPrice = formattedPrice,
        period = periodType
    )
}

private fun convertToTierViewPeriod(tier: MembershipTierData): TierPeriod {
    return when (tier.periodType) {
        MembershipPeriodType.PERIOD_TYPE_UNKNOWN -> TierPeriod.Unknown
        MembershipPeriodType.PERIOD_TYPE_UNLIMITED -> TierPeriod.Unlimited
        MembershipPeriodType.PERIOD_TYPE_DAYS -> TierPeriod.Day(tier.periodValue)
        MembershipPeriodType.PERIOD_TYPE_WEEKS -> TierPeriod.Week(tier.periodValue)
        MembershipPeriodType.PERIOD_TYPE_MONTHS -> TierPeriod.Month(tier.periodValue)
        MembershipPeriodType.PERIOD_TYPE_YEARS -> TierPeriod.Year(tier.periodValue)
    }
}

private fun MembershipTierData.getTierEmail(isActive: Boolean, membershipEmail: String): TierEmail {
    if (isActive) {
        if (id == EXPLORER_ID && membershipEmail.isBlank()) {
            return TierEmail.Visible.Enter
        }
    }
    return TierEmail.Hidden
}