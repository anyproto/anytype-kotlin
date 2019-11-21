package com.agileburo.anytype.presentation.auth.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.SetupWallet
import com.agileburo.anytype.domain.device.PathProvider
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class StartLoginViewModel(
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onLoginClicked() {
        navigation.postValue(Event(AppNavigation.Command.EnterKeyChainScreen))
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
                fnR = {
                    navigation.postValue(Event(AppNavigation.Command.OpenCreateAccount))
                }
            )
        }
    }
}