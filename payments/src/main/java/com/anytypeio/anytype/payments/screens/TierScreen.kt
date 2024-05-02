package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.payments.viewmodel.TierAction


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierViewScreen(
    state: MembershipTierState,
    onDismiss: () -> Unit,
    actionTier: (TierAction) -> Unit
) {
    if (state is MembershipTierState.Visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 30.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            onDismissRequest = { onDismiss() },
            content = {
                TierViewVisible(
                    state = state,
                    actionTier = actionTier,
                )
            }
        )
    }
}

@Composable
private fun TierViewVisible(
    state: MembershipTierState.Visible,
    actionTier: (TierAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val tierResources: TierResources = mapTierToResources(state.tierView)
            val brush = Brush.verticalGradient(
                listOf(
                    tierResources.colors.gradientStart,
                    Color.Transparent
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(brush = brush, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.BottomStart
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 16.dp),
                    painter = painterResource(id = tierResources.mediumIcon),
                    contentDescription = "logo",
                    tint = tierResources.colors.gradientEnd
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(id = state.tierView.title),
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitle,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                text = stringResource(id = state.tierView.subtitle),
                color = colorResource(id = R.color.text_primary),
                style = BodyCallout,
                textAlign = TextAlign.Start
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp),
                text = stringResource(id = R.string.payments_details_whats_included),
                color = colorResource(id = R.color.text_secondary),
                style = BodyCallout,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(6.dp))
            state.tierView.features.forEach { benefit ->
                //Benefit(benefit = benefit)
                Spacer(modifier = Modifier.height(6.dp))
            }

        }
    }
}

@Composable
private fun getButtonText(buttonState: TierButton): Pair<Int, Boolean> {
    return when (buttonState)  {
        TierButton.Hidden -> Pair(0, false)
        TierButton.Info.Disabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_info, false)
        is TierButton.Info.Enabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_info, true)
        TierButton.Manage.Android.Disabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_manage, false)
        is TierButton.Manage.Android.Enabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_manage, true)
        TierButton.Manage.External.Disabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_manage, false)
        is TierButton.Manage.External.Enabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_manage, true)
        TierButton.Submit.Disabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_submit, false)
        TierButton.Submit.Enabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_submit, true)
        TierButton.Pay.Disabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_pay, false)
        TierButton.Pay.Enabled -> Pair(com.anytypeio.anytype.core_ui.R.string.payments_button_pay, true)
    }
}