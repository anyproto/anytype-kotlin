package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.payments.constants.TiersConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.payments.constants.TiersConstants.BUILDER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.CO_CREATOR_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.MEMBERSHIP_CONTACT_EMAIL
import com.anytypeio.anytype.payments.constants.TiersConstants.MEMBERSHIP_LEVEL_DETAILS
import com.anytypeio.anytype.payments.constants.TiersConstants.PRIVACY_POLICY
import com.anytypeio.anytype.payments.constants.TiersConstants.TERMS_OF_SERVICE
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.TierId


fun MembershipStatus.toMainView(
    billingClientState: BillingClientState
): MembershipMainState {
    val (showBanner, subtitle) = if (activeTier.value in ACTIVE_TIERS_WITH_BANNERS) {
        true to R.string.payments_subheader
    } else {
        false to null
    }
    return MembershipMainState.Default(
        title = R.string.payments_header,
        subtitle = subtitle,
        tiers = tiers.map {
            convertToPreviewTierView(
                membershipStatus = this,
                tier = it,
                billingClientState = billingClientState
            )
        },
        membershipLevelDetails = MEMBERSHIP_LEVEL_DETAILS,
        privacyPolicy = PRIVACY_POLICY,
        termsOfService = TERMS_OF_SERVICE,
        contactEmail = MEMBERSHIP_CONTACT_EMAIL,
        showBanner = showBanner
    )
}

fun MembershipStatus.toTierView(
    tier: MembershipTierData,
    billingClientState: BillingClientState
): MembershipTierState {
//    val product = products.find { it.productId == tier.androidProductId }
//    val price = product?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
    val isTierActive = isTierActive(tier.id)
    return MembershipTierState.Visible(
        tierView = MembershipTierView(
            id = TierId(tier.id),
            title = getTierTitle(tier.id),
            subtitle = getTierSubtitle(tier.id),
            features = tier.features,
            conditionInfo = MembershipTierConditionInfo.Hidden,
            buttonState = MembershipTierButton.Pay.Enabled,
            membershipAnyName = MembershipTierAnyName.Hidden
        )
    )
}

private fun getConditionInfo(
    isCurrent: Boolean,
    tier: MembershipTierData,
    billingClientState: BillingClientState
): MembershipTierConditionInfo {
    if (isCurrent)  {
        return MembershipTierConditionInfo.Hidden
    } else {
        val androidProductId = tier.androidProductId
        if (androidProductId == null)  {
            //берем инфу по цене из модельки тира

            // this one is a price we use ONLY on Stripe platform, If 0 then it's a free tier
            val price = tier.priceStripeUsdCents

            // i.e. "5 days" or "3 years"
            val periodValue = tier.periodValue

            // how long is the period of the subscription
            val periodType = tier.periodType

        }
    }


    return when (billingClientState) {
        is BillingClientState.Connected.Error -> MembershipTierConditionInfo.Error
        is BillingClientState.Connected.Ready -> MembershipTierConditionInfo.Hidden
        BillingClientState.Disconnected -> MembershipTierConditionInfo.Error
    }
}

private fun getTierPrice(tier: MembershipTierData, billingClientState: BillingClientState) : String {
    when (billingClientState) {
        is BillingClientState.Connected.Error -> TODO()
        is BillingClientState.Connected.Ready -> TODO()
        BillingClientState.Disconnected -> TODO()
    }
    return "$99 per year"
}

fun convertToPreviewTierView(
    membershipStatus: MembershipStatus,
    tier: MembershipTierData,
    billingClientState: BillingClientState
): MembershipTierPreviewView {
    return MembershipTierPreviewView(
        id = TierId(tier.id),
        title = getTierTitle(tier.id),
        subtitle = getTierSubtitle(tier.id),
        conditionInfo = MembershipTierConditionInfo.Hidden,
        isCurrent = membershipStatus.isTierActive(tier.id)
    )
}

private fun getTierTitle(tierId: Int): Int {
    return when (tierId) {
        EXPLORER_ID -> R.string.payments_tier_explorer
        BUILDER_ID -> R.string.payments_tier_builder
        CO_CREATOR_ID -> R.string.payments_tier_cocreator
        else -> R.string.payments_tier_custom
    }
}

private fun getTierSubtitle(tierId: Int): Int {
    return when (tierId) {
        EXPLORER_ID -> R.string.payments_tier_explorer_description
        BUILDER_ID -> R.string.payments_tier_builder_description
        CO_CREATOR_ID -> R.string.payments_tier_cocreator_description
        else -> R.string.payments_tier_custom_description
    }
}

fun MembershipStatus.toTiersView(
): List<Tier> {
    return this.tiers.map { tier ->
        when (tier.id) {
            EXPLORER_ID -> Tier.Explorer(
                id = TierId(tier.id),
                isCurrent = isTierActive(EXPLORER_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features
            )

            BUILDER_ID -> Tier.Builder(
                id = TierId(tier.id),
                isCurrent = isTierActive(BUILDER_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features,
            )

            CO_CREATOR_ID -> Tier.CoCreator(
                id = TierId(tier.id),
                isCurrent = isTierActive(CO_CREATOR_ID),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features,
            )

            else -> Tier.Custom(
                id = TierId(tier.id),
                isCurrent = isTierActive(tier.id),
                prettyName = tier.name,
                color = tier.colorStr,
                features = tier.features,
            )
        }
    }
}

fun MembershipStatus.isTierActive(tierId: Int): Boolean {
    return when (this.status) {
        Membership.Status.STATUS_ACTIVE -> activeTier.value == tierId
        else -> false
    }
}