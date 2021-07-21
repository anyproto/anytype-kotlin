package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amplitude.api.Amplitude
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.ACCOUNT_STOP
import com.anytypeio.anytype.analytics.base.EventsDictionary.BTN_PROFILE_BACK
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_DOCUMENT
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_KEYCHAIN
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.auth.interactor.GetCurrentAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

open class ProfileViewModel(
    private val getCurrentAccount: GetCurrentAccount,
    private val logout: Logout,
    private val analytics: Analytics,
    private val getLibraryVersion: GetLibraryVersion
) : ViewStateViewModel<ViewState<ProfileView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    private var target = ""

    private val libVersion = MutableLiveData("")
    val version: LiveData<String> = libVersion

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
        proceedWithGettingAccount()
        proceedWithGetVersion()
    }

    fun onBackButtonClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = BTN_PROFILE_BACK
        )
    }

    fun onProfileCardClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = SCREEN_DOCUMENT
        )
        navigate(EventWrapper(AppNavigation.Command.OpenObject(target)))
    }

    fun onDebugSettingsClicked() {
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

    private fun proceedWithGetVersion() {
        getLibraryVersion.invoke(scope = viewModelScope, params = BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting middleware version") },
                fnR = { version -> libVersion.postValue(version) }
            )
        }
    }

    fun onLogoutClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_PROFILE_LOG_OUT
        )
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            logout(params = BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        _isLoggingOut.value = true
                    }
                    is Interactor.Status.Success -> {
                        _isLoggingOut.value = false
                        sendEvent(startTime)
                        Amplitude.getInstance().userId = null
                        navigation.postValue(EventWrapper(AppNavigation.Command.StartSplashFromDesktop))
                    }
                    is Interactor.Status.Error -> {
                        Timber.e(status.throwable, "Error while logging out")
                    }
                }
            }
        }
    }

    fun onKeyChainPhraseClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = SCREEN_KEYCHAIN
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenKeychainScreen))
    }

    fun onPinCodeClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_PROFILE_PIN
        )
    }

    fun onWallpaperClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_PROFILE_WALLPAPER
        )
    }

    private fun sendEvent(startTime: Long) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            eventName = ACCOUNT_STOP
        )
    }
}