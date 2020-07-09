package com.agileburo.anytype.presentation.navigation

import com.agileburo.anytype.presentation.settings.EditorSettings

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
    fun openDocument(id: String, editorSettings: EditorSettings?)
    fun startDesktopFromSplash()
    fun startDesktopFromLogin()
    fun startSplashFromDesktop()
    fun openKeychainScreen()
    fun openContacts()
    fun openDatabaseViewAddView()
    fun openEditDatabase()
    fun openSwitchDisplayView()
    fun openCustomizeDisplayView()
    fun openKanban()
    fun openGoals()
    fun exit()
    fun exitToDesktop()
    fun openDebugSettings()
    fun openPageNavigation(target: String)
    fun exitToDesktopAndOpenPage(pageId: String)

    sealed class Command {

        object Exit : Command()
        object ExitToDesktop : Command()

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
        data class OpenPage(val id: String, val editorSettings: EditorSettings? = null) : Command()
        object OpenProfile : Command()
        object OpenKeychainScreen : Command()
        object OpenPinCodeScreen : Command()
        object StartDesktopFromSplash : Command()
        object StartDesktopFromLogin : Command()
        object StartSplashFromDesktop : Command()
        object OpenContactsScreen : Command()
        object OpenDatabaseViewAddView : Command()
        object OpenEditDatabase : Command()
        object OpenSwitchDisplayView : Command()
        object OpenCustomizeDisplayView : Command()
        object OpenKanbanScreen : Command()
        object OpenGoalsScreen : Command()
        object OpenDebugSettingsScreen: Command()
        data class OpenPageNavigationScreen(val target: String) : Command()
        data class ExitToDesktopAndOpenPage(val pageId: String) : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}