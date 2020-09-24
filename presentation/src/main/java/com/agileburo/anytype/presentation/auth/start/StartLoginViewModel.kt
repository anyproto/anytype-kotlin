package com.agileburo.anytype.presentation.auth.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.analytics.base.EventsDictionary
import com.agileburo.anytype.analytics.base.sendEvent
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.domain.auth.interactor.SetupWallet
import com.agileburo.anytype.domain.device.PathProvider
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class StartLoginViewModel(
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        sendAuthEvent()
    }

    fun onLoginClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.EnterKeyChainScreen))
    }

    fun onSignUpClicked() {
        val startTime = System.currentTimeMillis()
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
                    sendWalletEvent(startTime)
                    navigation.postValue(EventWrapper(AppNavigation.Command.InvitationCodeScreen))
                }
            )
        }
    }

    private fun sendAuthEvent() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_AUTH
        )
    }

    private fun sendWalletEvent(startTime: Long) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            renderTime = middleTime,
            eventName = EventsDictionary.WALLET_CREATE
        )
    }
}