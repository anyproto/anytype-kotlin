package com.anytypeio.anytype.payments.mapping

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.payments.constants.TiersConstants.ACTIVE_TIERS_WITH_BANNERS
import com.anytypeio.anytype.payments.constants.TiersConstants.MEMBERSHIP_CONTACT_EMAIL
import com.anytypeio.anytype.payments.constants.TiersConstants.MEMBERSHIP_LEVEL_DETAILS
import com.anytypeio.anytype.payments.constants.TiersConstants.PRIVACY_POLICY
import com.anytypeio.anytype.payments.constants.TiersConstants.TERMS_OF_SERVICE
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus


fun MembershipStatus.toMainView(
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState
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
            it.toPreviewView(
                membershipStatus = this,
                billingClientState = billingClientState,
                billingPurchaseState = billingPurchaseState
            )
        },
        membershipLevelDetails = MEMBERSHIP_LEVEL_DETAILS,
        privacyPolicy = PRIVACY_POLICY,
        termsOfService = TERMS_OF_SERVICE,
        contactEmail = MEMBERSHIP_CONTACT_EMAIL,
        showBanner = showBanner
    )
}