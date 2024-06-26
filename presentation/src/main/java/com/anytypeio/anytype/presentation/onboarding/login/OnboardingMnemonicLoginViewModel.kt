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
import com.anytypeio.anytype.core_models.exceptions.LoginException
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugGoroutines
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingLoginEvent
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
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
    private val localeProvider: LocaleProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val debugGoroutines: DebugGoroutines,
    private val uriFileProvider: UriFileProvider,
    private val logout: Logout
) : ViewModel() {

    private val jobs = mutableListOf<Job>()
    private var goroutinesJob : Job? = null

    val sideEffects = MutableSharedFlow<SideEffect>()
    val state = MutableStateFlow<SetupState>(SetupState.Idle)

    val command = MutableSharedFlow<Command>(replay = 0)

    val error by lazy { MutableStateFlow(NO_ERROR) }

    private var debugClickCount = 0
    private val _fiveClicks = MutableStateFlow(false)

    init {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.loginScreenShow
        )
        viewModelScope.launch {
            _fiveClicks.collect {
                if (it) proceedWithGoroutinesDebug()
            }
        }
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
            when(state.value) {
                is SetupState.InProgress -> {
                    proceedWithAbortAndExit()
                }
                is SetupState.Abort -> {
                    command.emit(Command.ShowToast("Aborting... Please wait!"))
                }
                else -> {
                    sideEffects.emit(SideEffect.Exit)
                }
            }
        }
    }

    private suspend fun proceedWithAbortAndExit() {
        state.value = SetupState.Abort
        Timber.d("Starting abort")
        jobs.cancel()
        logout(Logout.Params(clearLocalRepositoryData = true)).collect { status ->
            when (status) {
                is Interactor.Status.Error -> {
                    Timber.e(status.throwable, "Failed to logout after unsuccessful login")
                    sideEffects.emit(SideEffect.Exit)
                }

                is Interactor.Status.Started -> {
                    Timber.d("Logout started...")
                }

                is Interactor.Status.Success -> {
                    sideEffects.emit(SideEffect.Exit)
                }
            }
        }
    }

    fun onGetEntropyFromQRCode(entropy: String) {
        viewModelScope.launch {
            convertWallet(
                params = ConvertWallet.Request(entropy)
            ).proceed(
                failure = { exception ->
                    val error = when(exception) {
                        is LoginException.InvalidMnemonic -> SideEffect.Error.InvalidMnemonic
                        is LoginException.NetworkIdMismatch -> SideEffect.Error.NetworkIdMismatch
                        else -> SideEffect.Error.Unknown("Error while wallet convert: ${exception.message}")
                    }
                    sideEffects.emit(error).also {
                        Timber.e(exception, "Error while convert wallet")
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
                fnL = { exception ->
                    Timber.d("Got exception: $exception")
                    viewModelScope.launch {
                        val error = when(exception) {
                            is LoginException.InvalidMnemonic -> SideEffect.Error.InvalidMnemonic
                            is LoginException.NetworkIdMismatch -> SideEffect.Error.NetworkIdMismatch
                            else -> SideEffect.Error.Unknown("Error while login: ${exception.message}")
                        }
                        sideEffects.emit(error).also {
                            Timber.e(exception, "Error while selecting account")
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
                    viewModelScope.launch { command.emit(Command.Exit) }
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
        jobs += viewModelScope.launch {
            selectAccount(
                SelectAccount.Params(
                    id = id,
                    path = pathProvider.providePath(),

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
                        is LoginException.NetworkIdMismatch -> {
                            sideEffects.emit(SideEffect.Error.NetworkIdMismatch)
                        }
                        is LoginException.FailedToFindAccountInfo -> {
                            sideEffects.emit(SideEffect.Error.NetworkIdMismatch)
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
            command.emit(Command.NavigateToMigrationErrorScreen)
        }
    }

    private fun proceedWithGlobalSubscriptions() {
        relationsSubscriptionManager.onStart()
        objectTypesSubscriptionManager.onStart()
        spaceDeletedStatusWatcher.onStart()
        userPermissionProvider.start()
    }

    private fun navigateToDashboard() {
        viewModelScope.launch {
            command.emit(Command.NavigateToHomeScreen)
        }
    }

    fun onEnterMyVaultClicked() {
        Timber.d("onEnterMyVaultClicked")
        viewModelScope.launch {
            debugClickCount++
            if (debugClickCount == 5) {
                _fiveClicks.emit(true)
                debugClickCount = 0
            } else {
                _fiveClicks.emit(false)
            }
        }
    }

    private fun proceedWithGoroutinesDebug() {
        if (goroutinesJob?.isActive == true) {
            return
        }
        Timber.d("proceedWithGoroutinesDebug")
        goroutinesJob = viewModelScope.launch {
            debugGoroutines.async(DebugGoroutines.Params()).fold(
                onSuccess = { path ->
                    command.emit(Command.ShareDebugGoroutines(path, uriFileProvider))
                },
                onFailure = {
                    Timber.e(it, "Error while collecting goroutines diagnostics")
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        goroutinesJob?.cancel()
    }

    sealed class SideEffect {
        sealed class Error : SideEffect() {
            data object InvalidMnemonic : Error()
            data object NetworkIdMismatch: Error()
            data class Unknown(val msg: String): SideEffect()
        }
        data object Exit: SideEffect()
    }

    sealed class SetupState {
        data object Idle : SetupState()
        data object InProgress: SetupState()
        data object Failed: SetupState()
        data object Abort: SetupState()
    }

    sealed class Command {
        data object Exit : Command()
        data object NavigateToMigrationErrorScreen : Command()
        data object NavigateToHomeScreen: Command()
        data class ShowToast(val message: String) : Command()
        data class ShareDebugGoroutines(val path: String, val uriFileProvider: UriFileProvider) : Command()
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
        private val userPermissionProvider: UserPermissionProvider,
        private val debugGoroutines: DebugGoroutines,
        private val uriFileProvider: UriFileProvider,
        private val logout: Logout
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
                localeProvider = localeProvider,
                userPermissionProvider = userPermissionProvider,
                debugGoroutines = debugGoroutines,
                uriFileProvider = uriFileProvider,
                logout = logout
            ) as T
        }
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        const val NO_ERROR = ""
    }
}