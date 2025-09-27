package com.anytypeio.anytype.ui.onboarding.screens.signup

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingLinkLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingEmailAndSelectionViewModel

@Composable
fun SetEmailWrapper(
    viewModel: OnboardingEmailAndSelectionViewModel,
    startingObject: String?,
    space: Id,
    onBackClicked: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.sendAnalyticsOnboardingEmailScreen()
    }
    
    OnboardingEmailScreen(
        onContinueClicked = { email ->
            viewModel.onEmailContinueButtonClicked(
                space = space,
                startingObject = startingObject,
                email = email
            )
        },
        onSkipClicked = {
            viewModel.onEmailSkippedButtonClicked(
                space = space,
                startingObject = startingObject
            )
        },
        isLoading = viewModel.state
            .collectAsStateWithLifecycle()
            .value is OnboardingEmailAndSelectionViewModel.ScreenState.Loading,
        onBackClicked = onBackClicked
    )
}

@Composable
private fun OnboardingEmailScreen(
    onContinueClicked: (String) -> Unit,
    onSkipClicked: () -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean
) {
    var innerValue by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateAndSubmit() {
        if (innerValue.text.isEmpty()) {
            isError = false
            return
        }
        if (isValidEmail(innerValue.text)) {
            isError = false
            focusManager.clearFocus()
            keyboardController?.hide()
            onContinueClicked(innerValue.text)
        } else {
            isError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Image(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                        .noRippleClickable {
                            onBackClicked()
                        },
                    painter = painterResource(id = R.drawable.ic_back_24),
                    contentDescription = stringResource(R.string.content_description_back_button_icon)
                )
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_anytype_logo),
                    contentDescription = "Anytype logo",
                )
            }
            Spacer(modifier = Modifier.height(84.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.onboarding_email_add_title),
                    color = colorResource(id = R.color.text_primary),
                    style = HeadlineTitleSemibold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.48).sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.onboarding_email_add_description),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.text_secondary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    enabled = !isLoading,
                    value = innerValue,
                    onValueChange = { input ->
                        innerValue = input
                        isError = false
                    },
                    shape = RoundedCornerShape(size = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    placeholder = {
                        if (innerValue.text.isEmpty() && !isFocused) {
                            Text(
                                text = stringResource(id = R.string.onboarding_enter_email),
                                style = PreviewTitle1Regular,
                                color = colorResource(id = R.color.text_tertiary)
                            )
                        }
                    },
                    textStyle = PreviewTitle1Regular.copy(
                        color = colorResource(id = R.color.text_primary)
                    ),
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = stringResource(id = R.string.onboarding_email_error),
                                color = colorResource(id = R.color.palette_system_red),
                                style = Caption1Regular
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = colorResource(id = R.color.text_primary),
                        cursorColor = colorResource(id = R.color.color_accent),
                        focusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        unfocusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        errorContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        validateAndSubmit()
                    }
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .align(Alignment.BottomCenter)
        ) {
            ButtonOnboardingPrimaryLarge(
                text = stringResource(id = R.string.onboarding_button_continue),
                onClick = {
                    validateAndSubmit()
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
                loading = isLoading,
                enabled = innerValue.text.isNotEmpty()
            )
            if (!BuildConfig.MANDATORY_EMAIL_COLLECTION) {
                Spacer(modifier = Modifier.height(8.dp))
                ButtonOnboardingLinkLarge(
                    text = stringResource(id = R.string.onboarding_button_skip),
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSkipClicked()
                    },
                    enabled = !isLoading,
                    size = ButtonSize.Large,
                    modifierBox = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
private fun SetProfileNameScreenPreview() {
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        OnboardingEmailScreen(
            onContinueClicked = {},
            onBackClicked = {},
            onSkipClicked = {},
            isLoading = false
        )
    }
}