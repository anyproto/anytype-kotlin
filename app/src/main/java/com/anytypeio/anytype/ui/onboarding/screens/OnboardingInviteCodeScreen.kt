package com.anytypeio.anytype.ui.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.ui.onboarding.OnboardingInput
import com.anytypeio.anytype.ui.onboarding.OnboardingInviteCodeViewModel

@Composable
fun InviteCodeScreenWrapper(
    viewModel: OnboardingInviteCodeViewModel,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    InviteCodeScreen(
        state = state,
        onInviteCodeEntered = {
            viewModel.onInviteCodeEntered(it)
        }
    )
}

@Composable
fun InviteCodeScreen(
    state: OnboardingInviteCodeViewModel.InviteCodeViewState,
    onInviteCodeEntered: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    when (state) {
        is OnboardingInviteCodeViewModel.InviteCodeViewState.WalletCreating -> {}
        else -> {
            val inviteCode = remember { mutableStateOf("") }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                InviteCodeTitle(modifier = Modifier.padding(bottom = 16.dp))
                InviteCodeInput(inviteCode, focusRequester)
                Spacer(modifier = Modifier.height(9.dp))
                InviteCodeDescription()
                Spacer(modifier = Modifier.height(18.dp))
                InviteCodeNextButton(
                    onInviteCodeEntered,
                    inviteCode
                )
            }
        }
    }
}

@Composable
fun InviteCodeTitle(modifier: Modifier) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ), contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.onboarding_invite_code_title),
            style = Title1.copy(
                color = OnBoardingTextPrimaryColor
            )
        )
    }
}

@Composable
fun InviteCodeInput(text: MutableState<String>, focusRequester: FocusRequester) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onGloballyPositioned {
                focusRequester.requestFocus()
            }
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        OnboardingInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = text,
            placeholder = ""
        )
    }
}

@Composable
fun InviteCodeDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_invite_code_description),
            style = HeadlineOnBoardingDescription.copy(
                color = OnBoardingTextSecondaryColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun InviteCodeNextButton(
    onInviteCodeEntered: (String) -> Unit,
    text: MutableState<String>
) {
    OnBoardingButtonPrimary(
        text = stringResource(id = R.string.next),
        onClick = {
            onInviteCodeEntered.invoke(text.value)
        },
        size = ButtonSize.Large,
        enabled = text.value.trim().isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}