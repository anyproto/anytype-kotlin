package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel.SetupState

@DefaultPreviews
@Composable
private fun RecoveryScreenPreview() {
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

@DefaultPreviews
@Composable
private fun RecoveryScreenLoadingPreview() {
    RecoveryScreen(
        onBackClicked = {},
        onNextClicked = {},
        onActionDoneClicked = {},
        onScanQrClicked = {},
        state = SetupState.InProgress,
        onEnterMyVaultClicked = {},
        onDebugAccountTraceClicked = {},
        onRetryMigrationClicked = {},
        onStartMigrationClicked = {}
    )
}