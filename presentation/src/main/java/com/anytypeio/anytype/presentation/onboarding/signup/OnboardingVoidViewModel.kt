package com.anytypeio.anytype.presentation.onboarding.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingVoidViewModel @Inject constructor(
    private val createAccount: CreateAccount,
    private val setupWallet: SetupWallet,
    private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
    private val pathProvider: PathProvider,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val logout: Logout,
    private val analytics: Analytics
): BaseViewModel() {

    val state = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val navigation = MutableSharedFlow<Navigation>()

    fun onNextClicked() {
        if (state.value !is ScreenState.Loading) {
            proceedWithCreatingWallet()
        } else {
            sendToast(LOADING_MSG)
        }
    }

    private fun proceedWithCreatingWallet() {
        state.value = ScreenState.Loading
        setupWallet.invoke(
            scope = viewModelScope,
            params = SetupWallet.Params(
                path = pathProvider.providePath()
            )
        ) { result ->
            result.either(
                fnL = {
                    Timber.e(it, "Error while setting up wallet")
                },
                fnR = {
                    proceedWithCreatingAccount()
                }
            )
        }
    }

    private fun proceedWithCreatingAccount() {
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = "",
                avatarPath = null,
                iconGradientValue = spaceGradientProvider.randomId()
            )
        ) { result ->
            result.either(
                fnL = { error ->
                    Timber.d("Error while creating account: ${error.message}")
                },
                fnR = { account ->
//                    updateUserProps(account.id)
//                    sendAuthEvent(startTime)
                    relationsSubscriptionManager.onStart()
                    objectTypesSubscriptionManager.onStart()
                    proceedWithSettingUpMobileUseCase()
                }
            )
        }
    }

    private fun proceedWithSettingUpMobileUseCase() {
        viewModelScope.launch {
            setupMobileUseCaseSkip.execute(Unit).fold(
                onFailure = {
                    Timber.e(it, "Error while importing use case")
                    state.value = ScreenState.Success
                    navigation.emit(Navigation.NavigateToMnemonic)
                },
                onSuccess = {
                    state.value = ScreenState.Success
                    navigation.emit(Navigation.NavigateToMnemonic)
                }
            )
        }
    }

    fun onBackButtonPressed() {
        Timber.d("onBackButtonPressed!")
        resolveBackNavigation()
    }

    fun onSystemBackPressed() {
        resolveBackNavigation()
    }

    private fun resolveBackNavigation() {
        when(state.value) {
            is ScreenState.Loading -> {
                sendToast(LOADING_MSG)
            }
            is ScreenState.Exiting -> {
                sendToast(EXITING_MSG)
            }
            else -> {
                viewModelScope.launch {
                    state.value = ScreenState.Exiting.Status
                    Timber.d("Checking auth status")
                    checkAuthorizationStatus(Unit).proceed(
                        failure = { e ->
                            navigation.emit(Navigation.GoBack).also {
                                Timber.e(e, "Error while checking auth status")
                            }
                        },
                        success = { status ->
                            when(status) {
                                AuthStatus.AUTHORIZED -> {
                                    Timber.d("Proceeding with logout")
                                    proceedWithLogout()
                                }
                                AuthStatus.UNAUTHORIZED -> {
                                    navigation.emit(Navigation.GoBack)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun proceedWithLogout() {
        logout.invoke(Logout.Params(clearLocalRepositoryData = true)).collect { status ->
            when (status) {
                is Interactor.Status.Started -> {
                    state.value = ScreenState.Exiting.Logout.also {
                        sendToast(EXITING_MSG)
                    }
                }
                else -> {
                    navigation.emit(Navigation.GoBack)
                }
            }
        }
    }

    sealed class Navigation {
        object NavigateToMnemonic: Navigation()
        object GoBack: Navigation()
    }

    class Factory @Inject constructor(
        private val createAccount: CreateAccount,
        private val setupWallet: SetupWallet,
        private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
        private val pathProvider: PathProvider,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        private val checkAuthorizationStatus: CheckAuthorizationStatus,
        private val logout: Logout,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingVoidViewModel(
                createAccount = createAccount,
                setupWallet = setupWallet,
                setupMobileUseCaseSkip = setupMobileUseCaseSkip,
                pathProvider = pathProvider,
                spaceGradientProvider = spaceGradientProvider,
                relationsSubscriptionManager = relationsSubscriptionManager,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager,
                logout = logout,
                checkAuthorizationStatus = checkAuthorizationStatus,
                analytics = analytics
            ) as T
        }
    }

    companion object {
        const val LOADING_MSG = "Loading, please wait."
        const val EXITING_MSG = "Clearing resources, please wait."
    }

    sealed class ScreenState {
        object Idle: ScreenState()
        object Loading: ScreenState()
        object Success: ScreenState()
        sealed class Exiting : ScreenState() {
            object Status : Exiting()
            object Logout: Exiting()
        }
    }
}