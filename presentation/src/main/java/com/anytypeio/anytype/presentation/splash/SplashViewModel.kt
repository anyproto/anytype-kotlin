package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.ACCOUNT_SELECT
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_ACCOUNT_ID
import com.anytypeio.anytype.analytics.base.EventsDictionary.WALLET_RECOVER
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.presentation.navigation.AppNavigation
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
    private val launchAccount: LaunchAccount,
    private val storeObjectTypes: StoreObjectTypes
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
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = { retryLaunchingWallet() },
                fnR = {
                    sendEvent(startTime, WALLET_RECOVER, Props.empty())
                    proceedWithLaunchingAccount()
                }
            )
        }
    }

    private fun retryLaunchingWallet() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = { e ->
                    Timber.e(e, "Error while retrying launching wallet")
                    state.postValue(ViewState.Error(error = e.toString()))
                },
                fnR = {
                    sendEvent(startTime, WALLET_RECOVER, Props.empty())
                    proceedWithLaunchingAccount()
                }
            )
        }
    }

    private fun proceedWithLaunchingAccount() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            launchAccount(BaseUseCase.None).either(
                fnR = { accountId ->
                    updateUserProps(accountId)
                    val props = Props(mapOf(PROP_ACCOUNT_ID to accountId))
                    sendEvent(startTime, ACCOUNT_SELECT, props)
                    proceedWithUpdatingObjectTypesStore()
                },
                fnL = { e ->
                    state.postValue(ViewState.Error(error = ERROR_MESSAGE))
                    Timber.e(e, "Error while launching account")
                }
            )
        }
    }

    private fun proceedWithUpdatingObjectTypesStore() {
        viewModelScope.launch {
            storeObjectTypes.invoke(Unit).process(
                failure = {
                    Timber.e(it, "Error while store account object types")
                    navigateToDashboard()
                },
                success = { navigateToDashboard() }
            )
        }
    }

    private fun navigateToDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromSplash))
    }

    private fun updateUserProps(id: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(id)
        )
    }

    private fun sendEvent(startTime: Long, event: String, props: Props) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            renderTime = middleTime,
            eventName = event,
            props = props
        )
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
    }
}