package com.agileburo.anytype.presentation.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.presentation.navigation.AppNavigation
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModel(private val checkAuthorizationStatus: CheckAuthorizationStatus) : ViewModel() {

    val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() {
        checkAuthorizationStatus.invoke(viewModelScope, Unit) {
            it.either(
                fnL = { e -> Timber.e(e, "Error while checking auth status") },
                fnR = ::proceedWithAuthStatus
            )
        }
    }

    private fun proceedWithAuthStatus(status: AuthStatus) =
        if (status == AuthStatus.UNAUTHORIZED)
            navigation.postValue(Event(AppNavigation.Command.OpenStartLoginScreen))
        else
            navigation.postValue(Event(AppNavigation.Command.StartDesktopFromSplash))
}