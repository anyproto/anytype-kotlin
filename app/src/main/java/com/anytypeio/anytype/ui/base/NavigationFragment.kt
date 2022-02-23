package com.anytypeio.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.AppNavigation.Command
import timber.log.Timber

abstract class NavigationFragment<BINDING : ViewBinding>(
    @LayoutRes private val layout: Int
) : BaseFragment<BINDING>(layout) {

    val navObserver = Observer<EventWrapper<Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    fun navigate(command: Command) {

        val navigation = (requireActivity() as AppNavigation.Provider).nav()

        when (command) {
            is Command.StartSplashFromDesktop -> navigation.startSplashFromDesktop()
            is Command.StartDesktopFromLogin -> navigation.startDesktopFromLogin()
            is Command.StartDesktopFromSignUp -> navigation.startDesktopFromSignUp()
            is Command.StartDesktopFromSplash -> navigation.startDesktopFromSplash()
            is Command.OpenStartLoginScreen -> navigation.startLogin()
            is Command.OpenCreateAccount -> navigation.createProfile(command.invitationCode)
            is Command.ChoosePinCodeScreen -> navigation.choosePinCode()
            is Command.EnterKeyChainScreen -> navigation.enterKeychain()
            is Command.SelectAccountScreen -> navigation.chooseAccount()
            is Command.WorkspaceScreen -> navigation.workspace()
            is Command.InvitationCodeScreen -> navigation.enterInvitationCode()
            is Command.AboutAnalyticsScreen -> navigation.aboutAnalyticsScreen()
            is Command.ExitToInvitationCodeScreen -> navigation.exitToInvitationCodeScreen()
            is Command.SetupNewAccountScreen -> navigation.setupNewAccount()
            is Command.SetupSelectedAccountScreen -> navigation.setupSelectedAccount(command.id)
            is Command.ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
            is Command.OpenSettings -> navigation.openSettings()
            is Command.OpenObject -> navigation.openDocument(command.id, command.editorSettings)
            is Command.OpenArchive -> navigation.openArchive(command.target)
            is Command.OpenObjectSet -> navigation.openObjectSet(command.target)
            is Command.LaunchObjectSet -> navigation.launchObjectSet(command.target)
            is Command.LaunchDocument -> navigation.launchDocument(command.id)
            is Command.LaunchObjectFromSplash -> navigation.launchObjectFromSplash(command.target)
            is Command.LaunchObjectSetFromSplash -> navigation.launchObjectSetFromSplash(command.target)
            is Command.OpenDatabaseViewAddView -> navigation.openDatabaseViewAddView()
            is Command.OpenEditDatabase -> navigation.openEditDatabase()
            is Command.OpenKeychainScreen -> navigation.openKeychainScreen()
            is Command.OpenUserSettingsScreen -> navigation.openUserSettingsScreen()
            is Command.OpenContactsScreen -> navigation.openContacts()
            is Command.OpenSwitchDisplayView -> navigation.openSwitchDisplayView()
            is Command.OpenCustomizeDisplayView -> navigation.openCustomizeDisplayView()
            is Command.Exit -> navigation.exit()
            is Command.ExitToDesktop -> navigation.exitToDesktop()
            is Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
            is Command.OpenPageNavigationScreen -> navigation.openPageNavigation(command.target)
            is Command.ExitToDesktopAndOpenPage -> navigation.exitToDesktopAndOpenPage(command.pageId)
            is Command.OpenPageSearch -> navigation.openPageSearch()
            is Command.OpenCreateSetScreen -> navigation.openCreateSetScreen(command.ctx)
            is Command.OpenUpdateAppScreen -> navigation.openUpdateAppScreen()
            else -> Timber.d("Nav command ignored: $command")
        }
    }
}
