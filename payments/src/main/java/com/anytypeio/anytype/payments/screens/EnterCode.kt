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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val enteredDigits = remember { mutableStateListOf<Char>() }
    val focusManager = LocalFocusManager.current

    var timeLeft by remember { mutableStateOf(RESEND_DELAY) }
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    LaunchedEffect(key1 = enteredDigits.size) {
        if (enteredDigits.size == 4) {
            val code = enteredDigits.joinToString("")
            action(TierAction.OnVerifyCodeClicked(code))
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
            val modifier = Modifier
                .width(48.dp)
                .height(64.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                focusRequesters.forEachIndexed { index, focusRequester ->
                    CodeNumber(
                        isEnabled = state !is MembershipEmailCodeState.Visible.Loading,
                        modifier = modifier,
                        focusRequester = focusRequester,
                        onDigitEntered = { digit ->
                            if (enteredDigits.size < 4) {
                                enteredDigits.add(digit)
                            }
                            if (index < 3) focusRequesters[index + 1].requestFocus()
                        },
                        onBackspace = {
                            if (enteredDigits.isNotEmpty()) enteredDigits.removeLast()
                            if (index > 0) focusRequesters[index - 1].requestFocus()
                        }
                    )
                    if (index < 3) Spacer(modifier = Modifier.width(8.dp))
                }
            }
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
private fun CodeNumber(
    isEnabled: Boolean,
    focusRequester: FocusRequester,
    onDigitEntered: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier
) {
    val (text, setText) = remember { mutableStateOf("") }

    val borderColor = colorResource(id = R.color.shape_primary)
    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            when {
                newValue.length == 1 && newValue[0].isDigit() && text.isEmpty() -> {
                    setText(newValue)
                    onDigitEntered(newValue[0])
                }

                newValue.isEmpty() -> {
                    if (text.isNotEmpty()) {
                        setText("")
                        onBackspace()
                    }
                }
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && text.isEmpty()) {
                    onBackspace()
                    true
                } else false
            },
        singleLine = true,
        enabled = isEnabled,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        },
        textStyle = HeadlineTitle.copy(color = colorResource(id = R.color.text_primary))
    )
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
        state = MembershipEmailCodeState.Visible.Loading,
        action = {}
    )
}