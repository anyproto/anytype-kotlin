package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.ProfileIconView


@Composable
fun SetProfileNameWrapper(
    viewModel: OnboardingSetProfileNameViewModel,
    spaceId: String,
    startingObjectId: String?,
    profileId: String,
    onBackClicked: () -> Unit,
) {
    val name = remember { mutableStateOf("") }
    val placeholderName = viewModel.profileView.collectAsStateWithLifecycle().value

    SetProfileNameScreen(
        placeholderName = placeholderName,
        onNextClicked = { inputName ->
            name.value = inputName
            viewModel.onNextClicked(
                name = inputName,
                spaceId = spaceId,
                startingObjectId = startingObjectId,
                profileId = profileId
            )
        },
        isLoading = viewModel.state
            .collectAsStateWithLifecycle()
            .value is OnboardingSetProfileNameViewModel.ScreenState.Loading,
        onBackClicked = onBackClicked
    )
}

@Composable
private fun SetProfileNameScreen(
    placeholderName: AccountProfile,
    onNextClicked: (Name) -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean
) {
    var innerValue by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isError by remember { mutableStateOf(false) }
    val placeholderText = (placeholderName as? AccountProfile.Data)?.name ?: stringResource(id = R.string.onboarding_your_name)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun submit() {
        isError = false
        focusManager.clearFocus()
        keyboardController?.hide()
        onNextClicked(innerValue.text)
    }

    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
            .fillMaxSize()
    ) {
        Column {
            Spacer(
                modifier = Modifier.height(140.dp)
            )
            SetProfileNameTitle(modifier = Modifier.padding(bottom = 12.dp))
            SetProfileNameDescription()
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
                        text = placeholderText,
                        style = PreviewTitle1Regular,
                        color = Color(0xFF646464)
                    )
                },
                supportingText = {
                    if (isError) {
                        Text(
                            text = stringResource(id = R.string.onboarding_name_error),
                            color = colorResource(id = R.color.palette_system_red),
                            style = Caption1Regular
                        )
                    }
                },
                textStyle = PreviewTitle1Regular.copy(
                    color = Color(0xFFC2C2C2)
                ),
                singleLine = true,
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
                    submit()
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        Image(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .noRippleClickable {
                    focusManager.clearFocus()
                    onBackClicked()
                },
            painter = painterResource(id = R.drawable.ic_back_24),
            contentDescription = stringResource(R.string.content_description_back_button_icon)
        )
        OnBoardingButtonPrimary(
            text = stringResource(id = R.string.onboarding_button_continue),
            onClick = {
                submit()
            },
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .align(Alignment.BottomCenter),
            isLoading = isLoading,
            enabled = true
        )
    }
}

@Composable
fun SetProfileNameTitle(modifier: Modifier) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ), contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.onboarding_set_your_name_title),
            style = HeadlineTitleSemibold,
            color = colorResource(id = R.color.text_white)
        )
    }
}

@Composable
fun SetProfileNameDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_soul_creation_description),
            style = UXBody,
            color = colorResource(id = R.color.text_white),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    showSystemUi = true,
    device = PIXEL_7
)
@Composable
private fun SetProfileNameScreenPreview() {
    SetProfileNameScreen(
        onNextClicked = {},
        onBackClicked = {},
        isLoading = false,
        placeholderName = AccountProfile.Data(
            name = "Funny Name",
            icon = ProfileIconView.Loading
        )
    )
}