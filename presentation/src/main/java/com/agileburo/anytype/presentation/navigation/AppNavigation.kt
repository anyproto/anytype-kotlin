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
    fun chooseAccount()
    fun workspace()
    fun openProfile()
    fun openDocument(id: String)
    fun startDesktopFromSplash()
    fun startDesktopFromLogin()
    fun startSplashFromDesktop()
    fun openKeychainScreen()
    fun openContacts()
    fun exit()

    sealed class Command {

        object Exit : Command()

        object OpenStartLoginScreen : Command()
        object OpenCreateAccount : Command()
        object ChoosePinCodeScreen : Command()
        object SetupNewAccountScreen : Command()
        data class SetupSelectedAccountScreen(val id: String) : Command()
        data class ConfirmPinCodeScreen(val code: String) : Command()
        object CongratulationScreen : Command()
        object SelectAccountScreen : Command()
        object EnterKeyChainScreen : Command()
        object WorkspaceScreen : Command()
        data class OpenDocument(val id: String) : Command()
        object OpenProfile : Command()
        object OpenKeychainScreen : Command()
        object OpenPinCodeScreen : Command()
        object StartDesktopFromSplash : Command()
        object StartDesktopFromLogin : Command()
        object StartSplashFromDesktop : Command()
        object OpenContactsScreen : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}