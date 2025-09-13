package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel.SetupState
import com.anytypeio.anytype.ui.onboarding.OnboardingMnemonicInput
import com.anytypeio.anytype.ui.update.MigrationFailedScreen
import com.anytypeio.anytype.ui.update.MigrationInProgressScreen
import com.anytypeio.anytype.ui.update.MigrationStartScreen


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
                        focus.clearFocus()
                        onBackClicked()
                    },
                painter = painterResource(id = R.drawable.ic_back_24),
                contentDescription = "Back button"
            )
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_anytype_logo),
                contentDescription = "Anytype logo",
            )
            if (BuildConfig.DEBUG) {
                Image(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 16.dp)
                        .align(Alignment.CenterEnd)
                        .clickable {
                            onDebugAccountTraceClicked()
                        },
                    painter = painterResource(R.drawable.ic_vault_settings),
                    contentDescription = "Debug account select command"
                )
            }
        }

        val emptyRecoveryPhraseError = stringResource(R.string.onboarding_your_key_can_t_be_empty)

        LazyColumn(
            modifier = Modifier.padding(top = 72.dp),
            content = {
                item {
                    OnboardingMnemonicInput(
                        modifier = Modifier
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                bottom = 20.dp
                            )
                            .height(106.dp)
                            .fillMaxWidth()
                            .background(
                                color = colorResource(R.color.shape_transparent_secondary),
                                shape = RoundedCornerShape(16.dp)
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
                    ButtonOnboardingPrimaryLarge(
                        text = stringResource(id = R.string.onboarding_enter_my_vault),
                        onClick = {
                            onNextClicked.invoke(text.value).also {
                                focus.clearFocus()
                            }
                        },
                        enabled = text.value.isNotEmpty(),
                        size = ButtonSize.Large,
                        modifierBox = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth(),
                        loading = state is SetupState.InProgress,
                    )
                }
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.onboarding_login_or),
                        style = Caption1Regular.copy(
                            color = colorResource(R.color.text_secondary)
                        )
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )  {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    shape = CircleShape,
                                    color = colorResource(id = R.color.shape_transparent_secondary)
                                )
                                .noRippleThrottledClickable {
                                    if (state !is SetupState.InProgress) {
                                        onScanQrClicked.invoke()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.ic_qr_code_24),
                                contentDescription = stringResource(R.string.content_description_qr_image)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            modifier = Modifier,
                            text = stringResource(R.string.onboarding_login_qr),
                            style = Caption2Regular,
                            color = colorResource(id = R.color.text_primary),
                        )
                    }
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
                    MigrationInProgressScreen(progress = state.progress.progress)
                }
                is SetupState.Migration.AwaitingStart -> {
                    MigrationStartScreen(
                        onStartUpdate = {
                            onStartMigrationClicked(state.account)
                        }
                    )
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun PreviewRecoveryScreen() {
    Column {
        Spacer(modifier = Modifier.height(40.dp))
        RecoveryScreen(
            onBackClicked = {},
            onNextClicked = {},
            onActionDoneClicked = {},
            onScanQrClicked = {},
            state = SetupState.Idle,
            onEnterMyVaultClicked = {},
            onDebugAccountTraceClicked = {},
            onRetryMigrationClicked = {},
            onStartMigrationClicked = {}
        )
    }
}

typealias Mnemonic = String