package com.agileburo.anytype.presentation.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.analytics.base.EventsDictionary
import com.agileburo.anytype.analytics.base.sendEvent
import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.Props
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.amplitude.api.Amplitude
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewModel(
    private val getCurrentAccount: GetCurrentAccount,
    private val logout: Logout,
    private val analytics: Analytics
) : ViewStateViewModel<ViewState<ProfileView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var target = ""

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
        proceedWithGettingAccount()
    }

    fun onBackButtonClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    fun onProfileCardClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DOCUMENT
        )
        navigate(EventWrapper(AppNavigation.Command.OpenPage(target)))
    }

    fun onDebugSettingsClicked(){
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenDebugSettingsScreen))
    }

    private fun proceedWithGettingAccount() {
        getCurrentAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting account") },
                fnR = { account ->
                    target = account.id
                    stateData.postValue(
                        ViewState.Success(
                            data = ProfileView(
                                name = account.name,
                                avatar = account.avatar
                            )
                        )
                    )
                }
            )
        }
    }

    fun onLogoutClicked() {
        val startTime = System.currentTimeMillis()
        logout.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e ->
                    Timber.e(e, "Error while logging out")
                },
                fnR = {
                    sendEvent(startTime)
                    Amplitude.getInstance().userId = null
                    navigation.postValue(EventWrapper(AppNavigation.Command.StartSplashFromDesktop))
                }
            )
        }
    }

    fun onKeyChainPhraseClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_KEYCHAIN
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenKeychainScreen))
    }

    fun onPinCodeClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPinCodeScreen))
    }

    private fun sendEvent(startTime: Long) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            eventName = EventsDictionary.ACCOUNT_STOP
        )
    }

    companion object DEBUG_SETTINGS {

        const val ANYTYPE_ACTION_MODE = "debug.settings.aam"
    }
}