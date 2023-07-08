package com.anytypeio.anytype.ui.onboarding.screens.signup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
import com.anytypeio.anytype.presentation.common.ScreenState


@Composable
fun VoidScreenWrapper(
    screenState: ScreenState,
    contentPaddingTop: Int,
    onNextClicked: () -> Unit
) {
    VoidScreen(
        screenState = screenState,
        onNextClicked = onNextClicked,
        contentPaddingTop = contentPaddingTop
    )
}

@Composable
fun VoidScreen(
    screenState: ScreenState,
    onNextClicked: () -> Unit,
    contentPaddingTop: Int
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(contentPaddingTop.dp))
            VoidTitle()
            Spacer(modifier = Modifier.height(12.dp))
            VoidDescription()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .align(Alignment.BottomCenter)
        ) {
            OnBoardingButtonPrimary(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.next),
                onClick = { onNextClicked() },
                size = ButtonSize.Large
            )
            if (screenState is ScreenState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .size(18.dp),
                    color = colorResource(R.color.shape_secondary),
                    strokeWidth = 1.5.dp
                )
            }
        }
    }
}

@Composable
private fun VoidTitle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.onboarding_void_title),
            style = Title1.copy(
                color = OnBoardingTextPrimaryColor
            )
        )
    }
}

@Composable
private fun VoidDescription() {
    Text(
        modifier = Modifier.padding(start = 40.dp, end = 40.dp),
        text = stringResource(id = R.string.onboarding_void_description),
        style = HeadlineOnBoardingDescription.copy(
            color = OnBoardingTextSecondaryColor,
            textAlign = TextAlign.Center
        )
    )
}