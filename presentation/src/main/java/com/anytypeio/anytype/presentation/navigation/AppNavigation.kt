package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.settings.EditorSettings
import com.anytypeio.anytype.presentation.widgets.collection.Subscription

interface AppNavigation {

    fun startLogin()
    fun createProfile(invitationCode: String)
    fun enterKeychain()
    fun choosePinCode()
    fun confirmPinCode(pin: String)
    fun enterInvitationCode()
    fun aboutAnalyticsScreen()
    fun setupNewAccount()
    fun setupSelectedAccount(id: String)
    fun chooseAccount()
    fun workspace()
    fun openSettings()

    fun openArchive(target: String)
    fun openObjectSet(target: String, isPopUpToDashboard: Boolean = false)
    fun openDocument(id: String, editorSettings: EditorSettings? = null)

    fun launchDocument(id: String)
    fun launchCollections(subscription: Subscription)
    fun launchObjectFromSplash(id: Id)
    fun launchObjectSetFromSplash(id: Id)
    fun launchObjectSet(id: Id)

    fun startDesktopFromSplash()
    fun startDesktopFromLogin()
    fun startDesktopFromSignUp()
    fun startSplashFromDesktop()
    fun openKeychainScreen()
    fun openUserSettingsScreen()
    fun openContacts()
    fun openDatabaseViewAddView()
    fun openEditDatabase()
    fun openSwitchDisplayView()
    fun openCustomizeDisplayView()
    fun exit()
    fun exitToDesktop()
    fun openDebugSettings()
    fun openPageSearch()
    fun exitToDesktopAndOpenPage(pageId: String)
    fun exitToInvitationCodeScreen()
    fun openUpdateAppScreen()

    fun deletedAccountScreen(deadline: Long)

    fun openTemplates(
        ctx: Id,
        type: String,
        templates: List<Id>
    )

    fun openLibrary()

    fun logout()

    fun migrationErrorScreen()

    sealed class Command {

        object Exit : Command()
        object ExitToDesktop : Command()

        object OpenStartLoginScreen : Command()
        data class OpenCreateAccount(val invitationCode: String) : Command()
        object ChoosePinCodeScreen : Command()
        object InvitationCodeScreen : Command()
        object AboutAnalyticsScreen : Command()
        object ExitToInvitationCodeScreen : Command()
        object SetupNewAccountScreen : Command()
        data class SetupSelectedAccountScreen(val id: String) : Command()
        data class ConfirmPinCodeScreen(val code: String) : Command()
        object SelectAccountScreen : Command()
        object EnterKeyChainScreen : Command()
        object WorkspaceScreen : Command()

        data class OpenObject(val id: String, val editorSettings: EditorSettings? = null) :
            Command()

        data class LaunchDocument(val id: String) : Command()
        data class LaunchObjectFromSplash(val target: Id) : Command()
        data class LaunchObjectSetFromSplash(val target: Id) : Command()

        object OpenSettings : Command()
        object OpenKeychainScreen : Command()
        object OpenPinCodeScreen : Command()
        object OpenUserSettingsScreen : Command()
        object StartDesktopFromSplash : Command()
        object StartDesktopFromLogin : Command()
        object MigrationErrorScreen: Command()
        object StartDesktopFromSignUp : Command()
        object StartSplashFromDesktop : Command()
        object OpenContactsScreen : Command()
        object OpenDatabaseViewAddView : Command()
        object OpenEditDatabase : Command()
        object OpenSwitchDisplayView : Command()
        object OpenCustomizeDisplayView : Command()
        object OpenDebugSettingsScreen : Command()

        data class ExitToDesktopAndOpenPage(val pageId: String) : Command()
        object OpenPageSearch : Command()

        data class OpenArchive(val target: String) : Command()
        data class OpenObjectSet(val target: String, val isPopUpToDashboard: Boolean = false) :
            Command()

        data class LaunchObjectSet(val target: Id) : Command()

        object OpenUpdateAppScreen : Command()

        data class DeletedAccountScreen(val deadline: Long) : Command()

        data class OpenTemplates(
            val ctx: Id,
            val type: String,
            val templates: List<Id>
        ) : Command()

        object OpenLibrary: Command()
    }

    interface Provider {
        fun nav(): AppNavigation
    }
}
