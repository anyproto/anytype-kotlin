package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.constants.TiersConstants.BUILDER_ID
import com.anytypeio.anytype.payments.constants.TiersConstants.EXPLORER_ID
import com.anytypeio.anytype.payments.models.TierAnyName
import com.anytypeio.anytype.payments.models.TierButton
import com.anytypeio.anytype.payments.models.TierConditionInfo
import com.anytypeio.anytype.payments.models.TierEmail
import com.anytypeio.anytype.payments.models.TierPeriod
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.payments.viewmodel.MembershipTierState
import com.anytypeio.anytype.payments.viewmodel.TierAction
import com.anytypeio.anytype.presentation.membership.models.TierId
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TierViewScreen(
    state: MembershipTierState,
    onDismiss: () -> Unit,
    actionTier: (TierAction) -> Unit,
    anyNameTextField: TextFieldState,
    anyEmailTextField: TextFieldState
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
                    anyNameTextField = anyNameTextField,
                    anyEmailTextField = anyEmailTextField
                )
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TierViewVisible(
    state: MembershipTierState.Visible,
    actionTier: (TierAction) -> Unit,
    anyNameTextField: TextFieldState,
    anyEmailTextField: TextFieldState
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            )
            .verticalScroll(scrollState),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                .wrapContentHeight()
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(id = R.color.background_primary)
                )
        ) {
            Spacer(modifier = Modifier.height(26.dp))
            if (state.tierView.isActive) {
                ConditionInfoView(state = state.tierView.conditionInfo)
                MembershipEmailScreen(
                    state = state.tierView.email,
                    anyEmailTextField = anyEmailTextField
                )
                Spacer(modifier = Modifier.height(20.dp))
                SecondaryButton(
                    buttonState = state.tierView.buttonState,
                    tierId = state.tierView.id,
                    actionTier = actionTier
                )
            } else {
                AnyNameView(
                    anyNameState = state.tierView.membershipAnyName,
                    anyNameTextField = anyNameTextField
                )
                ConditionInfoView(state = state.tierView.conditionInfo)
                Spacer(modifier = Modifier.height(14.dp))
                MainButton(
                    buttonState = state.tierView.buttonState,
                    tierView = state.tierView,
                    actionTier = actionTier
                )
            }
            Spacer(modifier = Modifier.height(300.dp))
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
private fun MainButton(
    tierView: TierView,
    buttonState: TierButton,
    actionTier: (TierAction) -> Unit
) {
    if (buttonState !is TierButton.Hidden) {
        val (stringRes, enabled) = getButtonText(buttonState)
        ButtonPrimary(
            enabled = enabled,
            text = stringResource(id = stringRes),
            onClick = {
                      when (buttonState) {
                          is TierButton.Pay.Enabled -> actionTier(TierAction.PayClicked(tierView.id))
                          is TierButton.Info.Enabled -> actionTier(TierAction.OpenUrl(tierView.urlInfo))
                          is TierButton.Manage.Android.Enabled -> TODO()
                          is TierButton.Manage.External.Enabled -> TODO()
                          TierButton.Submit.Enabled -> actionTier(TierAction.SubmitClicked)
                          else -> {
                              Timber.d("MainButton: skipped action: $buttonState")
                          }
                      }

            },
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun SecondaryButton(
    tierId: TierId,
    buttonState: TierButton,
    actionTier: (TierAction) -> Unit
) {
    if (buttonState !is TierButton.Hidden) {
        val (stringRes, enabled) = getButtonText(buttonState)
        ButtonSecondary(
            enabled = enabled,
            text = stringResource(id = stringRes),
            onClick = {
                when (buttonState) {
                    is TierButton.Pay.Enabled -> actionTier(TierAction.PayClicked(tierId))
                    is TierButton.Manage.Android.Enabled -> actionTier(TierAction.ManagePayment(tierId))
                    TierButton.Submit.Enabled -> actionTier(TierAction.SubmitClicked)
                    else -> {}
                }

            },
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

@OptIn(ExperimentalFoundationApi::class)
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
                    "Feature 1"
                ),
                isActive = true,
                conditionInfo = TierConditionInfo.Visible.Valid(
                    dateEnds = 1714199910,
                    period = TierPeriod.Year(1),
                    payedBy = MembershipPaymentMethod.METHOD_INAPP_GOOGLE
                ),
                buttonState = TierButton.Manage.Android.Enabled("eqweqw"),
                id = TierId(value = EXPLORER_ID),
                membershipAnyName = TierAnyName.Hidden,
                email = TierEmail.Visible.Enter,
                color = "teal"
            )
        ),
        actionTier = {},
        onDismiss = {},
        anyNameTextField = TextFieldState(),
        anyEmailTextField = TextFieldState()
    )
}
