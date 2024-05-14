package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldLineLimits
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.models.TierEmail

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MembershipEmailScreen(
    state: TierEmail,
    anyEmailTextField: TextFieldState
) {
    if (state != TierEmail.Hidden) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val anyEmailEnabled = remember { mutableStateOf(false) }
        val showHint = remember { mutableStateOf(false) }

        anyEmailEnabled.value = when (state) {
            TierEmail.Hidden -> false
            TierEmail.Visible.Enter -> true
            TierEmail.Visible.Validated -> true
            TierEmail.Visible.Validating -> false
            is TierEmail.Visible.Error -> true
            is TierEmail.Visible.ErrorOther -> true
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(id = R.color.background_primary)
                )
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.payments_email_title),
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.payments_email_subtitle),
                color = colorResource(id = R.color.text_secondary),
                style = BodyCallout,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier) {
                BasicTextField2(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            showHint.value = !it.isFocused && anyEmailTextField.text.isEmpty()
                        },
                    state = anyEmailTextField,
                    textStyle = BodyRegular.copy(color = colorResource(id = R.color.text_primary)),
                    enabled = anyEmailEnabled.value,
                    cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    interactionSource = remember { MutableInteractionSource() }
                )
                if (showHint.value) {
                    Text(
                        text = stringResource(id = R.string.payments_email_hint),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            val (messageTextColor, messageText) = when (state) {
                is TierEmail.Visible.Error -> ErrorMessage(state)
                is TierEmail.Visible.ErrorOther -> colorResource(id = R.color.palette_system_red) to (state.message ?: stringResource(id = R.string.membership_any_name_unknown))
                else -> Color.Transparent to ""
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
        }
    }
}

@Composable
fun ErrorMessage(state: TierEmail.Visible.Error): Pair<Color, String> {
    val color = colorResource(id = R.color.palette_system_red)
    val res = when (state.membershipErrors) {
        is MembershipErrors.GetVerificationEmail.EmailAlreadySent -> R.string.membership_email_already_sent
        is MembershipErrors.GetVerificationEmail.EmailAlreadyVerified -> R.string.membership_email_already_verified
        is MembershipErrors.GetVerificationEmail.EmailFailedToSend -> R.string.membership_email_failed_to_send
        is MembershipErrors.GetVerificationEmail.EmailWrongFormat -> R.string.membership_email_wrong_format
        is MembershipErrors.GetVerificationEmail.MembershipAlreadyExists -> R.string.membership_email_membership_already_exists
        is MembershipErrors.GetVerificationEmail.BadInput -> R.string.membership_name_bad_input
        is MembershipErrors.GetVerificationEmail.CacheError -> R.string.membership_name_cache_error
        is MembershipErrors.GetVerificationEmail.CanNotConnect -> R.string.membership_name_cant_connect
        is MembershipErrors.GetVerificationEmail.NotLoggedIn -> R.string.membership_name_not_logged
        is MembershipErrors.GetVerificationEmail.PaymentNodeError -> R.string.membership_name_payment_node_error
        else -> R.string.membership_any_name_unknown
    }
    return color to stringResource(id = res)
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun MembershipEmailScreenPreview() {
    MembershipEmailScreen(
        state = TierEmail.Visible.Error(MembershipErrors.GetVerificationEmail.EmailWrongFormat("error")),
        anyEmailTextField = TextFieldState(initialText = "")
    )
}