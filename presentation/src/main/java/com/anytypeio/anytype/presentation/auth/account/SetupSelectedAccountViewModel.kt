package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.openAccount
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class SetupSelectedAccountViewModel(
    private val startAccount: StartAccount,
    private val pathProvider: PathProvider,
    private val analytics: Analytics,
    private val storeObjectTypes: StoreObjectTypes
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val isMigrationInProgress = MutableStateFlow(false)
    val error = MutableLiveData<String>()
    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val migrationMessageTimer = flow {
        if (viewModelScope.isActive) {
            delay(TIMEOUT_DURATION)
            emit(true)
        }
    }

    private val migrationMessageJob: Job = viewModelScope.launch {
        migrationMessageTimer.collect { isMigrationInProgress.value = true }
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
                    isMigrationInProgress.value = false
                    val msg = it.message ?: "Unknown error"
                    error.postValue("$ERROR_MESSAGE: $msg")
                    Timber.e(it, "Error while selecting account with id: $id")
                },
                success = { accountId ->
                    migrationMessageJob.cancel()
                    isMigrationInProgress.value = false
                    updateUserProps(accountId)
                    sendEvent(startTime)
                    proceedWithUpdatingObjectTypesStore()
                }
            )
        }
    }

    fun onRetryClicked(id: Id) {
        selectAccount(id)
    }

    private fun proceedWithUpdatingObjectTypesStore() {
        viewModelScope.launch {
            storeObjectTypes.invoke(Unit).process(
                failure = {
                    Timber.e(it, "Error while store account object types")
                    navigateToDashboard()
                },
                success = {
                    navigateToDashboard()
                }
            )
        }
    }

    private fun navigateToDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromLogin))
    }

    private fun updateUserProps(id: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(id)
        )
    }

    private fun sendEvent(startTime: Long) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            eventName = openAccount
        )
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        const val TIMEOUT_DURATION = 5000L
    }
}