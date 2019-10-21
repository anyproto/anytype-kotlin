package com.agileburo.anytype.presentation.navigation

interface AppNavigation {
    fun startLogin()
    fun createProfile()
    fun enterKeychain()
    fun choosePinCode()
    fun confirmPinCode(pin: String)
    fun setupNewAccount()
    fun setupSelectedAccount(id: String)
    fun congratulation()
    fun chooseProfile()
    fun workspace()
    fun openProfile()
    fun openDocument(id: String)

    sealed class Command {
        object OpenStartLoginScreen : Command()
        object OpenCreateProfile : Command()
        object ChoosePinCodeScreen : Command()
        object SetupNewAccountScreen : Command()
        data class SetupSelectedAccountScreen(val id: String) : Command()
        data class ConfirmPinCodeScreen(val code: String) : Command()
        object CongratulationScreen : Command()
        object ChooseProfileScreen : Command()
        object EnterKeyChainScreen : Command()
        object WorkspaceScreen : Command()
        data class OpenDocument(val id: String) : Command()
        object OpenProfile : Command()
        object OpenKeychainScreen : Command()
        object OpenPinCodeScreen : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}