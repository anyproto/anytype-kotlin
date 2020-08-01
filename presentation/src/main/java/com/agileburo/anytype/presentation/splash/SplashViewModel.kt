package com.agileburo.anytype.presentation.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.interactor.LaunchAccount
import com.agileburo.anytype.domain.auth.interactor.LaunchWallet
import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.presentation.navigation.AppNavigation
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModel(
    private val analytics: Analytics,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchWallet: LaunchWallet,
    private val launchAccount: LaunchAccount
) : ViewModel() {

    val state = MutableLiveData<ViewState<Nothing>>()

    val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> = MutableLiveData()

    fun onResume() {
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).either(
                fnL = { e -> Timber.e(e, "Error while checking auth status") },
                fnR = { status ->
                    if (status == AuthStatus.UNAUTHORIZED)
                        navigation.postValue(EventWrapper(AppNavigation.Command.OpenStartLoginScreen))
                    else
                        proceedWithLaunchingWallet()
                }
            )
        }
    }

    private fun proceedWithLaunchingWallet() {
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = { retryLaunchingWallet() },
                fnR = { proceedWithLaunchingAccount() }
            )
        }
    }

    private fun retryLaunchingWallet() {
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = { e ->
                    Timber.e(e, "Error while retrying launching wallet")
                    state.postValue(ViewState.Error(error = e.toString()))
                },
                fnR = { proceedWithLaunchingAccount() }
            )
        }
    }

    private fun proceedWithLaunchingAccount() {
        viewModelScope.launch {
            launchAccount(BaseUseCase.None).either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromSplash)) },
                fnL = { e -> Timber.e(e, "Error while launching account") }
            )
        }
    }
}