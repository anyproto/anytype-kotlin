package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SetupWallet
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber

class StartLoginViewModel(
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation {

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    override fun observeNavigation(): Observable<NavigationCommand> = navigation

    fun onLoginClicked() {
        navigation.accept(NavigationCommand.EnterKeyChainScreen)
    }

    fun onSignUpClicked() {
        setupWallet.invoke(
            scope = viewModelScope,
            params = SetupWallet.Params(
                path = pathProvider.providePath()
            )
        ) { result ->
            result.either(
                fnL = {
                    Timber.e(it, "Error while setting up wallet")
                },
                fnR = { navigation.accept(NavigationCommand.OpenCreateProfile) }
            )
        }
    }
}