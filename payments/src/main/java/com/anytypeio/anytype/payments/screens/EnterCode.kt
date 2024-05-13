package com.anytypeio.anytype.payments.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.viewmodel.MembershipEmailCodeState
import com.anytypeio.anytype.payments.viewmodel.TierAction
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(
    state: MembershipEmailCodeState,
    action: (TierAction) -> Unit,
    onDismiss: () -> Unit
) {
    if (state is MembershipEmailCodeState.Visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = colorResource(id = R.color.background_primary),
            content = {
                ModalCodeContent(
                    state = state,
                    action = action
                )
            }
        )
    }
}

@Composable
private fun ModalCodeContent(
    state: MembershipEmailCodeState.Visible,
    action: (TierAction) -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val borderColor = colorResource(id = R.color.shape_primary)
    val borderSelectedColor = colorResource(id = R.color.shape_secondary)

    var timeLeft by remember { mutableStateOf(RESEND_DELAY) }
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    LaunchedEffect(key1 = enteredDigits.length) {
        if (enteredDigits.length == 4) {
            action(TierAction.OnVerifyCodeClicked(enteredDigits))
        }
    }

    LaunchedEffect(key1 = state) {
        if (state is MembershipEmailCodeState.Visible.Loading) {
            focusManager.clearFocus(true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.padding(118.dp))
            Text(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                text = stringResource(id = com.anytypeio.anytype.localization.R.string.payments_code_title),
                style = BodyBold,
                color = colorResource(
                    id = R.color.text_primary
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(44.dp))

            BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = enteredDigits,
                    onValueChange = {
                        if (it.length <= 4) {
                            enteredDigits = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                    ),
                    decorationBox = {
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(4) { index ->
                                val char = when {
                                    index >= enteredDigits.length -> ""
                                    else -> enteredDigits[index].toString()
                                }
                                val isFocused = index == enteredDigits.length
                                Text(modifier = Modifier
                                        .width(50.dp)
                                        .border(BorderStroke(
                                                if (isFocused) 2.dp else 1.dp,
                                                if (isFocused) borderSelectedColor else borderColor),
                                                RoundedCornerShape(8.dp)
                                        )
                                    .padding(vertical = 16.dp),
                                        text = char,
                                        style = HeadlineTitle,
                                        color = colorResource(id = R.color.text_primary),
                                        textAlign = TextAlign.Center
                                )
                                if (index < 3) Spacer(modifier = Modifier.width(15.dp))
                            }
                        }
                    }
            )
            val (messageTextColor, messageText) = when (state) {
                is MembershipEmailCodeState.Visible.Error -> ErrorMessage(state)
                is MembershipEmailCodeState.Visible.ErrorOther -> colorResource(id = R.color.palette_system_red) to state.message
                MembershipEmailCodeState.Visible.Success -> colorResource(id = R.color.palette_dark_lime) to stringResource(
                    id = R.string.membership_email_code_success
                )

                else -> Color.Transparent to ""
            }
            Text(
                text = messageText.orEmpty(),
                color = messageTextColor,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 7.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(149.dp))
            val (resendEnabled, resendText) = if (timeLeft == 0) {
                true to stringResource(id = R.string.payments_code_resend)
            } else {
                false to stringResource(id = R.string.payments_code_resend_in, timeLeft)
            }
            Text(
                modifier = Modifier
                        .fillMaxWidth()
                        .noRippleThrottledClickable {
                            if (resendEnabled) {
                                timeLeft = RESEND_DELAY
                                action(TierAction.OnResendCodeClicked)
                            }
                        }
                        .alpha(if (resendEnabled) 1f else 0.5f),
                text = resendText,
                style = PreviewTitle1Regular,
                color = colorResource(id = R.color.text_tertiary),
                textAlign = TextAlign.Center
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = state is MembershipEmailCodeState.Visible.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = colorResource(R.color.shape_secondary),
                trackColor = colorResource(R.color.shape_primary)
            )
        }
    }
}

@Composable
private fun ErrorMessage(state: MembershipEmailCodeState.Visible.Error): Pair<Color, String> {
    val color = colorResource(id = R.color.palette_system_red)
    val message = when (state.error) {
        is MembershipErrors.VerifyEmailCode.BadInput -> R.string.membership_name_bad_input
        is MembershipErrors.VerifyEmailCode.CacheError -> R.string.membership_name_cache_error
        is MembershipErrors.VerifyEmailCode.CanNotConnect -> R.string.membership_name_cant_connect
        is MembershipErrors.VerifyEmailCode.CodeExpired -> R.string.membership_email_code_expired
        is MembershipErrors.VerifyEmailCode.CodeWrong -> R.string.membership_email_code_wrong
        is MembershipErrors.VerifyEmailCode.EmailAlreadyVerified -> R.string.membership_email_already_verified
        is MembershipErrors.VerifyEmailCode.MembershipAlreadyActive -> R.string.membership_email_membership_already_active
        is MembershipErrors.VerifyEmailCode.MembershipNotFound -> R.string.membership_email_membership_not_found
        is MembershipErrors.VerifyEmailCode.NotLoggedIn -> R.string.membership_name_not_logged
        is MembershipErrors.VerifyEmailCode.Null -> R.string.membership_any_name_null_error
        is MembershipErrors.VerifyEmailCode.PaymentNodeError -> R.string.membership_name_payment_node_error
        is MembershipErrors.VerifyEmailCode.UnknownError -> R.string.membership_any_name_unknown
        else -> R.string.membership_any_name_unknown
    }
    return color to stringResource(id = message)
}

const val RESEND_DELAY = 60

@Preview
@Composable
fun EnterCodeModalPreview() {
    ModalCodeContent(
        state = MembershipEmailCodeState.Visible.Initial,
        action = {}
    )
}