package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
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
import kotlinx.coroutines.launch
import timber.log.Timber

@Deprecated("legacy")
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
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            logout(params = BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        _isLoggingOut.value = true
                    }
                    is Interactor.Status.Success -> {
                        _isLoggingOut.value = false
                        updateUserProperties(
                            analytics = analytics,
                            userProperty = UserProperty.AccountId(null)
                        )
                        navigation.postValue(EventWrapper(AppNavigation.Command.StartSplashFromDesktop))
                    }
                    is Interactor.Status.Error -> {
                        Timber.e(status.throwable, "Error while logging out")
                    }
                }
            }
        }
    }
}