package com.anytypeio.anytype.payments.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.payments.viewmodel.PaymentsMainState
import com.anytypeio.anytype.presentation.membership.models.MembershipStatus
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId

@Preview
@Composable
fun MainPaymentsScreenPreview() {
    val tiers = listOf(
        Tier.Explorer(
            TierId(1),
            isCurrent = true,
            validUntil = "2022-12-31",
            color = "blue",
            features = listOf("Feature 1", "Feature 2"),
            status = Membership.Status.STATUS_ACTIVE,
            androidTierId = null
        ),
        Tier.Builder(
            TierId(2),
            isCurrent = true,
            validUntil = "5 Feb 2025",
            color = "red",
            features = listOf("Feature 1", "Feature 2"),
            status = Membership.Status.STATUS_ACTIVE,
            androidTierId = null
        ),
        Tier.CoCreator(
            TierId(3),
            isCurrent = false,
            validUntil = "2022-12-31",
            color = "green",
            features = listOf("Feature 1", "Feature 2"),
            status = Membership.Status.STATUS_ACTIVE,
            androidTierId = null
        ),
        Tier.Custom(
            TierId(4),
            isCurrent = false,
            validUntil = "2022-12-31",
            color = "blue",
            features = listOf("Feature 1", "Feature 2"),
            status = Membership.Status.STATUS_ACTIVE,
            androidTierId = null
        )
    )
    MainPaymentsScreen(PaymentsMainState.Default.WithBanner(tiers = tiers, 
        membershipStatus = MembershipStatus(
            activeTier = TierId(value = 9654),
            status = Membership.Status.STATUS_UNKNOWN,
            dateEnds = 6570,
            paymentMethod = MembershipPaymentMethod.METHOD_CRYPTO,
            anyName = "Ofelia Conrad",
            tiers = listOf()
        )), {})
}