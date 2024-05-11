package com.anytypeio.anytype.payments.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.payments.R
import com.anytypeio.anytype.payments.models.TierAnyName

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnyNameView(
    anyNameState: TierAnyName,
    anyNameTextField: TextFieldState
) {

    if (anyNameState != TierAnyName.Hidden) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val anyNameEnabled = remember { mutableStateOf(false) }

        anyNameEnabled.value = when (anyNameState) {
            TierAnyName.Hidden -> false
            TierAnyName.Visible.Disabled -> false
            TierAnyName.Visible.Enter -> true
            is TierAnyName.Visible.Error -> true
            is TierAnyName.Visible.Validated -> true
            TierAnyName.Visible.Validating -> true
            is TierAnyName.Visible.ErrorOther -> true
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
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.payments_tier_details_name_title),
                color = colorResource(id = R.color.text_primary),
                style = BodyBold,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.payments_tier_details_name_subtitle),
                color = colorResource(id = R.color.text_primary),
                style = BodyCallout,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
            BasicTextField2(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .focusRequester(focusRequester),
                state = anyNameTextField,
                textStyle = BodyRegular.copy(color = colorResource(id = R.color.text_primary)),
                enabled = anyNameEnabled.value,
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
            Text(
                text = stringResource(id = R.string.payments_tier_details_name_domain),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        val (messageTextColor, messageText) = when (anyNameState) {
            TierAnyName.Hidden -> Color.Transparent to ""
            TierAnyName.Visible.Disabled -> colorResource(id = R.color.text_secondary) to stringResource(id = R.string.payments_tier_details_name_min)
            TierAnyName.Visible.Enter -> colorResource(id = R.color.text_secondary) to stringResource(id = R.string.payments_tier_details_name_min)
            is TierAnyName.Visible.Error -> ErrorMessage(anyNameState)
            is TierAnyName.Visible.Validated -> colorResource(id = R.color.palette_dark_lime) to stringResource(id = R.string.payments_tier_details_name_validated, anyNameState.validatedName)
            TierAnyName.Visible.Validating -> colorResource(id = R.color.palette_dark_orange) to stringResource(id = R.string.payments_tier_details_name_validating)
            is TierAnyName.Visible.ErrorOther -> colorResource(id = R.color.palette_system_red) to (anyNameState.message ?: stringResource(id = R.string.membership_any_name_unknown))
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
private fun ErrorMessage(state: TierAnyName.Visible.Error): Pair<Color, String> {
    val color = colorResource(id = R.color.palette_system_red)
    val res = when (state.membershipErrors) {
        is MembershipErrors.IsNameValid.BadInput -> R.string.membership_name_bad_input
        is MembershipErrors.IsNameValid.CacheError -> R.string.membership_name_cache_error
        is MembershipErrors.IsNameValid.CanNotConnect -> R.string.membership_name_cant_connect
        is MembershipErrors.IsNameValid.CanNotReserve -> R.string.membership_any_name_not_reserved
        is MembershipErrors.IsNameValid.HasInvalidChars -> R.string.membership_name_invalid_chars
        is MembershipErrors.IsNameValid.NotLoggedIn -> R.string.membership_name_not_logged
        is MembershipErrors.IsNameValid.Null -> R.string.membership_any_name_null_error
        is MembershipErrors.IsNameValid.PaymentNodeError -> R.string.membership_name_payment_node_error
        is MembershipErrors.IsNameValid.TierFeaturesNoName -> R.string.membership_name_tier_features_no_name
        is MembershipErrors.IsNameValid.TierNotFound -> R.string.membership_name_tier_not_found
        is MembershipErrors.IsNameValid.TooLong -> R.string.membership_name_too_long
        is MembershipErrors.IsNameValid.TooShort -> R.string.membership_name_too_short
        is MembershipErrors.IsNameValid.UnknownError -> R.string.membership_any_name_unknown
        else -> R.string.membership_any_name_unknown
    }
    return color to stringResource(id = res)
}