package com.anytypeio.anytype.ui.onboarding.screens.signup

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.ColorBackgroundField
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.conditional
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonMedium
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingLinkLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingMnemonicViewModel
import com.anytypeio.anytype.ui.onboarding.MnemonicPhraseWidget
import com.anytypeio.anytype.ui.onboarding.MnemonicStub

@Composable
fun MnemonicPhraseScreenWrapper(
    space: Id,
    startingObject: Id?,
    copyMnemonicToClipboard: (String) -> Unit,
    vm: OnboardingMnemonicViewModel,
    mnemonicColorPalette: List<Color>,
    onBackClicked: () -> Unit
) {
    val state = vm.state.collectAsStateWithLifecycle().value
    MnemonicPhraseScreen(
        state = state,
        reviewMnemonic = { vm.openMnemonic() },
        onCheckLaterClicked = {
            vm.onCheckLaterClicked(
                space = space,
                startingObject = startingObject,
            )
        },
        copyMnemonicToClipboard = copyMnemonicToClipboard,
        mnemonicColorPalette = mnemonicColorPalette,
        onGoToAppClicked = {
            vm.handleAppEntryClick(
                space = space,
                startingObject = startingObject,
            )
        },
        onBackClicked = onBackClicked
    )
}


@DefaultPreviews
@Composable
fun PreviewMnemonicPhraseScreen() {
    val fakeMnemonic = "One Two Three Four Five Six Seven Eight Nine Ten Eleven Twelve"
    MnemonicPhraseScreen(
        state = OnboardingMnemonicViewModel.State.MnemonicOpened(fakeMnemonic),
        reviewMnemonic = { /*TODO*/ },
        onCheckLaterClicked = { /*TODO*/ },
        copyMnemonicToClipboard = {},
        mnemonicColorPalette = emptyList(),
        onGoToAppClicked = {},
        onBackClicked = { /*TODO*/ }
    )
}

@DefaultPreviews
@Composable
fun PreviewMnemonicPhraseScreen2() {
    val fakeMnemonic = "One Two Three Four Five Six Seven Eight Nine Ten Eleven Twelve"
    MnemonicPhraseScreen(
        state = OnboardingMnemonicViewModel.State.Mnemonic(fakeMnemonic),
        reviewMnemonic = { /*TODO*/ },
        onCheckLaterClicked = { /*TODO*/ },
        copyMnemonicToClipboard = {},
        mnemonicColorPalette = emptyList(),
        onGoToAppClicked = {},
        onBackClicked = { /*TODO*/ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MnemonicPhraseScreen(
    state: OnboardingMnemonicViewModel.State,
    reviewMnemonic: () -> Unit,
    onCheckLaterClicked: () -> Unit,
    onGoToAppClicked: () -> Unit,
    copyMnemonicToClipboard: (String) -> Unit,
    mnemonicColorPalette: List<Color>,
    onBackClicked: () -> Unit
) {
    val showWhatIsRecoveryPhraseDialog = remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
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
                MnemonicTitle()
                MnemonicDescription()
                Spacer(modifier = Modifier.height(31.dp))
                MnemonicPhrase(
                    state = state,
                    copyMnemonicToClipboard = copyMnemonicToClipboard,
                    mnemonicColorPalette = mnemonicColorPalette,
                    reviewMnemonic = reviewMnemonic
                )
                ReadMoreButton(showWhatIsRecoveryPhraseDialog)
            }
        }
        MnemonicButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            reviewMnemonic = reviewMnemonic,
            onCheckLaterClicked = onCheckLaterClicked,
            onGoToAppClicked = onGoToAppClicked,
            state = state,
            copyMnemonicToClipboard = copyMnemonicToClipboard
        )
    }
    if (showWhatIsRecoveryPhraseDialog.value) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
            containerColor = colorResource(id = R.color.background_secondary),
            onDismissRequest = {
                showWhatIsRecoveryPhraseDialog.value = false
            },
            content = { WhatIsRecoveryPhraseScreen() },
            dragHandle = {
                // Do nothing
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        )
    }
}

@Composable
private fun ColumnScope.ReadMoreButton(showWhatIsRecoveryPhraseDialog: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(top = 16.dp)
            .height(24.dp)
            .noRippleClickable {
                showWhatIsRecoveryPhraseDialog.value = true
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = R.drawable.ic_plus_18),
            contentDescription = "Read more about recovery phrase",
        )
        Text(
            text = stringResource(id = R.string.onboarding_mnemonic_read_more),
            style = ButtonMedium,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun MnemonicButtons(
    modifier: Modifier = Modifier,
    reviewMnemonic: () -> Unit,
    onCheckLaterClicked: () -> Unit,
    copyMnemonicToClipboard: (String) -> Unit,
    onGoToAppClicked: () -> Unit,
    state: OnboardingMnemonicViewModel.State
) {
    Column(modifier.wrapContentHeight()) {
        when (state) {
            is OnboardingMnemonicViewModel.State.MnemonicOpened -> {
                ButtonOnboardingPrimaryLarge(
                    text = stringResource(id = R.string.onboarding_enter_my_vault),
                    modifierBox = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    onClick = { onGoToAppClicked() },
                )
            }

            else -> {
                ButtonOnboardingPrimaryLarge(
                    text = stringResource(id = R.string.onboarding_tap_to_reveal),
                    modifierBox = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    onClick = {
                        reviewMnemonic.invoke().also {
                            if (state is OnboardingMnemonicViewModel.State.Mnemonic) {
                                copyMnemonicToClipboard.invoke(state.mnemonicPhrase)
                            }
                        }
                    }
                )
                ButtonOnboardingLinkLarge(
                    text = stringResource(id = R.string.onboarding_key_not_now),
                    modifierBox = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            top = 8.dp,
                            end = 20.dp,
                            bottom = 24.dp
                        ),
                    onClick = {
                        onCheckLaterClicked.invoke()
                    }
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
            text = stringResource(R.string.onboarding_this_is_your_key_title),
            style = HeadlineTitleSemibold,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MnemonicPhrase(
    state: OnboardingMnemonicViewModel.State,
    reviewMnemonic: () -> Unit,
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
                        .background(color = ColorBackgroundField, shape = RoundedCornerShape(16.dp))
                        .wrapContentHeight()
                        .noRippleThrottledClickable {
                            reviewMnemonic()
                            copyMnemonicToClipboard.invoke(state.mnemonicPhrase)
                        }
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
                                    start = 42.dp,
                                    top = 17.dp,
                                    end = 42.dp,
                                    bottom = 17.dp
                                ),
                            mnemonic = state.mnemonicPhrase,
                            mnemonicColorPalette = mnemonicColorPalette
                        )
                    }
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
            .padding(horizontal = 20.dp)
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_key_description),
            textAlign = TextAlign.Center,
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}