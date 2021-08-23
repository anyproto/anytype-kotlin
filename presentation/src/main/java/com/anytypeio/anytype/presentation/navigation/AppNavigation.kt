package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.presentation.settings.EditorSettings

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

    fun openArchive(target: String)
    fun openObjectSet(target: String)
    fun openDocument(id: String, editorSettings: EditorSettings?)

    fun launchDocument(id: String)
    fun launchObjectSet(id: Id)

    fun startDesktopFromSplash()
    fun startDesktopFromLogin()
    fun startSplashFromDesktop()
    fun openKeychainScreen()
    fun openContacts()
    fun openDatabaseViewAddView()
    fun openEditDatabase()
    fun openSwitchDisplayView()
    fun openCustomizeDisplayView()
    fun openGoals()
    fun exit()
    fun exitToDesktop()
    fun openDebugSettings()
    fun openPageNavigation(target: String)
    fun openLinkTo(target: String, context: String, replace: Boolean, position: Position)
    fun openPageSearch()
    fun exitToDesktopAndOpenPage(pageId: String)
    fun exitToInvitationCodeScreen()
    fun openCreateSetScreen(ctx: Id)

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

        data class OpenObject(val id: String, val editorSettings: EditorSettings? = null) : Command()
        data class LaunchDocument(val id: String) : Command()

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
        object OpenDebugSettingsScreen: Command()

        data class OpenPageNavigationScreen(val target: String) : Command()

        data class OpenLinkToScreen(
            val context: String,
            val target: String,
            val replace: Boolean,
            val position: Position
        ) : Command()

        data class ExitToDesktopAndOpenPage(val pageId: String) : Command()
        object OpenPageSearch : Command()

        data class OpenArchive(val target: String) : Command()
        data class OpenObjectSet(val target: String) : Command()
        data class LaunchObjectSet(val target: Id) : Command()

        data class OpenCreateSetScreen(val ctx: Id) : Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
