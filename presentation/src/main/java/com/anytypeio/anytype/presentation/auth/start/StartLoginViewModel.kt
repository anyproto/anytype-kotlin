package com.anytypeio.anytype.presentation.auth.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.authScreenShow
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
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
                    navigation.postValue(EventWrapper(AppNavigation.Command.AboutAnalyticsScreen))
                }
            )
        }
    }

    private fun sendAuthEvent() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = authScreenShow
        )
    }
}