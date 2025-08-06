package com.anytypeio.anytype.ui.onboarding.screens.signup

import android.util.Patterns
import androidx.compose.foundation.Image
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel

@Composable
fun SetEmailWrapper(
    viewModel: OnboardingSetProfileNameViewModel,
    startingObject: String?,
    space: Id,
    onBackClicked: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.sendAnalyticsOnboardingEmailScreen()
    }
    
    OnboardingEmailScreen(
        onContinueClicked = { email ->
            viewModel.onEmailContinueClicked(
                space = space,
                startingObject = startingObject,
                email = email
            )
        },
        onSkipClicked = {
            viewModel.onEmailSkippedClicked(
                space = space,
                startingObject = startingObject
            )
        },
        isLoading = viewModel.state
            .collectAsStateWithLifecycle()
            .value is OnboardingSetProfileNameViewModel.ScreenState.Loading,
        onBackClicked = onBackClicked
    )
}

@Composable
fun OnboardingEmailScreen(
    onContinueClicked: (String) -> Unit,
    onSkipClicked: () -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean
) {
    var innerValue by remember { mutableStateOf(TextFieldValue()) }
    var isError by remember { mutableStateOf(false) }
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
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.onboarding_email_add_title),
                color = colorResource(id = R.color.text_white),
                style = HeadlineTitleSemibold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.onboarding_email_add_description),
                style = UXBody,
                color = colorResource(id = R.color.text_white),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = innerValue,
                onValueChange = { input ->
                    innerValue = input
                    isError = false
                },
                shape = RoundedCornerShape(size = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.onboarding_enter_email),
                        style = PreviewTitle1Regular,
                        color = Color(0xFF646464)
                    )
                },
                textStyle = PreviewTitle1Regular.copy(
                    color = Color(0xFFC2C2C2)
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
                    cursorColor = Color(0xFFC2C2C2),
                    focusedContainerColor = Color(0xFF212121),
                    unfocusedContainerColor = Color(0xFF212121),
                    errorContainerColor = Color(0xFF212121),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions {
                    validateAndSubmit()
                }
            )
        }
        Image(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 9.dp)
                .noRippleClickable {
                    onBackClicked()
                },
            painter = painterResource(id = R.drawable.ic_back_onboarding_32),
            contentDescription = stringResource(R.string.content_description_back_button_icon)
        )
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
            OnBoardingButtonPrimary(
                text = stringResource(id = R.string.onboarding_button_continue),
                onClick = {
                    validateAndSubmit()
                },
                size = ButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoading,
                enabled = innerValue.text.isNotEmpty()
            )
            if (!BuildConfig.MANDATORY_EMAIL_COLLECTION) {
                Spacer(modifier = Modifier.height(8.dp))
                OnBoardingButtonSecondary(
                    text = stringResource(id = R.string.onboarding_button_skip),
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onSkipClicked()
                    },
                    textColor = colorResource(id = R.color.text_white),
                    size = ButtonSize.Large,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
private fun SetProfileNameScreenPreview() {
    OnboardingEmailScreen(
        onContinueClicked = {},
        onBackClicked = {},
        onSkipClicked = {},
        isLoading = false
    )
}