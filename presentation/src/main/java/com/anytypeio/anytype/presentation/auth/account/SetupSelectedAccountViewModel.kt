package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplitude.api.Amplitude
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class SetupSelectedAccountViewModel(
    private val startAccount: StartAccount,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val isMigrationInProgres = MutableStateFlow(false)
    val error = MutableLiveData<String>()
    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val migrationMessageTimer = flow {
        if (viewModelScope.isActive) {
            delay(TIMEOUT_DURATION)
            emit(true)
        }
    }

    private val migrationMessageJob: Job = viewModelScope.launch {
        migrationMessageTimer.collect { isMigrationInProgres.value = true }
    }

    fun selectAccount(id: String) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            startAccount(
                StartAccount.Params(
                    id = id,
                    path = pathProvider.providePath()
                )
            ).process(
                failure = {
                    migrationMessageJob.cancel()
                    isMigrationInProgres.value = false
                    error.postValue(ERROR_MESSAGE)
                    Timber.e(it, "Error while selecting account with id: $id")
                },
                success = { accountId ->
                    migrationMessageJob.cancel()
                    isMigrationInProgres.value = false
                    Amplitude.getInstance().setUserId(accountId, true)
                    sendEvent(startTime)
                    navigateToHomeDashboard()
                }
            )
        }
    }

    private fun navigateToHomeDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromLogin))
    }

    private fun sendEvent(startTime: Long) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            renderTime = middleTime,
            eventName = EventsDictionary.ACCOUNT_SELECT,
            props = Props.empty()
        )
    }

    companion object {
        const val ERROR_MESSAGE = "An error occured while starting account..."
        const val TIMEOUT_DURATION = 5000L
    }
}