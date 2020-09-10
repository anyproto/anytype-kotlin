package com.agileburo.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.BaseFragment
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.AppNavigation.Command

abstract class NavigationFragment(
    @LayoutRes private val layout: Int
) : BaseFragment(layout) {

    val navObserver = Observer<EventWrapper<Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    private fun navigate(command: Command) {

        val navigation = (requireActivity() as AppNavigation.Provider).nav()

        when (command) {

            is Command.StartSplashFromDesktop -> navigation.startSplashFromDesktop()
            is Command.StartDesktopFromLogin -> navigation.startDesktopFromLogin()
            is Command.StartDesktopFromSplash -> navigation.startDesktopFromSplash()
            is Command.OpenStartLoginScreen -> navigation.startLogin()
            is Command.OpenCreateAccount -> navigation.createProfile(command.invitationCode)
            is Command.ChoosePinCodeScreen -> navigation.choosePinCode()
            is Command.CongratulationScreen -> navigation.congratulation()
            is Command.EnterKeyChainScreen -> navigation.enterKeychain()
            is Command.SelectAccountScreen -> navigation.chooseAccount()
            is Command.WorkspaceScreen -> navigation.workspace()
            is Command.InvitationCodeScreen -> navigation.enterInvitationCode()
            is Command.ExitToInvitationCodeScreen -> navigation.exitToInvitationCodeScreen()
            is Command.SetupNewAccountScreen -> navigation.setupNewAccount()
            is Command.SetupSelectedAccountScreen -> navigation.setupSelectedAccount(command.id)
            is Command.ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
            is Command.OpenProfile -> navigation.openProfile()
            is Command.OpenPage -> navigation.openDocument(command.id, command.editorSettings)
            is Command.OpenDatabaseViewAddView -> navigation.openDatabaseViewAddView()
            is Command.OpenEditDatabase -> navigation.openEditDatabase()
            is Command.OpenKeychainScreen -> navigation.openKeychainScreen()
            is Command.OpenContactsScreen -> navigation.openContacts()
            is Command.OpenSwitchDisplayView -> navigation.openSwitchDisplayView()
            is Command.OpenCustomizeDisplayView -> navigation.openCustomizeDisplayView()
            is Command.Exit -> navigation.exit()
            is Command.OpenKanbanScreen -> navigation.openKanban()
            is Command.OpenGoalsScreen -> navigation.openGoals()
            is Command.ExitToDesktop -> navigation.exitToDesktop()
            is Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
            is Command.OpenPageNavigationScreen -> navigation.openPageNavigation(
                command.target
            )
            is Command.ExitToDesktopAndOpenPage -> navigation.exitToDesktopAndOpenPage(command.pageId)
            is Command.OpenPageSearch -> navigation.openPageSearch()
        }
    }

}
