package com.anytypeio.anytype.payments.mapping

import com.android.billingclient.api.ProductDetails
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

fun MembershipTierData.toView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState
): TierView {
    val tierId = TierId(id)
    val isActive = membershipStatus.isTierActive(id)
    val emailState = getTierEmail(isActive, membershipStatus.userEmail)
    return TierView(
        id = tierId,
        title = tierId.getTierTitle(),
        subtitle = tierId.getTierSubtitle(),
        conditionInfo = getConditionInfo(
            isActive = isActive,
            billingClientState = billingClientState,
            paymentMethod = membershipStatus.paymentMethod
        ),
        isActive = isActive,
        features = features,
        membershipAnyName = getAnyName(isActive, billingClientState),
        buttonState = toButtonView(
            isActive = isActive,
            billingPurchaseState = billingPurchaseState,
            emailState = emailState
        ),
        email = emailState
    )
}

fun MembershipTierData.toPreviewView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
): TierPreviewView {
    val tierId = TierId(id)
    val isActive = membershipStatus.isTierActive(id)
    return TierPreviewView(
        id = tierId,
        title = tierId.getTierTitle(),
        subtitle = tierId.getTierSubtitle(),
        conditionInfo = getConditionInfo(
            isActive = isActive,
            billingClientState = billingClientState,
            paymentMethod = membershipStatus.paymentMethod
        ),
        isActive = isActive
    )
}

private fun MembershipTierData.toButtonView(
    isActive: Boolean,
    billingPurchaseState: BillingPurchaseState,
    emailState: TierEmail
): TierButton {
    val androidProductId = this.androidProductId
    return if (isActive) {
        if (androidProductId == null) {
            if (id == EXPLORER_ID) {
                when (emailState)  {
                    TierEmail.Hidden -> TierButton.Hidden
                    TierEmail.Visible.Enter -> TierButton.Submit.Disabled
                    is TierEmail.Visible.Error -> TierButton.Submit.Disabled
                    TierEmail.Visible.Validated -> TierButton.Submit.Enabled
                    TierEmail.Visible.Validating -> TierButton.Submit.Disabled
                }
            } else {
                //todo: add logic for other tiers
                TierButton.Info.Disabled
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
            TierButton.Info.Enabled("")
        } else {
            TierButton.Pay.Disabled
        }
    }
}

private fun MembershipTierData.getAnyName(
    isActive: Boolean,
    billingClientState: BillingClientState
): TierAnyName {
    return if (isActive) {
        TierAnyName.Hidden
    } else {
        if (androidProductId == null) {
            TierAnyName.Hidden
        } else {
            if (billingClientState is BillingClientState.Connected) {
                val product =
                    billingClientState.productDetails.find { it.productId == androidProductId }
                if (product == null) {
                    TierAnyName.Visible.Disabled
                } else {
                    if (product.billingPriceInfo() == null) {
                        TierAnyName.Visible.Disabled
                    } else {
                        TierAnyName.Visible.Enter
                    }
                }
            } else {
                TierAnyName.Visible.Disabled
            }
        }
    }
}

private fun MembershipTierData.getConditionInfo(
    isActive: Boolean,
    billingClientState: BillingClientState,
    paymentMethod: MembershipPaymentMethod
): TierConditionInfo {
    return if (isActive) {
        createConditionInfoForCurrentTier(paymentMethod)
    } else {
        if (androidProductId == null) {
            createConditionInfoForNonBillingTier()
        } else {
            createConditionInfoForBillingTier(billingClientState)
        }
    }
}

private fun MembershipTierData.createConditionInfoForCurrentTier(paymentMethod: MembershipPaymentMethod): TierConditionInfo {
    return if (priceStripeUsdCents == 0) {
        TierConditionInfo.Visible.Free(
            period = convertToTierViewPeriod(this)
        )
    } else {
        TierConditionInfo.Visible.Valid(
            period = convertToTierViewPeriod(this),
            payedBy = paymentMethod
        )
    }
}

private fun MembershipTierData.createConditionInfoForNonBillingTier(): TierConditionInfo {
    return if (priceStripeUsdCents == 0) {
        TierConditionInfo.Visible.Free(
            period = convertToTierViewPeriod(this)
        )
    } else {
        TierConditionInfo.Visible.Price(
            price = "$$priceStripeUsdCents",
            period = convertToTierViewPeriod(this)
        )
    }
}

private fun MembershipTierData.createConditionInfoForBillingTier(billingClientState: BillingClientState): TierConditionInfo {
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

private fun TierEmail.getButtonState(): TierButton {
    return when (this) {
        TierEmail.Hidden -> TierButton.Hidden
        TierEmail.Visible.Validated -> TierButton.Submit.Enabled
        TierEmail.Visible.Enter -> TierButton.Submit.Disabled
        is TierEmail.Visible.Error -> TierButton.Submit.Disabled
        TierEmail.Visible.Validating -> TierButton.Submit.Disabled
    }
}