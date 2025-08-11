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
import com.anytypeio.anytype.core_models.exceptions.AccountMigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.LoginException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugAccountSelectTrace
import com.anytypeio.anytype.domain.debugging.DebugGoroutines
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOnboardingLoginEvent
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
    private val crashReporter: CrashReporter,
    private val configStorage: ConfigStorage,
    private val localeProvider: LocaleProvider,
    private val debugGoroutines: DebugGoroutines,
    private val uriFileProvider: UriFileProvider,
    private val logout: Logout,
    private val globalSubscriptionManager: GlobalSubscriptionManager,
    private val debugAccountSelectTrace: DebugAccountSelectTrace,
    private val migrationDelegate: MigrationHelperDelegate
) : ViewModel(), MigrationHelperDelegate by migrationDelegate {

    private val jobs = mutableListOf<Job>()
    private var goroutinesJob : Job? = null

    val sideEffects = MutableSharedFlow<SideEffect>()
    val state = MutableStateFlow<SetupState>(SetupState.Idle)

    val command = MutableSharedFlow<Command>(replay = 0)

    private var debugClickCount = 0
    private var migrationRetryCount: Int = 0
    private val _fiveClicks = MutableStateFlow(false)

    init {
        Timber.i("OnboardingMnemonicLoginViewModel, init")
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
        Timber.d("onLoginClicked")
        if (state.value !is SetupState.InProgress) {
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
                    sideEffects.emit(SideEffect.Exit)
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
                            Timber.e(exception, "Error while recovering wallet")
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
                    viewModelScope.launch {
                        if (e is AccountIsDeletedException) {
                            sideEffects.emit(
                                value = SideEffect.Error.Unknown(
                                    msg = "This account is deleted. Try using another account or create a new one."
                                )
                            )
                        } else {
                            sideEffects.emit(
                                value = SideEffect.Error.Unknown(
                                    msg = "Error while account loading \n ${e.localizedMessage}"
                                )
                            )
                        }
                        Timber.e(e, "Error while account loading")
                    }
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
                        is AccountMigrationNeededException -> {
                            state.value = SetupState.Migration.AwaitingStart(account = id)
                        }
                        is AccountIsDeletedException -> {
                            sideEffects.emit(value = SideEffect.Error.AccountDeletedError)
                        }
                        is NeedToUpdateApplicationException -> {
                            sideEffects.emit(value = SideEffect.Error.NeedUpdateError)
                        }
                        is LoginException.NetworkIdMismatch -> {
                            sideEffects.emit(SideEffect.Error.NetworkIdMismatch)
                        }
                        is LoginException.FailedToFindAccountInfo -> {
                            sideEffects.emit(SideEffect.Error.SelectVaultError)
                        }
                        else -> {
                            val msg = e.message ?: "Unknown error"
                            sideEffects.emit(
                                value = SideEffect.Error.Unknown(
                                    msg = "${ERROR_MESSAGE}: $msg"
                                )
                            )
                        }
                    }
                },
                success = { (analyticsId, _) ->
                    analytics.proceedWithAccountEvent(
                        startTime = startTime,
                        eventName = EventsDictionary.openAccount,
                        analyticsId = analyticsId
                    )
                    crashReporter.setUser(analyticsId)
                    proceedWithGlobalSubscriptions()
                    navigateToDashboard()
                }
            )
        }
    }

    private suspend fun proceedWithAccountMigration(id: String) {
        proceedWithMigration(
            params = MigrateAccount.Params.Other(acc = id)
        ).collect { migrationState ->
            when (migrationState) {
                is MigrationHelperDelegate.State.Failed -> {
                    state.value = SetupState.Migration.Failed(
                        state = migrationState,
                        account = id
                    )
                }
                is MigrationHelperDelegate.State.InProgress -> {
                    state.value = SetupState.Migration.InProgress(
                        account = id,
                        progress = migrationState
                    )
                }
                is MigrationHelperDelegate.State.Migrated -> {
                    proceedWithSelectingAccount(id)
                }
                is MigrationHelperDelegate.State.Init -> {
                    // Do nothing.
                }
            }
        }
    }

    private fun proceedWithGlobalSubscriptions() {
        globalSubscriptionManager.onStart()
    }

    private fun navigateToDashboard() {
        viewModelScope.launch {
            command.emit(Command.NavigateToVaultScreen)
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

    fun onAccountThraceButtonClicked() {
        jobs += viewModelScope.launch {
            val path = pathProvider.providePath()
            val params = DebugAccountSelectTrace.Params(
                dir = path
            )
            debugAccountSelectTrace.async(params).fold(
                onSuccess = {
                    Timber.d("On account trace success")
                    command.emit(Command.ShowToast("On account trace success"))
                    command.emit(Command.ShareDebugGoroutines(path, uriFileProvider))
                },
                onFailure = {
                    Timber.e(it, "Error while collecting account trace")
                    command.emit(Command.ShowToast("Error while collecting account trace: ${it.message}"))
                }
            )
        }
    }

    fun onRetryMigrationClicked(account: Id) {
        if (state.value !is SetupState.InProgress) {
            migrationRetryCount = migrationRetryCount + 1
            viewModelScope.launch {
                proceedWithAccountMigration(account)
            }
        }
    }

    fun onStartMigrationClicked(account: Id) {
        viewModelScope.launch {
            if (state.value is SetupState.Migration.AwaitingStart) {
                proceedWithAccountMigration(account)
            }
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
            data object SelectVaultError: Error()
            data object AccountDeletedError: Error()
            data object NeedUpdateError: Error()
            data class Unknown(val msg: String): SideEffect()
        }
        data object Exit: SideEffect()
    }

    sealed class SetupState {
        data object Idle : SetupState()
        data object InProgress: SetupState()
        data object Failed: SetupState()
        data object Abort: SetupState()
        sealed class Migration : SetupState() {
            abstract val account: Id
            data class AwaitingStart(override val account: Id) : Migration()
            data class InProgress(
                override val account: Id,
                val progress: MigrationHelperDelegate.State.InProgress
            ): Migration()
            data class Failed(
                val state: MigrationHelperDelegate.State.Failed,
                override val account: Id
            ) : Migration()
        }
    }

    sealed class Command {
        data object Exit : Command()
        data object NavigateToVaultScreen: Command()
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
        private val crashReporter: CrashReporter,
        private val configStorage: ConfigStorage,
        private val localeProvider: LocaleProvider,
        private val debugGoroutines: DebugGoroutines,
        private val uriFileProvider: UriFileProvider,
        private val logout: Logout,
        private val globalSubscriptionManager: GlobalSubscriptionManager,
        private val debugAccountSelectTrace: DebugAccountSelectTrace,
        private val migrationHelperDelegate: MigrationHelperDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicLoginViewModel(
                recoverWallet = recoverWallet,
                convertWallet = convertWallet,
                pathProvider = pathProvider,
                saveMnemonic = saveMnemonic,
                analytics = analytics,
                crashReporter = crashReporter,
                configStorage = configStorage,
                startLoadingAccounts = startLoadingAccounts,
                observeAccounts = observeAccounts,
                selectAccount = selectAccount,
                localeProvider = localeProvider,
                debugGoroutines = debugGoroutines,
                uriFileProvider = uriFileProvider,
                logout = logout,
                globalSubscriptionManager = globalSubscriptionManager,
                debugAccountSelectTrace = debugAccountSelectTrace,
                migrationDelegate = migrationHelperDelegate
            ) as T
        }
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        const val NO_ERROR = ""
    }
}