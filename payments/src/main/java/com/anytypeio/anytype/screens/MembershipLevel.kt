package com.anytypeio.anytype.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.models.Tier
import com.anytypeio.anytype.peyments.R
import com.anytypeio.anytype.viewmodel.PaymentsTierState
import com.anytypeio.anytype.viewmodel.TierId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierScreen(
    state: PaymentsTierState,
    onDismiss: () -> Unit,
    actionPay: (TierId) -> Unit,
    actionSubmitEmail: (TierId, String) -> Unit
) {
    if (state is PaymentsTierState.Visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            modifier = Modifier.padding(top = 30.dp),
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            onDismissRequest = { onDismiss() },
            content = {
                MembershipLevels(
                    tier = state.tier,
                    actionPay = { actionPay(state.tier.id) },
                    actionSubmitEmail = { email -> actionSubmitEmail(state.tier.id, email) }
                )
            }
        )
    }
}

@Composable
fun MembershipLevels(tier: Tier, actionPay: () -> Unit, actionSubmitEmail: (String) -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            ),
    ) {

        val tierResources = mapTierToResources(tier)

        if (tierResources != null) {
            val brush = Brush.verticalGradient(
                listOf(
                    tierResources.colorGradient,
                    Color.Transparent
                )
            )
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .background(brush = brush, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        painter = painterResource(id = tierResources.mediumIcon!!),
                        contentDescription = "logo",
                        tint = tierResources.radialGradient
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    text = tierResources.title,
                    color = colorResource(id = R.color.text_primary),
                    style = HeadlineTitle,
                    textAlign = TextAlign.Start
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 6.dp),
                    text = tierResources.subtitle,
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
                tierResources.benefits.forEach { benefit ->
                    Benefit(benefit = benefit)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.height(30.dp))
                if (tier is Tier.Explorer) {
                    if (tier.isCurrent) {
                        StatusSubscribedExplorer(tier)
                    } else {
                        SubmitEmail(tier = tier, updateEmail = { email ->
                            actionSubmitEmail(email)
                        })
                    }
                }
                if (tier is Tier.Builder) {
                    if (tier.isCurrent) {
                        StatusSubscribed(tier, {})
                    } else {
                        NamePickerAndButton(
                            name = tier.name,
                            nameIsTaken = tier.nameIsTaken,
                            nameIsFree = tier.nameIsFree,
                            price = tier.price,
                            interval = tier.interval,
                            actionPay = actionPay
                        )
                    }
                }
                if (tier is Tier.CoCreator) {
                    if (tier.isCurrent) {
                        StatusSubscribed(tier, {})
                    } else {
                        NamePickerAndButton(
                            name = tier.name,
                            nameIsTaken = tier.nameIsTaken,
                            nameIsFree = tier.nameIsFree,
                            price = tier.price,
                            interval = tier.interval,
                            actionPay = actionPay
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NamePickerAndButton(
    name: String,
    nameIsTaken: Boolean,
    nameIsFree: Boolean,
    price: String,
    interval: String,
    actionPay: () -> Unit
) {
    var innerValue by remember(name) { mutableStateOf(name) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            )
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            text = stringResource(id = R.string.payments_tier_details_name_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            text = stringResource(id = R.string.payments_tier_details_name_subtitle),
            color = colorResource(id = R.color.text_primary),
            style = BodyCallout,
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = innerValue,
                onValueChange = { innerValue = it },
                textStyle = BodyRegular.copy(color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary)),
                singleLine = true,
                enabled = true,
                cursorBrush = SolidColor(colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary)),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(start = 0.dp, top = 2.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {

                        } else {

                        }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                decorationBox = { innerTextField ->
                    if (innerValue.isEmpty()) {
                        Text(
                            text = stringResource(id = com.anytypeio.anytype.localization.R.string.payments_tier_details_name_hint),
                            style = BodyRegular,
                            color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_tertiary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                    innerTextField()
                }
            )
            Text(
                text = stringResource(id = R.string.payments_tier_details_name_domain),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        val (messageTextColor, messageText) = when {
            nameIsTaken ->
                colorResource(id = R.color.palette_system_red) to stringResource(id = R.string.payments_tier_details_name_error)

            nameIsFree ->
                colorResource(id = R.color.palette_dark_lime) to stringResource(id = R.string.payments_tier_details_name_success)

            else ->
                colorResource(id = R.color.text_secondary) to stringResource(id = R.string.payments_tier_details_name_min)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = messageText,
            color = messageTextColor,
            style = Relations2,
            textAlign = TextAlign.Center
        )
        Price(price = price, interval = interval)
        Spacer(modifier = Modifier.height(14.dp))
        ButtonPay(enabled = true, actionPay = {
            actionPay()
        })
    }
}

@Composable
private fun StatusSubscribed(tier: Tier, actionManage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            )
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            text = stringResource(id = R.string.payments_tier_current_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.payments_tier_current_background)
                )
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                text = stringResource(id = R.string.payments_tier_current_valid),
                color = colorResource(id = R.color.text_primary),
                style = Relations2,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = tier.validUntil,
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitle,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 23.dp, bottom = 15.dp),
                text = stringResource(id = R.string.payments_tier_current_paid_by),
                color = colorResource(id = R.color.text_secondary),
                style = Relations2,
                textAlign = TextAlign.Center
            )

        }
        Spacer(modifier = Modifier.height(20.dp))
        ButtonSecondary(
            enabled = true,
            text = stringResource(id = R.string.payments_tier_current_button),
            onClick = { actionManage() },
            size = ButtonSize.LargeSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun StatusSubscribedExplorer(tier: Tier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            )
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            text = stringResource(id = R.string.payments_tier_current_title),
            color = colorResource(id = R.color.text_primary),
            style = BodyBold,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.payments_tier_current_background)
                )
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                text = stringResource(id = R.string.payments_tier_current_valid),
                color = colorResource(id = R.color.text_primary),
                style = Relations2,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 56.dp),
                text = tier.validUntil,
                color = colorResource(id = R.color.text_primary),
                style = HeadlineTitle,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        ButtonSecondary(
            enabled = true,
            text = stringResource(id = R.string.payments_tier_current_change_email_button),
            onClick = {  },
            size = ButtonSize.LargeSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun Price(price: String, interval: String) {
    Row() {
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 20.dp),
            text = price,
            color = colorResource(id = R.color.text_primary),
            style = HeadlineTitle,
            textAlign = TextAlign.Start
        )
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Bottom)
                .padding(bottom = 4.dp, start = 6.dp),
            text = interval,
            color = colorResource(id = R.color.text_primary),
            style = Relations1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun Benefit(benefit: String) {
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
private fun SubmitEmail(tier: Tier.Explorer, updateEmail: (String) -> Unit) {

    var innerValue by remember(tier.email) { mutableStateOf(tier.email) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isChecked by remember(tier.isChecked) { mutableStateOf(tier.isChecked) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            )
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = stringResource(id = R.string.payments_email_title),
            style = BodyBold,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = R.string.payments_email_subtitle),
            style = BodyCallout,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        BasicTextField(
            value = innerValue,
            onValueChange = { innerValue = it },
            textStyle = BodyRegular.copy(color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary)),
            singleLine = true,
            enabled = true,
            cursorBrush = SolidColor(colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 0.dp, top = 2.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {

                    } else {

                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            decorationBox = { innerTextField ->
                if (innerValue.isEmpty()) {
                    Text(
                        text = stringResource(id = com.anytypeio.anytype.localization.R.string.payments_email_hint),
                        style = BodyRegular,
                        color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                }
                innerTextField()
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        val icon = if (isChecked) {
            R.drawable.ic_system_checkbox
        } else {
            R.drawable.ic_system_checkbox_empty
        }
        Spacer(modifier = Modifier.height(15.dp))
        Row {
            Image(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(16.dp)
                    .noRippleClickable { isChecked = !isChecked },
                painter = painterResource(id = icon),
                contentDescription = "checkbox"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                text = stringResource(id = R.string.payments_email_checkbox_text),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        Spacer(modifier = Modifier.height(31.dp))
        val enabled = innerValue.isNotEmpty()
        ButtonPrimary(
            enabled = enabled,
            text = stringResource(id = R.string.payments_detials_button_submit),
            onClick = { updateEmail.invoke(innerValue) },
            size = ButtonSize.Large,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ButtonPay(enabled: Boolean, actionPay: () -> Unit) {
    ButtonPrimary(
        enabled = enabled,
        text = stringResource(id = R.string.payments_detials_button_pay),
        onClick = { actionPay() },
        size = ButtonSize.Large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}


@Preview()
@Composable
fun MyLevel() {
    MembershipLevels(
        tier = Tier.Explorer(
            id = TierId("121"),
            isCurrent = true,
            price = "$99",
            validUntil = "12/12/2025",
        ),
        actionPay = {},
        actionSubmitEmail = {}
    )
}