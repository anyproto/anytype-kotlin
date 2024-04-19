package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.Membership
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.payments.viewmodel.PaymentsWelcomeState
import com.anytypeio.anytype.presentation.membership.models.TierId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWelcomeScreen(state: PaymentsWelcomeState, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (state is PaymentsWelcomeState.Initial) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = colorResource(id = R.color.background_secondary),
            content = {
                val tierResources = mapTierToResources(state.tier)
                if (tierResources != null) WelcomeContent(tierResources, onDismiss)
            },
            shape = RoundedCornerShape(16.dp),
            dragHandle = null
        )
    }
}

@Composable
private fun WelcomeContent(tierResources: TierResources, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Icon(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = tierResources.mediumIcon!!),
            contentDescription = "logo",
            tint = tierResources.colors.gradientStart
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.payments_welcome_title, tierResources.title),
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


@Preview
@Composable
fun PaymentWelcomeScreenPreview() {
    PaymentWelcomeScreen(
        PaymentsWelcomeState.Initial(
            Tier.Explorer(
                TierId(22),
                true,
                "01-01-2025",
                color = "green",
                features = listOf("Feature 1", "Feature 2"),
                androidTierId = null
            )
        ), {})
}