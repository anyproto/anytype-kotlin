package com.agileburo.anytype.presentation.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.interactor.LaunchAccount
import com.agileburo.anytype.domain.auth.interactor.LaunchWallet
import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.presentation.navigation.AppNavigation
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModel(
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchWallet: LaunchWallet,
    private val launchAccount: LaunchAccount
) : ViewModel() {

    val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() {
        checkAuthorizationStatus.invoke(viewModelScope, Unit) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while checking auth status") },
                fnR = { status ->
                    if (status == AuthStatus.UNAUTHORIZED)
                        navigation.postValue(Event(AppNavigation.Command.OpenStartLoginScreen))
                    else
                        proceedWithLaunchingWallet()
                }
            )
        }
    }

    private fun proceedWithLaunchingWallet() {
        launchWallet.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while launching wallet") },
                fnR = { proceedWithLaunchingAccount() }
            )
        }
    }

    private fun proceedWithLaunchingAccount() {
        launchAccount.invoke(viewModelScope, BaseUseCase.None) { result ->
            result.either(
                fnR = { navigation.postValue(Event(AppNavigation.Command.StartDesktopFromSplash)) },
                fnL = { e -> Timber.e(e, "Error while launching account") }
            )
        }
    }
}