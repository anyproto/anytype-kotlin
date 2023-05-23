package com.anytypeio.anytype.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.exceptions.CreateAccountException
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingInviteCodeViewModel @Inject constructor(
    private val createAccount: CreateAccount,
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
) : ViewModel() {

    val state = MutableStateFlow<InviteCodeViewState>(InviteCodeViewState.WalletCreating)

    private val _navigationFlow = MutableSharedFlow<InviteCodeNavigation>()
    val navigationFlow: SharedFlow<InviteCodeNavigation> = _navigationFlow

    init {
        createWallet()
    }

    private fun createWallet() {
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
                    setupScreen()
                }
            )
        }
    }

    private fun setupScreen() {
        state.value = InviteCodeViewState.Idle
    }

    fun onInviteCodeEntered(inviteCode: String) {
        createAccount.invoke(
            scope = viewModelScope,
            params = CreateAccount.Params(
                name = "",
                avatarPath = null,
                invitationCode = inviteCode,
                iconGradientValue = spaceGradientProvider.randomId()
            )
        ) { result ->
            result.either(
                fnL = { error ->
                    when (error) {
                        CreateAccountException.BadInviteCode -> {
                            state.value = InviteCodeViewState.InvalidCodeError(
                                "Invalid invitation code!"
                            )
                        }
                        CreateAccountException.NetworkError -> {
                            state.value = InviteCodeViewState.ErrorNetwork(
                                "Failed to create your account due to a network error: ${error.message}"
                            )
                        }
                        CreateAccountException.OfflineDevice -> {
                            state.value = InviteCodeViewState.ErrorNetwork(
                                "Your device seems to be offline. Please, check your connection and try again."
                            )
                        }
                        else -> {
                            state.value = InviteCodeViewState.Error(
                                "Error while creating an account: ${error.message ?: "Unknown error"}"
                            )
                        }
                    }
                },
                fnR = { account ->
//                    updateUserProps(account.id)
//                    sendAuthEvent(startTime)
                    relationsSubscriptionManager.onStart()
                    objectTypesSubscriptionManager.onStart()
                    state.value = InviteCodeViewState.Idle
                    navigateTo(InviteCodeNavigation.Void)
                }
            )
        }
    }

    private fun navigateTo(void: InviteCodeNavigation.Void) {
        viewModelScope.launch {
            _navigationFlow.emit(void)
        }
    }

    sealed class InviteCodeViewState {
        object WalletCreating : InviteCodeViewState()
        object Idle : InviteCodeViewState()

        data class Error(val message: String) : InviteCodeViewState()
        data class InvalidCodeError(val message: String) : InviteCodeViewState()
        data class ErrorNetwork(val msg: String) : InviteCodeViewState()
    }

    interface InviteCodeNavigation {
        object Idle : InviteCodeNavigation
        object Void : InviteCodeNavigation
    }

    class Factory @Inject constructor(
        private val createAccount: CreateAccount,
        private val setupWallet: SetupWallet,
        private val pathProvider: PathProvider,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val relationsSubscriptionManager: RelationsSubscriptionManager,
        private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingInviteCodeViewModel(
                createAccount,
                setupWallet,
                pathProvider,
                spaceGradientProvider,
                relationsSubscriptionManager,
                objectTypesSubscriptionManager
            ) as T
        }
    }

}