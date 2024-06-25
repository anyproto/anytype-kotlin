package com.anytypeio.anytype.payments.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.payments.viewmodel.WelcomeState
import com.anytypeio.anytype.core_models.membership.TierId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(state: WelcomeState, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (state is WelcomeState.Initial) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = Color.Transparent,
            content = {
                BoxWithConstraints(
                    Modifier.navigationBarsPadding()
                ) {
                    val boxWithConstraintsScope = this
                    val tierResources = mapTierToResources(state.tier)
                    WelcomeContent(state.tier, tierResources, onDismiss)
                }
            },
            shape = RoundedCornerShape(16.dp),
            dragHandle = null,
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}

@Composable
private fun WelcomeContent(tier: Tier, tierResources: TierResources, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(36.dp))
        Icon(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = tierResources.mediumIcon),
            contentDescription = "logo",
            tint = tierResources.colors.gradientEnd
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.payments_welcome_title, tier.title),
            color = colorResource(id = R.color.text_primary),
            style = HeadlineHeading,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.payments_welcome_subtitle),
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        ButtonSecondary(
            text = stringResource(id = R.string.payments_welcome_button),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            onClick = { onDismiss() },
            size = ButtonSize.LargeSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun PaymentWelcomeScreenPreview() {
    WelcomeScreen(
        WelcomeState.Initial(
            tier = Tier(
                id = TierId(value = 3506),
                isActive = false,
                title = "Tier Title",
                subtitle = "Tier Subtitle",
                conditionInfo = TierConditionInfo.Visible.Price(
                    price = "$99.9", period = TierPeriod.Year(1)

                ),
                features = listOf(),
                membershipAnyName = TierAnyName.Visible.Enter,
                buttonState = TierButton.Manage.Android.Enabled(""),
                email = TierEmail.Visible.Enter,
                color = "red",
                stripeManageUrl = "",
                iosManageUrl = "",
                androidManageUrl = "",
                androidProductId = "",
                paymentMethod = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
            )
        )
    ) { }
}