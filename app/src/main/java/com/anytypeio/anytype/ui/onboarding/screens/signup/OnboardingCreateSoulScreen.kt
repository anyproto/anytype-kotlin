package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSoulCreationViewModel
import com.anytypeio.anytype.ui.onboarding.OnboardingInput


@Composable
fun CreateSoulWrapper(viewModel: OnboardingSoulCreationViewModel, contentPaddingTop: Int) {
    CreateSoulScreen(contentPaddingTop) {
        viewModel.setAccountAndSpaceName(it)
    }
}

@Composable
private fun CreateSoulScreen(
    contentPaddingTop: Int,
    onCreateSoulClicked: (String) -> Unit
) {
    val text = remember { mutableStateOf("") }
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val animatedSpacerHeight = animateDpAsState(
        targetValue = if (isKeyboardVisible)
            contentPaddingTop.dp - 72.dp
        else
            contentPaddingTop.dp
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Spacer(
                    modifier = Modifier.height(animatedSpacerHeight.value)
                )
            }
            item {
                CreateSoulTitle(modifier = Modifier.padding(bottom = 16.dp))
            }
            item {
                CreateSoulInput(
                    text = text,
                    onKeyboardActionDoneClicked = { onCreateSoulClicked(text.value) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(9.dp))
            }
            item {
                CreateSoulDescription()
            }
        }
        CreateSoulNextButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp)
                .then(
                    if (isKeyboardVisible)
                        Modifier.padding(bottom = 0.dp)
                    else
                        Modifier.padding(bottom = 13.dp)
                )
            ,
            onCreateSoulEntered = onCreateSoulClicked,
            text = text
        )
    }
}

@Composable
fun CreateSoulTitle(modifier: Modifier) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ), contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.onboarding_soul_creation_title),
            style = Title1.copy(
                color = OnBoardingTextPrimaryColor
            )
        )
    }
}

@Composable
fun CreateSoulInput(
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
        val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val focusRequester = FocusRequester()
        val context = LocalContext.current
        val emptyNameError = stringResource(R.string.name_is_required)

        OnboardingInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
            ,
            text = text,
            placeholder = stringResource(id = R.string.onboarding_soul_creation_placeholder),
            keyboardActions = KeyboardActions(
                onDone = {
                    val input = text.value
                    if (input.isNotEmpty()) {
                        focus.clearFocus()
                        onKeyboardActionDoneClicked()
                    } else {
                        context.toast(emptyNameError)
                    }
                }
            )
        )

        if (!isKeyboardVisible) {
            focus.clearFocus()
        }
        
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun CreateSoulDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
fun CreateSoulNextButton(
    modifier: Modifier,
    onCreateSoulEntered: (String) -> Unit,
    text: MutableState<String>
) {
    OnBoardingButtonPrimary(
        text = stringResource(id = R.string.go_to_the_app),
        onClick = {
            onCreateSoulEntered.invoke(text.value)
        },
        size = ButtonSize.Large,
        enabled = text.value.trim().isNotEmpty(),
        modifier = modifier
    )
}