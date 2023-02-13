package com.anytypeio.anytype.ui.base

import androidx.fragment.app.Fragment
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import timber.log.Timber

class NavigationRouter(
    private val navigation: AppNavigation
) {
    fun navigate(command: AppNavigation.Command) {
        Timber.d("Navigate to $command")

        when (command) {
            is AppNavigation.Command.StartSplashFromDesktop -> navigation.startSplashFromDesktop()
            is AppNavigation.Command.StartDesktopFromLogin -> navigation.startDesktopFromLogin()
            is AppNavigation.Command.StartDesktopFromSignUp -> navigation.startDesktopFromSignUp()
            is AppNavigation.Command.StartDesktopFromSplash -> navigation.startDesktopFromSplash()
            is AppNavigation.Command.OpenStartLoginScreen -> navigation.startLogin()
            is AppNavigation.Command.OpenCreateAccount -> navigation.createProfile(command.invitationCode)
            is AppNavigation.Command.ChoosePinCodeScreen -> navigation.choosePinCode()
            is AppNavigation.Command.EnterKeyChainScreen -> navigation.enterKeychain()
            is AppNavigation.Command.SelectAccountScreen -> navigation.chooseAccount()
            is AppNavigation.Command.WorkspaceScreen -> navigation.workspace()
            is AppNavigation.Command.InvitationCodeScreen -> navigation.enterInvitationCode()
            is AppNavigation.Command.AboutAnalyticsScreen -> navigation.aboutAnalyticsScreen()
            is AppNavigation.Command.ExitToInvitationCodeScreen -> navigation.exitToInvitationCodeScreen()
            is AppNavigation.Command.SetupNewAccountScreen -> navigation.setupNewAccount()
            is AppNavigation.Command.SetupSelectedAccountScreen -> navigation.setupSelectedAccount(
                command.id
            )
            is AppNavigation.Command.ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
            is AppNavigation.Command.OpenSettings -> navigation.openSettings()
            is AppNavigation.Command.OpenObject -> navigation.openDocument(
                command.id,
                command.editorSettings
            )
            is AppNavigation.Command.OpenArchive -> navigation.openArchive(command.target)
            is AppNavigation.Command.OpenObjectSet -> navigation.openObjectSet(
                command.target,
                command.isPopUpToDashboard
            )
            is AppNavigation.Command.LaunchObjectSet -> navigation.launchObjectSet(command.target)
            is AppNavigation.Command.LaunchDocument -> navigation.launchDocument(command.id)
            is AppNavigation.Command.LaunchObjectFromSplash -> navigation.launchObjectFromSplash(
                command.target
            )
            is AppNavigation.Command.LaunchObjectSetFromSplash -> navigation.launchObjectSetFromSplash(
                command.target
            )
            is AppNavigation.Command.OpenDatabaseViewAddView -> navigation.openDatabaseViewAddView()
            is AppNavigation.Command.OpenEditDatabase -> navigation.openEditDatabase()
            is AppNavigation.Command.OpenKeychainScreen -> navigation.openKeychainScreen()
            is AppNavigation.Command.OpenUserSettingsScreen -> navigation.openUserSettingsScreen()
            is AppNavigation.Command.OpenContactsScreen -> navigation.openContacts()
            is AppNavigation.Command.OpenSwitchDisplayView -> navigation.openSwitchDisplayView()
            is AppNavigation.Command.OpenCustomizeDisplayView -> navigation.openCustomizeDisplayView()
            is AppNavigation.Command.Exit -> navigation.exit()
            is AppNavigation.Command.ExitToDesktop -> navigation.exitToDesktop()
            is AppNavigation.Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
            is AppNavigation.Command.OpenPageNavigationScreen -> navigation.openPageNavigation(
                command.target
            )
            is AppNavigation.Command.ExitToDesktopAndOpenPage -> navigation.exitToDesktopAndOpenPage(
                command.pageId
            )
            is AppNavigation.Command.OpenPageSearch -> navigation.openPageSearch()
            is AppNavigation.Command.OpenUpdateAppScreen -> navigation.openUpdateAppScreen()
            is AppNavigation.Command.DeletedAccountScreen -> navigation.deletedAccountScreen(command.deadline)
            is AppNavigation.Command.OpenTemplates -> navigation.openTemplates(
                ctx = command.ctx,
                type = command.type,
                templates = command.templates
            )
            is AppNavigation.Command.OpenLibrary -> navigation.openLibrary()
            else -> Timber.d("Nav command ignored: $command")
        }
    }
}

fun Fragment.navigation() = (requireActivity() as AppNavigation.Provider).nav()