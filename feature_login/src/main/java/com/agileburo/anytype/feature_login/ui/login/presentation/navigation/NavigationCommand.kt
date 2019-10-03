package com.agileburo.anytype.feature_login.ui.login.presentation.navigation

sealed class NavigationCommand {
    object OpenCreateProfile : NavigationCommand()
    object ChoosePinCodeScreen : NavigationCommand()
    object SetupNewAccountScreen : NavigationCommand()
    data class SetupSelectedAccountScreen(val id: String) : NavigationCommand()
    data class ConfirmPinCodeScreen(val code: String) : NavigationCommand()
    object CongratulationScreen : NavigationCommand()
    object ChooseProfileScreen : NavigationCommand()
    object EnterKeyChainScreen : NavigationCommand()
    object WorkspaceScreen : NavigationCommand()
}