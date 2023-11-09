package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.ui.onboarding.OnboardingInput


@Composable
fun SetProfileNameWrapper(viewModel: OnboardingSetProfileNameViewModel) {
    SetProfileNameScreen(
        onNextClicked = viewModel::onNextClicked,
        isLoading = viewModel.state
            .collectAsStateWithLifecycle()
            .value is OnboardingSetProfileNameViewModel.ScreenState.Loading
    )
}

@Composable
private fun SetProfileNameScreen(
    onNextClicked: (Name) -> Unit,
    isLoading: Boolean
) {
    val text = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(
                modifier = Modifier.height(148.dp)
            )
            SetProfileNameTitle(modifier = Modifier.padding(bottom = 12.dp))
            SetProfileNameDescription()
            Spacer(modifier = Modifier.height(16.dp))
            SetProfileNameInput(
                text = text,
                onKeyboardActionDoneClicked = { onNextClicked(text.value) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            item {
//                Spacer(
//                    modifier = Modifier.height(148.dp)
//                )
//            }
//            item {
//                SetProfileNameTitle(modifier = Modifier.padding(bottom = 12.dp))
//            }
//            item {
//                SetProfileNameDescription()
//            }
//            item {
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//            item {
//                SetProfileNameInput(
//                    text = text,
//                    onKeyboardActionDoneClicked = { onNextClicked(text.value) }
//                )
//            }
//            item {
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
        SetProfileNameNextButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            ,
            onNextClicked = onNextClicked,
            text = text,
            isLoading = isLoading
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
            style = HeadlineHeading.copy(
                color = OnBoardingTextPrimaryColor
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetProfileNameInput(
    text: MutableState<String>,
    onKeyboardActionDoneClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        val focus = LocalFocusManager.current
        val focusRequester = FocusRequester()
        OnboardingInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            text = text,
            placeholder = stringResource(id = R.string.untitled),
            keyboardActions = KeyboardActions(
                onDone = {
                    focus.clearFocus()
                    onKeyboardActionDoneClicked()
                }
            )
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun SetProfileNameDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_soul_creation_description),
            style = HeadlineOnBoardingDescription.copy(
                color = OnBoardingTextSecondaryColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun SetProfileNameNextButton(
    modifier: Modifier,
    onNextClicked: (Name) -> Unit,
    text: MutableState<String>,
    isLoading: Boolean
) {
    val focus = LocalFocusManager.current
    Box(modifier = modifier) {
        OnBoardingButtonPrimary(
            text = stringResource(id = R.string.next),
            onClick = {
                onNextClicked(text.value).also {
                    focus.clearFocus(force = true)
                }
            },
            size = ButtonSize.Large,
            modifier = modifier,
            isLoading = isLoading
        )
    }
}