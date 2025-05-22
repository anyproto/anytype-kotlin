package com.anytypeio.anytype.payments.mapping

import com.android.billingclient.api.ProductDetails
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod.METHOD_CRYPTO
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod.METHOD_INAPP_APPLE
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod.METHOD_INAPP_GOOGLE
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod.METHOD_NONE
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod.METHOD_STRIPE
import com.anytypeio.anytype.core_models.membership.MembershipPeriodType
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_models.membership.MembershipConstants
import com.anytypeio.anytype.core_models.membership.MembershipConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.core_models.membership.MembershipConstants.CO_CREATOR_ID
import com.anytypeio.anytype.core_models.membership.MembershipConstants.MEMBERSHIP_CONTACT_EMAIL
import com.anytypeio.anytype.core_models.membership.MembershipConstants.MEMBERSHIP_LEVEL_DETAILS
import com.anytypeio.anytype.core_models.membership.MembershipConstants.PRIVACY_POLICY
import com.anytypeio.anytype.core_models.membership.MembershipConstants.TERMS_OF_SERVICE
import com.anytypeio.anytype.payments.models.BillingPriceInfo
import com.anytypeio.anytype.payments.models.MembershipPurchase
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierPreview
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.core_models.membership.MembershipStatus
import com.anytypeio.anytype.core_models.membership.TierId

fun MembershipStatus.toMainView(
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState,
    accountId: String
): MembershipMainState {
    val (showBanner, subtitle) = determineBannerAndSubtitle()
    return MembershipMainState.Default(
        title = R.string.payments_header,
        subtitle = subtitle,
        tiersPreview = tiers.map {
            it.toPreviewView(
                membershipStatus = this,
                billingClientState = billingClientState,
                billingPurchaseState = billingPurchaseState
            )
        }.sortedByDescending { it.isActive },
        membershipLevelDetails = MEMBERSHIP_LEVEL_DETAILS,
        privacyPolicy = PRIVACY_POLICY,
        termsOfService = TERMS_OF_SERVICE,
        contactEmail = MEMBERSHIP_CONTACT_EMAIL,
        showBanner = showBanner,
        tiers = tiers.map {
            it.toView(
                membershipStatus = this,
                billingClientState = billingClientState,
                billingPurchaseState = billingPurchaseState,
                accountId = accountId,
            )
        }
    )
}

private fun MembershipStatus.determineBannerAndSubtitle(): Pair<Boolean, Int?> {
    return if (activeTier.value in ACTIVE_TIERS_WITH_BANNERS) {
        true to R.string.payments_subheader
    } else {
        false to null
    }
}

private fun MembershipStatus.isTierActive(tierId: Int): Boolean {
    return status == Membership.Status.STATUS_ACTIVE && activeTier.value == tierId
}

private fun MembershipTierData.isActiveTierPurchasedOnAndroid(activePaymentMethod: MembershipPaymentMethod): Boolean {
    val androidProductId = this.androidProductId
    return activePaymentMethod == METHOD_INAPP_GOOGLE && !androidProductId.isNullOrBlank()
}

fun MembershipTierData.toView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState,
    accountId: String
): Tier {
    val isActive = membershipStatus.isTierActive(id)
    val (buttonState, anyNameState) = mapButtonAndNameStates(
        isActive = isActive,
        billingPurchaseState = billingPurchaseState,
        membershipStatus = membershipStatus,
        accountId = accountId,
        billingClientState = billingClientState
    )

    return Tier(
        id = TierId(id),
        title = name,
        subtitle = description,
        conditionInfo = getConditionInfo(
            isActive = isActive,
            billingClientState = billingClientState,
            membershipStatus = membershipStatus
        ),
        isActive = isActive,
        features = features,
        membershipAnyName = anyNameState,
        buttonState = buttonState,
        email = getTierEmail(isActive, membershipStatus.userEmail),
        color = colorStr,
        urlInfo = androidManageUrl,
        stripeManageUrl = stripeManageUrl,
        iosManageUrl = iosManageUrl,
        androidManageUrl = androidManageUrl,
        androidProductId = androidProductId,
        paymentMethod = membershipStatus.paymentMethod
    )
}

fun MembershipTierData.toPreviewView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState
): TierPreview {
    val tierId = TierId(id)
    val isActive = membershipStatus.isTierActive(id)
    val tierName = name
    val tierDescription = description
    return TierPreview(
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

private fun MembershipTierData.mapButtonAndNameStates(
    isActive: Boolean,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState,
    membershipStatus: MembershipStatus,
    accountId: String
): Pair<TierButton, TierAnyName> {
    if (membershipStatus.isPending()) {
        return TierButton.Hidden to TierAnyName.Hidden
    }
    return if (isActive) {
        mapActiveTierButtonAndNameStates(
            billingPurchaseState = billingPurchaseState,
            paymentMethod = membershipStatus.paymentMethod,
            userEmail = membershipStatus.userEmail,
            accountId = accountId
        )
    } else {
        mapInactiveTierButtonAndNameStates(
            billingClientState = billingClientState,
            billingPurchaseState = billingPurchaseState,
            membershipStatus = membershipStatus,
            accountId = accountId
        )
    }
}

private fun MembershipStatus.isPending(): Boolean {
    return status == Membership.Status.STATUS_PENDING || status == Membership.Status.STATUS_PENDING_FINALIZATION
}

private fun MembershipTierData.mapActiveTierButtonAndNameStates(
    billingPurchaseState: BillingPurchaseState,
    paymentMethod: MembershipPaymentMethod,
    userEmail: String,
    accountId: String
): Pair<TierButton, TierAnyName> {
    val wasPurchasedOnAndroid = isActiveTierPurchasedOnAndroid(paymentMethod)
    if (!wasPurchasedOnAndroid) {
        return when {
            id == MembershipConstants.OLD_EXPLORER_ID && userEmail.isBlank() -> {
                TierButton.Submit.Enabled to TierAnyName.Hidden
            }
            id == MembershipConstants.OLD_EXPLORER_ID -> {
                TierButton.ChangeEmail to TierAnyName.Hidden
            }
            id == MembershipConstants.STARTER_ID && userEmail.isBlank() -> {
                TierButton.Submit.Enabled to TierAnyName.Hidden
            }
            id == MembershipConstants.STARTER_ID -> {
                TierButton.ChangeEmail to TierAnyName.Hidden
            }
            paymentMethod == METHOD_NONE -> {
                TierButton.Hidden to TierAnyName.Hidden
            }
            paymentMethod == METHOD_STRIPE || paymentMethod == METHOD_CRYPTO -> {
                TierButton.HiddenWithText.ManageOnDesktop to TierAnyName.Hidden
            }
            paymentMethod == METHOD_INAPP_APPLE -> {
                TierButton.HiddenWithText.ManageOnIOS to TierAnyName.Hidden
            }
            else -> {
                TierButton.Hidden to TierAnyName.Hidden
            }
        }
    }

    return if (billingPurchaseState is BillingPurchaseState.HasPurchases) {
        val purchases = billingPurchaseState.purchases
        return when (purchases.size) {
            0 -> TierButton.Manage.Android.Disabled to TierAnyName.Hidden
            1 -> {
                val purchase = purchases[0]
                val purchaseObfuscatedAccountId = purchase.accountId
                val containsProduct = purchase.products.any { it == androidProductId }
                if (purchaseObfuscatedAccountId == accountId
                    && containsProduct
                    && purchase.state == MembershipPurchase.PurchaseState.PURCHASED
                ) {
                    TierButton.Manage.Android.Enabled(androidProductId) to TierAnyName.Hidden
                } else {
                    TierButton.HiddenWithText.ManageOnAnotherAccount to TierAnyName.Hidden
                }
            }
            else -> TierButton.HiddenWithText.ManageOnAnotherAccount to TierAnyName.Hidden
        }
    } else {
        TierButton.HiddenWithText.ManageOnAnotherAccount to TierAnyName.Hidden
    }
}

private fun MembershipTierData.mapInactiveTierButtonAndNameStates(
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState,
    membershipStatus: MembershipStatus,
    accountId: String
): Pair<TierButton, TierAnyName> {
    val androidProductId = this.androidProductId
    val androidInfoUrl = this.androidManageUrl
    if (billingClientState is BillingClientState.NotAvailable) {
        return if (androidInfoUrl == null) {
            TierButton.Info.Disabled to TierAnyName.Hidden
        } else {
            TierButton.Info.Enabled(androidInfoUrl) to TierAnyName.Hidden
        }
    }
    if (androidProductId == null) {
        return if (androidInfoUrl == null) {
            TierButton.Info.Disabled to TierAnyName.Hidden
        } else {
            TierButton.Info.Enabled(androidInfoUrl) to TierAnyName.Hidden
        }
    }

    if (membershipStatus.activeTier.value == CO_CREATOR_ID) {
        return TierButton.Hidden to TierAnyName.Hidden
    }

    return when (billingPurchaseState) {
        is BillingPurchaseState.HasPurchases -> {
            getButtonStateAccordingToPurchaseState(
                androidProductId = androidProductId,
                accountId = accountId,
                purchases = billingPurchaseState.purchases
            ) to TierAnyName.Hidden
        }
        BillingPurchaseState.Loading -> {
            TierButton.Hidden to TierAnyName.Hidden
        }
        BillingPurchaseState.NoPurchases -> {
            handleNoPurchasesState(
                billingClientState = billingClientState,
                membershipStatus = membershipStatus,
                androidProductId = androidProductId,
                androidInfoUrl = androidInfoUrl
            )
        }
    }
}

private fun handleNoPurchasesState(
    billingClientState: BillingClientState,
    membershipStatus: MembershipStatus,
    androidProductId: String,
    androidInfoUrl: String?
): Pair<TierButton, TierAnyName> {
    if (billingClientState is BillingClientState.Connected) {
        val product = billingClientState.productDetails.find { it.productId == androidProductId }
        return when {
            product == null -> TierButton.Pay.Disabled to TierAnyName.Visible.Disabled
            product.billingPriceInfo() == null -> TierButton.Pay.Disabled to TierAnyName.Visible.Disabled
            membershipStatus.anyName.isBlank() -> TierButton.Pay.Disabled to TierAnyName.Visible.Enter
            else -> TierButton.Pay.Enabled to TierAnyName.Visible.Purchased(membershipStatus.anyName)
        }
    }
    if (billingClientState is BillingClientState.NotAvailable ) {
        return if (androidInfoUrl == null) {
            TierButton.Info.Disabled to TierAnyName.Hidden
        } else {
            TierButton.Info.Enabled(androidInfoUrl) to TierAnyName.Hidden
        }
    }
    return TierButton.Pay.Disabled to TierAnyName.Visible.Disabled
}

private fun getButtonStateAccordingToPurchaseState(
    androidProductId: String?,
    accountId: String,
    purchases: List<MembershipPurchase>
): TierButton {
    return when (purchases.size) {
        0 -> TierButton.Hidden
        1 -> {
            val purchase = purchases[0]
            val purchaseAccountId = purchase.accountId
            val containsProduct = purchase.products.any { it == androidProductId }
            when {
                purchaseAccountId != accountId ->
                    TierButton.HiddenWithText.DifferentPurchaseAccountId
                !containsProduct ->
                    TierButton.HiddenWithText.DifferentPurchaseProductId
                else -> TierButton.Hidden
            }
        }
        else -> TierButton.HiddenWithText.MoreThenOnePurchase
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
            createConditionInfoForBillingTier(
                billingClientState = billingClientState,
                membershipStatus = membershipStatus
            )
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
                TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_NOT_FOUND)
            } else {
                val billingPriceInfo = product.billingPriceInfo()
                if (billingPriceInfo == null) {
                    TierConditionInfo.Visible.Error(MembershipConstants.ERROR_PRODUCT_PRICE)
                } else {
                    TierConditionInfo.Visible.PriceBilling(
                        price = billingPriceInfo
                    )
                }
            }
        }
        BillingClientState.NotAvailable -> {
            TierConditionInfo.Visible.Price(
                price = formatPriceInCents(priceStripeUsdCents),
                period = convertToTierViewPeriod(this))
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
        if (id == MembershipConstants.OLD_EXPLORER_ID && membershipEmail.isBlank()) {
            return TierEmail.Visible.Enter
        }
        if (id == MembershipConstants.STARTER_ID && membershipEmail.isBlank()) {
            return TierEmail.Visible.Enter
        }
    }
    return TierEmail.Hidden
}

