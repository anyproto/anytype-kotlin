package com.agileburo.anytype.feature_login.ui.login.presentation.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.AuthNavigationProvider
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand.*
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment(
    private val fragmentScope: Boolean = true
) : Fragment() {

    val subscriptions by lazy { CompositeDisposable() }

    abstract fun injectDependencies()
    abstract fun releaseDependencies()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragmentScope) releaseDependencies()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
    }

    fun navigation(command: NavigationCommand) {
        val navigation = navigationProvider().provideNavigation()

        when (command) {
            OpenCreateProfile -> navigation.createProfile()
            ChoosePinCodeScreen -> navigation.choosePinCode()
            CongratulationScreen -> navigation.congratulation()
            EnterKeyChainScreen -> navigation.enterKeychain()
            ChooseProfileScreen -> navigation.chooseProfile()
            WorkspaceScreen -> navigation.workspace()
            SetupNewAccountScreen -> navigation.setupNewAccount()
            is SetupSelectedAccountScreen -> navigation.setupSelectedAccount(command.id)
            is ConfirmPinCodeScreen -> navigation.confirmPinCode(command.code)
        }
    }

    private fun navigationProvider() = (requireActivity() as AuthNavigationProvider)
}
