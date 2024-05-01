package com.anytypeio.anytype.payments.mapping

import com.anytypeio.anytype.core_models.membership.MembershipTierData
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.payments.playbilling.BillingClientState
import com.anytypeio.anytype.payments.playbilling.BillingPurchaseState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus

fun MembershipTierData.toView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
    billingPurchaseState: BillingPurchaseState
): TierView {
    TODO("in the next PR")
}

fun MembershipTierData.toPreviewView(
    membershipStatus: MembershipStatus,
    billingClientState: BillingClientState,
): TierPreviewView {
    TODO("in the next PR")
}