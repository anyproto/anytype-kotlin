package com.anytypeio.anytype.presentation.onboarding.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.common.ViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicLoginViewModel @Inject constructor(
    private val recoverWallet: RecoverWallet,
    private val convertWallet: ConvertWallet,
    private val saveMnemonic: SaveMnemonic,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModel() {

    val sideEffects = MutableSharedFlow<SideEffect>()
    val state = MutableLiveData<ViewState<Boolean>>()

    init {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.loginScreenShow
        )
    }

    fun onLoginClicked(chain: String) {
        proceedWithRecoveringWallet(chain.trim())
    }

    fun onActionDone(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onBackButtonPressed() {
        viewModelScope.launch {
            sideEffects.emit(SideEffect.Exit)
        }
    }

    fun onGetEntropyFromQRCode(entropy: String) {
        viewModelScope.launch {
            convertWallet(
                params = ConvertWallet.Request(entropy)
            ).proceed(
                failure = { Timber.e(it, "Error while convert wallet") },
                success = { mnemonic -> proceedWithRecoveringWallet(mnemonic) }
            )
        }
    }

    private fun proceedWithRecoveringWallet(chain: String) {
        state.postValue(ViewState.Loading)

        recoverWallet.invoke(
            scope = viewModelScope,
            params = RecoverWallet.Params(
                path = pathProvider.providePath(),
                mnemonic = chain
            )
        ) { result ->
            result.either(
                fnR = {
                    state.postValue(ViewState.Success(true))
                    proceedWithSavingMnemonic(chain)
                },
                fnL = {
                    state.postValue(ViewState.Error(it.localizedMessage.orEmpty()))
                    Timber.e(it, "Error while recovering wallet")
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
                    viewModelScope.launch {
                        sideEffects.emit(SideEffect.ProceedWithLogin)
                    }
                },
                fnL = { Timber.e(it, "Error while saving mnemonic") }
            )
        }
    }

    sealed class SideEffect {
        object ProceedWithLogin : SideEffect()
        object Exit: SideEffect()
    }

    class Factory @Inject constructor(
        private val pathProvider: PathProvider,
        private val convertWallet: ConvertWallet,
        private val recoverWallet: RecoverWallet,
        private val saveMnemonic: SaveMnemonic,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicLoginViewModel(
                recoverWallet = recoverWallet,
                convertWallet = convertWallet,
                pathProvider = pathProvider,
                saveMnemonic = saveMnemonic,
                analytics = analytics
            ) as T
        }
    }
}