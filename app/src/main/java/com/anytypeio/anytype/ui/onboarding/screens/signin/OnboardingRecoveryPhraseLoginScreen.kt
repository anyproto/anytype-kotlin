package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.ColorButtonRegular
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ConditionLogin
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.TitleLogin
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel.SetupState
import com.anytypeio.anytype.ui.onboarding.OnboardingMnemonicInput
import com.anytypeio.anytype.ui.update.MigrationFailedScreen
import com.anytypeio.anytype.ui.update.MigrationInProgressScreen
import com.anytypeio.anytype.ui.update.MigrationStartScreen
import kotlin.Unit

@Composable
fun RecoveryScreenWrapper(
    vm: OnboardingMnemonicLoginViewModel,
    onBackClicked: () -> Unit,
    onScanQrClick: () -> Unit
) {
    RecoveryScreen(
        onBackClicked = onBackClicked,
        onNextClicked = vm::onLoginClicked,
        onActionDoneClicked = vm::onActionDone,
        onScanQrClicked = onScanQrClick,
        state = vm.state.collectAsState().value,
        onEnterMyVaultClicked = vm::onEnterMyVaultClicked,
        onDebugAccountTraceClicked = {
            vm.onAccountThraceButtonClicked()
        },
        onRetryMigrationClicked = vm::onRetryMigrationClicked,
        onStartMigrationClicked = vm::onStartMigrationClicked
    )
}

@Composable
fun RecoveryScreen(
    onBackClicked: () -> Unit,
    onNextClicked: (Mnemonic) -> Unit,
    onActionDoneClicked: (Mnemonic) -> Unit,
    onScanQrClicked: () -> Unit,
    state: SetupState,
    onEnterMyVaultClicked: () -> Unit,
    onDebugAccountTraceClicked: () -> Unit,
    onRetryMigrationClicked: (Id) -> Unit,
    onStartMigrationClicked: (Id) -> Unit
) {
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val text = remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .padding(top = 12.dp, start = 9.dp)
                .noRippleClickable {
                    focus.clearFocus()
                    onBackClicked()
                },
            painter = painterResource(id = R.drawable.ic_back_onboarding_32),
            contentDescription = "Back button"
        )
        Text(
            modifier = Modifier
                .noRippleClickable { onEnterMyVaultClicked() }
                .align(Alignment.TopCenter)
                .padding(top = 17.dp, start = 18.dp, end = 18.dp)
            ,
            text = stringResource(id = R.string.onboarding_enter_my_vault),
            style = TitleLogin.copy(
                color = colorResource(id = R.color.text_white)
            )
        )
        if (BuildConfig.DEBUG) {
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
                    .clickable {
                        onDebugAccountTraceClicked()
                    },
                painter = painterResource(R.drawable.ic_vault_settings),
                contentDescription = "Debug account select command"
            )
        }

        val emptyRecoveryPhraseError = stringResource(R.string.onboarding_your_key_can_t_be_empty)

        LazyColumn(
            modifier = Modifier.padding(top = 71.dp),
            content = {
                item {
                    OnboardingMnemonicInput(
                        modifier = Modifier
                            .padding(
                                start = 18.dp,
                                end = 18.dp,
                                bottom = 18.dp
                            )
                            .height(165.dp)
                            .fillMaxWidth()
                            .background(
                                color = Color(0x26DAD7CA),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        ,
                        text = text,
                        singleLine = false,
                        placeholder = stringResource(id = R.string.onboarding_type_your_key),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            autoCorrect = false,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val input = text.value
                                if (input.isNotEmpty()) {
                                    onActionDoneClicked(text.value).also {
                                        focus.clearFocus()
                                    }
                                } else {
                                    context.toast(emptyRecoveryPhraseError)
                                }
                            }
                        )
                    )
                }
                item {
                    OnBoardingButtonPrimary(
                        text = stringResource(id = R.string.onboarding_enter_my_vault),
                        onClick = {
                            onNextClicked.invoke(text.value).also {
                                focus.clearFocus()
                            }
                        },
                        enabled = text.value.isNotEmpty(),
                        size = ButtonSize.Large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        isLoading = state is SetupState.InProgress
                    )
                }
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.onboarding_login_or),
                        style = ConditionLogin.copy(
                            color = OnBoardingTextPrimaryColor
                        )
                    )
                }
                item {
                    OnBoardingButtonSecondary(
                        text = stringResource(id = R.string.or_scan_qr_code),
                        onClick = {
                            onScanQrClicked.invoke()
                        },
                        enabled = state !is SetupState.InProgress,
                        disabledBackgroundColor = Color.Transparent,
                        size = ButtonSize.Large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
                        textColor = ColorButtonRegular
                    )
                }
            }
        )

        if (state is SetupState.Migration) {
            when(state) {
                is SetupState.Migration.Failed -> {
                    MigrationFailedScreen(
                        state = state.state,
                        onRetryClicked = {
                            onRetryMigrationClicked(state.account)
                        }
                    )
                }
                is SetupState.Migration.InProgress -> {
                    MigrationInProgressScreen()
                }
                is SetupState.Migration.AwaitingStart -> {
                    MigrationStartScreen(
                        onStartUpdate = {
                            onStartMigrationClicked(state.account)
                        },
                        onReadMore = {
                            // TODO show bottom sheet.
                        }
                    )
                }
            }
        }
    }
}

typealias Mnemonic = String