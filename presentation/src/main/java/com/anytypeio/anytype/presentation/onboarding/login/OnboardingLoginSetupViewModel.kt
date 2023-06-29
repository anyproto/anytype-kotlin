package com.anytypeio.anytype.presentation.onboarding.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.exceptions.AccountIsDeletedException
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.anytypeio.anytype.presentation.auth.model.SelectAccountView
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingLoginSetupViewModel @Inject constructor(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val analytics: Analytics,
    private val selectAccount: SelectAccount,
    private val pathProvider: PathProvider,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val crashReporter: CrashReporter,
    private val configStorage: ConfigStorage
) : ViewModel() {

    val navigation = MutableSharedFlow<Navigation>()

    val state by lazy { MutableLiveData<List<SelectAccountView>>() }
    val error by lazy { MutableLiveData<String>() }

    init {
        startObservingAccounts()
        startLoadingAccount()
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e ->
                    if (e is AccountIsDeletedException) {
                        error.postValue("This account is deleted. Try using another account or create a new one.")
                    } else {
                        error.postValue("Error while account loading \n ${e.localizedMessage}")
                    }
                    Timber.e(e, "Error while account loading")
                    // TODO refact
                    viewModelScope.launch { navigation.emit(Navigation.Exit) }
                },
                fnR = {
                    Timber.d("Account loading successfully finished")
                }
            )
        }
    }

    private fun startObservingAccounts() {
        viewModelScope.launch {
            observeAccounts.build().take(1).collect { account ->
                onFirstAccountLoaded(account.id)
            }
        }
    }

    private fun onFirstAccountLoaded(id: String) {
        proceedWithSelectingAccount(id)
    }

    private fun proceedWithSelectingAccount(id: String) {
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
                    when (e) {
                        is MigrationNeededException -> {
                            navigateToMigrationErrorScreen()
                        }
                        is AccountIsDeletedException -> {
                            error.postValue("This account is deleted. Try using another account or create a new one.")
                        }
                        is NeedToUpdateApplicationException -> {
                            error.postValue(SplashViewModel.ERROR_NEED_UPDATE)
                        }
                        else -> {
//                            migrationMessageJob.cancel()
//                            isMigrationInProgress.value = false
                            val msg = e.message ?: "Unknown error"
                            error.postValue("${SetupSelectedAccountViewModel.ERROR_MESSAGE}: $msg")
                        }
                    }
                },
                success = { (analyticsId, status) ->
//                    migrationMessageJob.cancel()
//                    isMigrationInProgress.value = false
                    analytics.proceedWithAccountEvent(
                        configStorage = configStorage,
                        startTime = startTime,
                        eventName = EventsDictionary.openAccount
                    )
                    crashReporter.setUser(analyticsId)
                    if (status is AccountStatus.PendingDeletion) {
                        // TODO
//                        navigation.postValue(
//                            EventWrapper(
//                                AppNavigation.Command.DeletedAccountScreen(
//                                    deadline = status.deadline
//                                )
//                            )
//                        )
                    } else {
                        proceedWithGlobalSubscriptions()
                        navigateToDashboard()
                    }
                }
            )
        }
    }

    fun onRetryClicked(id: Id) {
        proceedWithSelectingAccount(id)
    }

    private fun navigateToDashboard() {
        viewModelScope.launch {
            navigation.emit(Navigation.NavigateToHomeScreen)
        }
//        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromLogin))
    }

    private fun navigateToMigrationErrorScreen() {
        viewModelScope.launch {
            navigation.emit(Navigation.NavigateToMigrationErrorScreen)
        }
//        navigation.postValue(EventWrapper(AppNavigation.Command.MigrationErrorScreen))
    }

    private fun proceedWithGlobalSubscriptions() {
        relationsSubscriptionManager.onStart()
        objectTypesSubscriptionManager.onStart()
    }

    sealed class Navigation {
        object Exit : Navigation()
        object NavigateToMigrationErrorScreen : Navigation()
        object NavigateToHomeScreen: Navigation()
    }

    sealed class SideEffect {
        // TODO
    }

    class Factory(
        private val startLoadingAccounts: StartLoadingAccounts,
        private val observeAccounts: ObserveAccounts,
        private val analytics: Analytics,
        private val selectAccount: SelectAccount,
        private val pathProvider: PathProvider,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        private val crashReporter: CrashReporter,
        private val configStorage: ConfigStorage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingLoginSetupViewModel(
                selectAccount = selectAccount,
                pathProvider = pathProvider,
                analytics = analytics,
                relationsSubscriptionManager = relationsSubscriptionManager,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager,
                crashReporter = crashReporter,
                configStorage = configStorage,
                startLoadingAccounts = startLoadingAccounts,
                observeAccounts = observeAccounts
            ) as T
        }
    }
}