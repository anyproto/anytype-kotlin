package com.anytypeio.anytype.ui.base

import androidx.lifecycle.Observer
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.presentation.navigation.AppNavigation

abstract class NavigationBottomSheetFragment : BaseBottomSheetFragment() {

    val navObserver = Observer<EventWrapper<AppNavigation.Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    private fun navigate(command: AppNavigation.Command) {

        val navigation = (requireActivity() as AppNavigation.Provider).nav()

        when (command) {

            is AppNavigation.Command.StartSplashFromDesktop -> navigation.startSplashFromDesktop()
            is AppNavigation.Command.StartDesktopFromLogin -> navigation.startDesktopFromLogin()
            is AppNavigation.Command.StartDesktopFromSplash -> navigation.startDesktopFromSplash()
            is AppNavigation.Command.OpenStartLoginScreen -> navigation.startLogin()
            is AppNavigation.Command.OpenCreateAccount -> navigation.createProfile(command.invitationCode)
            is AppNavigation.Command.ChoosePinCodeScreen -> navigation.choosePinCode()
            is AppNavigation.Command.CongratulationScreen -> navigation.congratulation()
            is AppNavigation.Command.EnterKeyChainScreen -> navigation.enterKeychain()
            is AppNavigation.Command.SelectAccountScreen -> navigation.chooseAccount()
            is AppNavigation.Command.WorkspaceScreen -> navigation.workspace()
            is AppNavigation.Command.SetupNewAccountScreen -> navigation.setupNewAccount()
            is AppNavigation.Command.InvitationCodeScreen -> navigation.enterInvitationCode()
            is AppNavigation.Command.ExitToInvitationCodeScreen -> navigation.exitToInvitationCodeScreen()
            is AppNavigation.Command.SetupSelectedAccountScreen -> navigation.setupSelectedAccount(
                command.id
            )
            is AppNavigation.Command.OpenArchive -> navigation.openArchive(command.target)
            is AppNavigation.Command.ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
            is AppNavigation.Command.OpenProfile -> navigation.openProfile()
            is AppNavigation.Command.OpenDatabaseViewAddView -> navigation.openDatabaseViewAddView()
            is AppNavigation.Command.OpenKeychainScreen -> navigation.openKeychainScreen()
            is AppNavigation.Command.OpenContactsScreen -> navigation.openContacts()
            is AppNavigation.Command.OpenCustomizeDisplayView -> navigation.openCustomizeDisplayView()
            is AppNavigation.Command.Exit -> navigation.exit()
            is AppNavigation.Command.ExitToDesktop -> navigation.exitToDesktop()
            is AppNavigation.Command.OpenDebugSettingsScreen -> navigation.openDebugSettings()
            is AppNavigation.Command.OpenPageNavigationScreen -> navigation.openPageNavigation(
                command.target
            )
            is AppNavigation.Command.ExitToDesktopAndOpenPage -> navigation.exitToDesktopAndOpenPage(command.pageId)
            is AppNavigation.Command.OpenPageSearch -> navigation.openPageSearch()
        }
    }

}
