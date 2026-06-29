package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.viewmodel.ActivateCodeState
import com.anytypeio.anytype.payments.viewmodel.TierAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActivateCodeScreen(
    state: ActivateCodeState,
    textField: TextFieldState,
    action: (TierAction) -> Unit,
    onDismiss: () -> Unit
) {
    if (state is ActivateCodeState.Visible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = colorResource(id = R.color.background_primary),
            content = {
                ActivateCodeContent(
                    state = state,
                    textField = textField,
                    action = action
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivateCodeContent(
    state: ActivateCodeState.Visible,
    textField: TextFieldState,
    action: (TierAction) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = state is ActivateCodeState.Visible.Loading

    // While submitting, drop focus and hide the keyboard (mirror EnterCodeScreen).
    LaunchedEffect(key1 = state) {
        if (state is ActivateCodeState.Visible.Loading) {
            keyboardController?.hide()
            focusManager.clearFocus(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(56.dp),
            painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_payment_code),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorResource(id = R.color.palette_system_teal))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.payments_activate_code_title),
            style = HeadlineSubheading,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.payments_activate_code_subtitle),
            style = BodyCallout,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        val canSubmit = textField.text.isNotBlank() && !isLoading

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    color = colorResource(id = R.color.shape_tertiary),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textField.text.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.payments_activate_code_hint),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                }
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = textField,
                    textStyle = BodyRegular.copy(color = colorResource(id = R.color.text_primary)),
                    enabled = !isLoading,
                    cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (canSubmit) {
                            action(TierAction.OnActivateCodeClicked(textField.text.toString()))
                        }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    interactionSource = remember { MutableInteractionSource() }
                )
            }
            if (textField.text.isNotEmpty() && !isLoading) {
                Image(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                        .noRippleThrottledClickable { textField.clearText() },
                    painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ci_close_circle),
                    contentDescription = "Clear code",
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.glyph_inactive))
                )
            }
        }

        val (messageColor, messageText) = when (state) {
            is ActivateCodeState.Visible.Error -> colorResource(id = R.color.palette_system_red) to
                    (state.codeError?.let { codeErrorMessage(it) }
                        ?: state.message
                        ?: stringResource(id = R.string.membership_any_name_unknown))
            else -> Color.Transparent to ""
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = messageText,
            style = Relations2,
            color = messageColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
        ButtonOnboardingPrimaryLarge(
            text = stringResource(id = R.string.payments_activate_code_button),
            size = ButtonSize.Large,
            modifierBox = Modifier.fillMaxWidth(),
            enabled = canSubmit,
            loading = isLoading,
            onClick = {
                action(TierAction.OnActivateCodeClicked(textField.text.toString()))
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun codeErrorMessage(error: MembershipErrors.CodeGetInfo): String {
    val res = when (error) {
        is MembershipErrors.CodeGetInfo.UnknownError -> R.string.membership_code_error_unknown
        is MembershipErrors.CodeGetInfo.BadInput -> R.string.membership_code_error_bad_input
        is MembershipErrors.CodeGetInfo.NotLoggedIn -> R.string.membership_code_error_not_logged_in
        is MembershipErrors.CodeGetInfo.PaymentNodeError -> R.string.membership_code_error_payment_node
        is MembershipErrors.CodeGetInfo.CodeNotFound -> R.string.membership_code_error_code_invalid
        is MembershipErrors.CodeGetInfo.CodeAlreadyUsed -> R.string.membership_code_error_already_member
        is MembershipErrors.CodeGetInfo.Null -> R.string.membership_code_error_unknown
    }
    return stringResource(id = res)
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun ActivateCodeDefaultPreview() {
    ActivateCodeContent(
        state = ActivateCodeState.Visible.Default,
        textField = TextFieldState(initialText = "KJ419-01091-13933-321ZV-ONYEE"),
        action = {}
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun ActivateCodeLoadingPreview() {
    ActivateCodeContent(
        state = ActivateCodeState.Visible.Loading,
        textField = TextFieldState(initialText = "KJ419-01091-13933-321ZV-ONYEE"),
        action = {}
    )
}
