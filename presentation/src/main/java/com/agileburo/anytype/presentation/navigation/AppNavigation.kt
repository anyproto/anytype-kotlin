package com.agileburo.anytype.presentation.navigation

import com.agileburo.anytype.presentation.settings.EditorSettings

interface AppNavigation {

    fun startLogin()
    fun createProfile(invitationCode: String)
    fun enterKeychain()
    fun choosePinCode()
    fun confirmPinCode(pin: String)
    fun enterInvitationCode()
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
    fun openLinkTo(target: String, context: String, replace: Boolean)
    fun openMoveTo(targets: List<String>, context: String)
    fun openPageSearch()
    fun exitToDesktopAndOpenPage(pageId: String)
    fun exitToInvitationCodeScreen()

    sealed class Command {

        object Exit : Command()
        object ExitToDesktop : Command()

        object OpenStartLoginScreen : Command()
        data class OpenCreateAccount(val invitationCode: String) : Command()
        object ChoosePinCodeScreen : Command()
        object InvitationCodeScreen : Command()
        object ExitToInvitationCodeScreen : Command()
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

        data class OpenLinkToScreen(
            val context: String,
            val target: String,
            val replace: Boolean
        ) : Command()

        data class OpenMoveToScreen(
            val context: String,
            val targets: List<String>
        ) : Command()

        data class ExitToDesktopAndOpenPage(val pageId: String) : Command()
        object OpenPageSearch: Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
