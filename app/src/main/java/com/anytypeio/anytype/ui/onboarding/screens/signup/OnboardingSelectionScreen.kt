package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary

@Composable
fun OnboardingSelectionScreen(
    isLoading: Boolean,
    onBackClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top toolbar with back button - same pattern as SetEmailWrapper
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

        // Placeholder content - you can add your selection UI here
        Text(
            text = "OnboardingSelectionScreen",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
            color = colorResource(id = R.color.text_primary)
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
            ButtonOnboardingPrimaryLarge(
                text = stringResource(id = R.string.onboarding_button_continue),
                onClick = {
                    //validateAndSubmit()
                },
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth(),
                loading = isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OnBoardingButtonSecondary(
                text = stringResource(id = R.string.onboarding_button_skip),
                onClick = {

                },
                size = ButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun OnboardingSelectionScreenPreview() {
    OnboardingSelectionScreen(
        isLoading = false,
        onBackClicked = {}
    )
}