package com.agileburo.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.core_utils.ui.BaseFragment
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.AppNavigation.Command

abstract class NavigationFragment(
    @LayoutRes private val layout: Int
) : BaseFragment(layout) {

    val navObserver = Observer<Event<Command>> { event ->
        event.getContentIfNotHandled()?.let { navigate(it) }
    }

    private fun navigate(command: Command) {

        val navigation = (requireActivity() as AppNavigation.Provider).nav()

        when (command) {
            is Command.OpenStartLoginScreen -> navigation.startLogin()
            is Command.OpenCreateProfile -> navigation.createProfile()
            is Command.ChoosePinCodeScreen -> navigation.choosePinCode()
            is Command.CongratulationScreen -> navigation.congratulation()
            is Command.EnterKeyChainScreen -> navigation.enterKeychain()
            is Command.ChooseProfileScreen -> navigation.chooseProfile()
            is Command.WorkspaceScreen -> navigation.workspace()
            is Command.SetupNewAccountScreen -> navigation.setupNewAccount()
            is Command.SetupSelectedAccountScreen -> navigation.setupSelectedAccount(command.id)
            is Command.ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
            is Command.OpenProfile -> navigation.openProfile()
            is Command.OpenDocument -> navigation.openDocument(command.id)
        }
    }

}