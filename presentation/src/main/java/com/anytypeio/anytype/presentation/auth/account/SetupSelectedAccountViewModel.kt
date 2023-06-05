package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.openAccount
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
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
    private val selectAccount: SelectAccount,
    private val pathProvider: PathProvider,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val crashReporter: CrashReporter,
    private val configStorage: ConfigStorage
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
            selectAccount(
                SelectAccount.Params(
                    id = id,
                    path = pathProvider.providePath()
                )
            ).process(
                failure = {e ->
                    Timber.e(e, "Error while selecting account with id: $id")
                    if (e is MigrationNeededException) {
                        navigateToMigrationErrorScreen()
                    } else {
                        migrationMessageJob.cancel()
                        isMigrationInProgress.value = false
                        val msg = e.message ?: "Unknown error"
                        error.postValue("$ERROR_MESSAGE: $msg")
                    }
                },
                success = { (analyticsId, status) ->
                    migrationMessageJob.cancel()
                    isMigrationInProgress.value = false
                    analytics.proceedWithAccountEvent(
                        configStorage = configStorage,
                        startTime = startTime,
                        eventName = openAccount
                    )
                    crashReporter.setUser(analyticsId)
                    if (status is AccountStatus.PendingDeletion) {
                        navigation.postValue(
                            EventWrapper(
                                AppNavigation.Command.DeletedAccountScreen(
                                    deadline = status.deadline
                                )
                            )
                        )
                    } else {
                        proceedWithGlobalSubscriptions()
                        navigateToDashboard()
                    }
                }
            )
        }
    }

    fun onRetryClicked(id: Id) {
        selectAccount(id)
    }

    private fun navigateToDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromLogin))
    }

    private fun navigateToMigrationErrorScreen() {
        navigation.postValue(EventWrapper(AppNavigation.Command.MigrationErrorScreen))
    }

    private fun proceedWithGlobalSubscriptions() {
        relationsSubscriptionManager.onStart()
        objectTypesSubscriptionManager.onStart()
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        const val TIMEOUT_DURATION = 5000L
    }
}