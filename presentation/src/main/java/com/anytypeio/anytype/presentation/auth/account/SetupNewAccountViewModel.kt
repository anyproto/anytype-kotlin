package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class SetupNewAccountViewState {
    object Loading : SetupNewAccountViewState()
    object Success : SetupNewAccountViewState()
    data class Error(val message: String) : SetupNewAccountViewState()
    data class InvalidCodeError(val message: String) : SetupNewAccountViewState()
    data class ErrorNetwork(val msg: String) : SetupNewAccountViewState()
}

class SetupNewAccountViewModel(
    private val session: Session,
    private val createAccount: CreateAccount,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val configStorage: ConfigStorage,
    private val crashReporter: CrashReporter,
    private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    private val _state = MutableLiveData<SetupNewAccountViewState>()
    val state: LiveData<SetupNewAccountViewState>
        get() = _state

    init {
        _state.postValue(SetupNewAccountViewState.Loading)
        proceedWithCreatingAccount()
    }

    fun onRetryClicked() {
        _state.postValue(SetupNewAccountViewState.Loading)
        proceedWithCreatingAccount()
    }

    private fun proceedWithCreatingAccount() {
        Timber.d("Starting setting up new account")
        val startTime = System.currentTimeMillis()
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = session.name ?: throw IllegalStateException("Name not set"),
                avatarPath = session.avatarPath,
                iconGradientValue = spaceGradientProvider.randomId()
            )
        ) { result ->
            result.either(
                fnL = { error ->
                    when (error) {
                        CreateAccountException.BadInviteCode -> {
                            _state.postValue(SetupNewAccountViewState.InvalidCodeError("Invalid invitation code!"))
                            viewModelScope.launch {
                                delay(300)
                                navigation.postValue(EventWrapper(AppNavigation.Command.ExitToInvitationCodeScreen))
                            }
                        }
                        CreateAccountException.NetworkError -> {
                            _state.postValue(
                                SetupNewAccountViewState.ErrorNetwork(
                                    "Failed to create your account due to a network error: ${error.message}"
                                )
                            )
                        }
                        CreateAccountException.OfflineDevice -> {
                            _state.postValue(
                                SetupNewAccountViewState.ErrorNetwork(
                                    "Your device seems to be offline. Please, check your connection and try again."
                                )
                            )
                        }
                        else -> {
                            _state.postValue(
                                SetupNewAccountViewState.Error(
                                    "Error while creating an account: ${error.message ?: "Unknown error"}"
                                )
                            )
                        }
                    }
                    Timber.e(error, "Error while creating account")
                },
                fnR = {
                    createAccountAnalytics(startTime)
                    crashReporter.setUser(configStorage.get().analytics)
                    _state.postValue(SetupNewAccountViewState.Success)
                    relationsSubscriptionManager.onStart()
                    objectTypesSubscriptionManager.onStart()
                    setupUseCase()
                }
            )
        }
    }

    private fun setupUseCase() {
        viewModelScope.launch {
            setupMobileUseCaseSkip.execute(Unit).fold(
                onFailure = {
                    Timber.e(it, "Error while importing use case")
                    navigateToDashboard()
                },
                onSuccess = {
                    navigateToDashboard()
                }
            )
        }
    }

    private fun createAccountAnalytics(startTime: Long) {
        viewModelScope.launch {
            analytics.proceedWithAccountEvent(
                startTime = startTime,
                configStorage = configStorage,
                eventName = EventsDictionary.createAccount
            )
        }
    }

    private fun navigateToDashboard() {
        navigation.postValue(EventWrapper(AppNavigation.Command.StartDesktopFromSignUp))
    }
}