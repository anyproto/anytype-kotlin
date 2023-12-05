package com.anytypeio.anytype.presentation.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.exceptions.AccountIsDeletedException
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingLoginEvent
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicLoginViewModel @Inject constructor(
    private val recoverWallet: RecoverWallet,
    private val convertWallet: ConvertWallet,
    private val saveMnemonic: SaveMnemonic,
    private val pathProvider: PathProvider,
    private val analytics: Analytics,
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts,
    private val selectAccount: SelectAccount,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val spaceDeletedStatusWatcher: SpaceDeletedStatusWatcher,
    private val crashReporter: CrashReporter,
    private val configStorage: ConfigStorage,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val jobs = mutableListOf<Job>()

    val sideEffects = MutableSharedFlow<SideEffect>()
    val state = MutableStateFlow<SetupState>(SetupState.Idle)

    val navigation = MutableSharedFlow<Navigation>()

    val error by lazy { MutableStateFlow(NO_ERROR) }

    init {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.loginScreenShow
        )
    }

    fun onLoginClicked(chain: String) {
        if (state.value is SetupState.Idle) {
            proceedWithRecoveringWallet(chain.trim())
            viewModelScope.sendAnalyticsOnboardingLoginEvent(
                analytics = analytics,
                type = EventsDictionary.ClickLoginButton.PHRASE
            )
        }
    }

    fun onActionDone(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onBackButtonPressed() {
        Timber.d("onBackButtonPressed")
        viewModelScope.launch {
            sideEffects.emit(SideEffect.Exit)
        }
    }

    fun onGetEntropyFromQRCode(entropy: String) {
        viewModelScope.launch {
            convertWallet(
                params = ConvertWallet.Request(entropy)
            ).proceed(
                failure = { error ->
                    sideEffects.emit(
                        SideEffect.Error(
                            "Error while login: ${error.message}"
                        )
                    ).also {
                        Timber.e(error, "Error while convert wallet")
                    }
                },
                success = { mnemonic -> proceedWithRecoveringWallet(mnemonic) }
            )
        }
    }

    private fun proceedWithRecoveringWallet(chain: String) {
        recoverWallet.invoke(
            scope = viewModelScope,
            params = RecoverWallet.Params(
                path = pathProvider.providePath(),
                mnemonic = chain
            )
        ) { result ->
            result.either(
                fnR = {
                    proceedWithSavingMnemonic(chain)
                },
                fnL = { error ->
                    viewModelScope.launch {
                        sideEffects.emit(
                            SideEffect.Error(
                                "Error while login: ${error.message}"
                            )
                        ).also {
                            Timber.e(error, "Error while recovering wallet")
                        }
                    }

                }
            )
        }
    }

    private fun proceedWithSavingMnemonic(mnemonic: String) {
        saveMnemonic.invoke(
            scope = viewModelScope,
            params = SaveMnemonic.Params(
                mnemonic = mnemonic
            )
        ) { result ->
            result.either(
                fnR = {
                    startObservingAccounts()
                    startLoadingAccount()
                },
                fnL = { Timber.e(it, "Error while saving mnemonic") }
            )
        }
    }

    fun onScanQrCodeClicked() {
        viewModelScope.sendAnalyticsOnboardingLoginEvent(
            analytics = analytics,
            type = EventsDictionary.ClickLoginButton.QR
        )
    }

    private fun startLoadingAccount() {
        startLoadingAccounts.invoke(
            viewModelScope, StartLoadingAccounts.Params()
        ) { result ->
            result.either(
                fnL = { e ->
                    if (e is AccountIsDeletedException) {
                        error.value = "This account is deleted. Try using another account or create a new one."
                    } else {
                        error.value = "Error while account loading \n ${e.localizedMessage}"
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
        jobs += viewModelScope.launch {
            state.value = SetupState.InProgress
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
                failure = { e ->
                    Timber.e(e, "Error while selecting account with id: $id")
                    state.value = SetupState.Failed
                    when (e) {
                        is MigrationNeededException -> {
                            navigateToMigrationErrorScreen()
                        }
                        is AccountIsDeletedException -> {
                            error.value = "This account is deleted. Try using another account or create a new one."
                        }
                        is NeedToUpdateApplicationException -> {
                            error.value = SplashViewModel.ERROR_NEED_UPDATE
                        }
                        else -> {
                            val msg = e.message ?: "Unknown error"
                            error.value = "${ERROR_MESSAGE}: $msg"
                        }
                    }
                },
                success = { (analyticsId, status) ->
                    analytics.proceedWithAccountEvent(
                        configStorage = configStorage,
                        startTime = startTime,
                        eventName = EventsDictionary.openAccount,
                        lang = localeProvider.language()
                    )
                    crashReporter.setUser(analyticsId)
                    proceedWithGlobalSubscriptions()
                    navigateToDashboard()
                }
            )
        }
    }

    private fun navigateToMigrationErrorScreen() {
        viewModelScope.launch {
            navigation.emit(Navigation.NavigateToMigrationErrorScreen)
        }
    }

    private fun proceedWithGlobalSubscriptions() {
        relationsSubscriptionManager.onStart()
        objectTypesSubscriptionManager.onStart()
        spaceDeletedStatusWatcher.onStart()
    }

    private fun navigateToDashboard() {
        viewModelScope.launch {
            navigation.emit(Navigation.NavigateToHomeScreen)
        }
    }

    fun onRetryClicked(id: Id) {
        // TODO
        proceedWithSelectingAccount(id)
    }

    sealed class SideEffect {
        data class Error(val msg: String): SideEffect()
        object Exit: SideEffect()
    }

    sealed class SetupState {
        object Idle : SetupState()
        object InProgress: SetupState()
        object Failed: SetupState()
    }

    sealed class Navigation {
        object Exit : Navigation()
        object NavigateToMigrationErrorScreen : Navigation()
        object NavigateToHomeScreen: Navigation()
    }

    class Factory @Inject constructor(
        private val pathProvider: PathProvider,
        private val convertWallet: ConvertWallet,
        private val recoverWallet: RecoverWallet,
        private val saveMnemonic: SaveMnemonic,
        private val analytics: Analytics,
        private val startLoadingAccounts: StartLoadingAccounts,
        private val observeAccounts: ObserveAccounts,
        private val selectAccount: SelectAccount,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        private val spaceDeletedStatusWatcher: SpaceDeletedStatusWatcher,
        private val crashReporter: CrashReporter,
        private val configStorage: ConfigStorage,
        private val localeProvider: LocaleProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicLoginViewModel(
                recoverWallet = recoverWallet,
                convertWallet = convertWallet,
                pathProvider = pathProvider,
                saveMnemonic = saveMnemonic,
                analytics = analytics,
                relationsSubscriptionManager = relationsSubscriptionManager,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager,
                crashReporter = crashReporter,
                configStorage = configStorage,
                startLoadingAccounts = startLoadingAccounts,
                observeAccounts = observeAccounts,
                spaceDeletedStatusWatcher = spaceDeletedStatusWatcher,
                selectAccount = selectAccount,
                localeProvider = localeProvider
            ) as T
        }
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        const val NO_ERROR = ""
    }
}