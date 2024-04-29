package com.anytypeio.anytype.payments.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.viewmodel.MembershipMainState
import com.anytypeio.anytype.payments.viewmodel.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId

@Preview
@Composable
fun MainPaymentsScreenPreview() {
    val tiers = listOf(
        Tier.Explorer(
            TierId(1),
            isCurrent = true,
            color = "blue",
            features = listOf("Feature 1", "Feature 2"),
        ),
        Tier.Builder(
            TierId(2),
            isCurrent = true,
            color = "red",
            features = listOf("Feature 1", "Feature 2"),
        ),
        Tier.CoCreator(
            TierId(3),
            isCurrent = false,
            color = "green",
            features = listOf("Feature 1", "Feature 2"),
        ),
        Tier.Custom(
            TierId(4),
            isCurrent = false,
            color = "blue",
            features = listOf("Feature 1", "Feature 2"),
        )
    )
//    MainPaymentsScreen(MembershipMainState.Default(tiers = tiers,
//        title = R.string.payments_header,
//        subtitle = null,
//        showBanner = false,
//        membershipLevelDetails = "ridens",
//        privacyPolicy = "alterum",
//        termsOfService = "esse",
//        contactEmail = "luella.sears@example.com"
//    )) {}
}