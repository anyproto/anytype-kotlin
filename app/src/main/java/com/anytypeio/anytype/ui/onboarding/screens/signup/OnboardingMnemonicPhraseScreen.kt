package com.anytypeio.anytype.ui.onboarding.screens.signup

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorBackgroundField
import com.anytypeio.anytype.core_ui.ColorButtonRegular
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.extensions.conditional
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingMnemonicViewModel
import com.anytypeio.anytype.ui.onboarding.MnemonicPhraseWidget
import com.anytypeio.anytype.ui.onboarding.MnemonicStub

@Composable
fun MnemonicPhraseScreenWrapper(
    viewModel: OnboardingMnemonicViewModel,
    onCheckLaterClicked: () -> Unit,
    copyMnemonicToClipboard: (String) -> Unit,
    contentPaddingTop: Int,
    vm: OnboardingMnemonicViewModel,
    mnemonicColorPalette: List<Color>
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    MnemonicPhraseScreen(
        state = state,
        reviewMnemonic = { viewModel.openMnemonic() },
        onCheckLaterClicked = onCheckLaterClicked,
        copyMnemonicToClipboard = copyMnemonicToClipboard,
        contentPaddingTop = contentPaddingTop,
        vm = vm,
        mnemonicColorPalette = mnemonicColorPalette
    )
}

@Composable
fun MnemonicPhraseScreen(
    state: OnboardingMnemonicViewModel.State,
    reviewMnemonic: () -> Unit,
    onCheckLaterClicked: () -> Unit,
    copyMnemonicToClipboard: (String) -> Unit,
    contentPaddingTop: Int,
    vm: OnboardingMnemonicViewModel,
    mnemonicColorPalette: List<Color>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(contentPaddingTop.dp))
            MnemonicTitle()
            MnemonicPhrase(
                state = state,
                copyMnemonicToClipboard = copyMnemonicToClipboard,
                mnemonicColorPalette = mnemonicColorPalette
            )
            MnemonicDescription()
        }
        MnemonicButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            openMnemonic = reviewMnemonic,
            onCheckLaterClicked = onCheckLaterClicked,
            state = state,
            vm = vm
        )
    }
}

@Composable
fun MnemonicButtons(
    modifier: Modifier = Modifier,
    openMnemonic: () -> Unit,
    onCheckLaterClicked: () -> Unit,
    state: OnboardingMnemonicViewModel.State,
    vm: OnboardingMnemonicViewModel
) {
    Column(modifier.wrapContentHeight()) {
        when (state) {
            is OnboardingMnemonicViewModel.State.MnemonicOpened -> {
                OnBoardingButtonPrimary(
                    text = stringResource(id = R.string.onboarding_mnemonic_key_saved),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    onClick = {
                        onCheckLaterClicked.invoke()
                    }, size = ButtonSize.Large
                )
            }
            else -> {
                OnBoardingButtonPrimary(
                    text = stringResource(id = R.string.onboarding_mnemonic_show_key),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        openMnemonic.invoke()
                    }, size = ButtonSize.Large
                )
                OnBoardingButtonSecondary(
                    text = stringResource(id = R.string.onboarding_mnemonic_check_later),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 14.dp,
                            end = 16.dp,
                            bottom = 24.dp
                        ),
                    onClick = {
                        onCheckLaterClicked.invoke()
                        vm.onCheckLaterClicked()
                    },
                    size = ButtonSize.Large,
                    textColor = ColorButtonRegular
                )
            }
        }
    }
}


@Composable
fun MnemonicTitle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .wrapContentHeight(), contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.onboarding_mnemonic_title),
            style = Title1.copy(
                color = OnBoardingTextPrimaryColor
            )
        )
    }
}

@Composable
fun MnemonicPhrase(
    state: OnboardingMnemonicViewModel.State,
    copyMnemonicToClipboard: (String) -> Unit,
    mnemonicColorPalette: List<Color>
) {
    when (state) {
        is OnboardingMnemonicViewModel.State.Idle -> {}
        else -> {
            Column(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(color = ColorBackgroundField, shape = RoundedCornerShape(24.dp))
                        .wrapContentHeight()
                ) {
                    if (Build.VERSION.SDK_INT <= VERSION_CODES.R && state is OnboardingMnemonicViewModel.State.Mnemonic) {
                        MnemonicStub()
                    } else {
                        MnemonicPhraseWidget(
                            modifier = Modifier
                                .fillMaxWidth()
                                .conditional(
                                    condition = state is OnboardingMnemonicViewModel.State.Mnemonic,
                                    positive = { blur(15.dp) }
                                )
                                .padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp
                                ),
                            mnemonic = state.mnemonicPhrase,
                            mnemonicColorPalette = mnemonicColorPalette
                        )
                    }
                }
                if (state is OnboardingMnemonicViewModel.State.MnemonicOpened) {
                    OnBoardingButtonSecondary(
                        text = stringResource(id = R.string.onboarding_mnemonic_copy),
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .padding(bottom = 12.dp),
                        onClick = {
                            copyMnemonicToClipboard.invoke(state.mnemonicPhrase)
                        },
                        size = ButtonSize.SmallSecondary,
                        textColor = ColorButtonRegular
                    )
                }
            }

        }
    }
}

@Composable
fun MnemonicDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_mnemonic_description),
            style = HeadlineOnBoardingDescription.copy(
                color = OnBoardingTextSecondaryColor,
                textAlign = TextAlign.Center
            )
        )
    }
}