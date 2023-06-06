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
import com.anytypeio.anytype.ui.onboarding.OnboardingInput
import com.anytypeio.anytype.ui.onboarding.OnboardingSoulCreationViewModel


@Composable
fun CreateSoulWrapper(viewModel: OnboardingSoulCreationViewModel) {
    CreateSoulScreen {
        viewModel.setAccountAndSpaceName(it)
    }
}

@Composable
private fun CreateSoulScreen(
    onCreateSoulClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        val text = remember { mutableStateOf("") }
        CreateSoulTitle(modifier = Modifier.padding(bottom = 16.dp))
        CreateSoulInput(text)
        Spacer(modifier = Modifier.height(9.dp))
        CreateSoulDescription()
        Spacer(modifier = Modifier.height(18.dp))
        CreateSoulNextButton(onCreateSoulClicked, text)
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
fun CreateSoulInput(text: MutableState<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        OnboardingInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = text,
            placeholder = stringResource(id = R.string.onboarding_soul_creation_placeholder)
        )
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
    onCreateSoulEntered: (String) -> Unit,
    text: MutableState<String>
) {
    OnBoardingButtonPrimary(
        text = stringResource(id = R.string.next),
        onClick = {
            onCreateSoulEntered.invoke(text.value)
        },
        size = ButtonSize.Large,
        enabled = text.value.trim().isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}