package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.payments.viewmodel.TierAction
import com.anytypeio.anytype.presentation.membership.models.TierId


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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                Benefit(benefit = benefit)
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(id = R.color.background_primary)
                )
                .weight(1f, true)
        ) {
            Spacer(modifier = Modifier.height(26.dp))
            if (state.tierView.isActive) {
                ConditionInfoView(state = state.tierView.conditionInfo)
            } else {
                ConditionInfoView(state = state.tierView.conditionInfo)
                Spacer(modifier = Modifier.height(14.dp))
                MainButton(buttonState = state.tierView.buttonState)
            }
        }
    }
}

@Composable
fun Benefit(benefit: String) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterStart),
            painter = painterResource(id = R.drawable.ic_check_16),
            contentDescription = "text check icon"
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp)
                .align(Alignment.CenterStart),
            text = benefit,
            style = BodyCallout,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
private fun MainButton(buttonState: TierButton) {
    if (buttonState !is TierButton.Hidden) {
        val (stringRes, enabled) = getButtonText(buttonState)
        ButtonPrimary(
            enabled = enabled,
            text = stringResource(id = stringRes),
            onClick = { },
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun getButtonText(buttonState: TierButton): Pair<Int, Boolean> {
    return when (buttonState)  {
        TierButton.Hidden -> Pair(0, false)
        TierButton.Info.Disabled -> Pair(R.string.payments_button_info, false)
        is TierButton.Info.Enabled -> Pair(R.string.payments_button_info, true)
        TierButton.Manage.Android.Disabled -> Pair(R.string.payments_button_manage, false)
        is TierButton.Manage.Android.Enabled -> Pair(R.string.payments_button_manage, true)
        TierButton.Manage.External.Disabled -> Pair(R.string.payments_button_manage, false)
        is TierButton.Manage.External.Enabled -> Pair(R.string.payments_button_manage, true)
        TierButton.Submit.Disabled -> Pair(R.string.payments_button_submit, false)
        TierButton.Submit.Enabled -> Pair(R.string.payments_button_submit, true)
        TierButton.Pay.Disabled -> Pair(R.string.payments_button_pay, false)
        TierButton.Pay.Enabled -> Pair(R.string.payments_button_pay, true)
    }
}

@Preview
@Composable
fun TierViewScreenPreview() {
    TierViewScreen(
        state = MembershipTierState.Visible(
            tierView = TierView(
                title = R.string.payments_tier_builder,
                subtitle = R.string.payments_tier_builder_description,
                features = listOf(
                    "Feature 1",
                    "Feature 2",
                    "Feature 3",
                    "Feature 1",
                    "Feature 2",
                    "Feature 3",
                    "Feature 1",
                    "Feature 2",
                    "Feature 3",
                    "Feature 1",
                    "Feature 2",
                    "Feature 3"
                ),
                isActive = false,
                conditionInfo = TierConditionInfo.Visible.Price(
                    price = "99.00",
                    period = TierPeriod.Year(1)
                ),
                buttonState = TierButton.Pay.Disabled,
                id = TierId(value = 2705),
                membershipAnyName = TierAnyName.Visible.Enter,
                email = TierEmail.Hidden,
                color = "red"
            )
        ),
        actionTier = {},
        onDismiss = {}
    )
}
