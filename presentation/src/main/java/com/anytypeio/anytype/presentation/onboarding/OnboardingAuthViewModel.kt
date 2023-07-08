package com.anytypeio.anytype.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingAuthViewModel @Inject constructor(
    private val createAccount: CreateAccount,
    private val setupWallet: SetupWallet,
    private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
    private val pathProvider: PathProvider,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
) : ViewModel() {

    val sideEffects = MutableSharedFlow<SideEffect>()

    private val _navigationFlow = MutableSharedFlow<AuthNavigation>()
    val navigationFlow: SharedFlow<AuthNavigation> = _navigationFlow

    val joinFlowState = MutableStateFlow<JoinFlowState>(JoinFlowState.Active)

    fun onJoinClicked() {
        joinFlowState.value = JoinFlowState.Loading
        proceedWithCreatingWallet()
    }

    fun onLoginClicked() {
        navigateTo(AuthNavigation.ProceedWithSignIn)
    }

    fun onPrivacyPolicyClicked() {
        viewModelScope.launch { sideEffects.emit(SideEffect.OpenPrivacyPolicy) }
    }

    fun onTermsOfUseClicked() {
        viewModelScope.launch { sideEffects.emit(SideEffect.OpenTermsOfUse) }
    }

    private fun proceedWithCreatingWallet() {
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
                    joinFlowState.value = JoinFlowState.Active
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
                    navigateTo(AuthNavigation.ProceedWithSignUp)
                },
                onSuccess = {
                    navigateTo(AuthNavigation.ProceedWithSignUp)
                }
            )
        }
    }

    private fun navigateTo(destination: AuthNavigation) {
        viewModelScope.launch {
            _navigationFlow.emit(destination)
        }
    }

    interface AuthNavigation {
        object Idle : AuthNavigation
        object ProceedWithSignUp : AuthNavigation
        object ProceedWithSignIn : AuthNavigation
    }

    sealed class SideEffect {
        object OpenPrivacyPolicy : SideEffect()
        object OpenTermsOfUse : SideEffect()
    }

    interface JoinFlowState {
        object Active : JoinFlowState
        object Loading : JoinFlowState
    }

    class Factory @Inject constructor(
        private val createAccount: CreateAccount,
        private val setupWallet: SetupWallet,
        private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip,
        private val pathProvider: PathProvider,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingAuthViewModel(
                createAccount = createAccount,
                setupWallet = setupWallet,
                setupMobileUseCaseSkip = setupMobileUseCaseSkip,
                pathProvider = pathProvider,
                spaceGradientProvider = spaceGradientProvider,
                relationsSubscriptionManager = relationsSubscriptionManager,
                objectTypesSubscriptionManager = objectTypesSubscriptionManager
            ) as T
        }
    }
}