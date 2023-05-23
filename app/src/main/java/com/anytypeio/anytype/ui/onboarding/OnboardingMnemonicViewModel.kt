package com.anytypeio.anytype.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingMnemonicViewModel @Inject constructor(
    private val getMnemonic: GetMnemonic
) : ViewModel() {

    val state = MutableStateFlow<State>(State.Idle)

    init {
        viewModelScope.launch {
            proceedWithMnemonicPhrase()
        }
    }

    fun openMnemonic() {
        state.value = (state.value as? State.Mnemonic)?.copy(
            visible = true
        ) ?: state.value
    }

    private suspend fun proceedWithMnemonicPhrase() {
        getMnemonic.invoke(Unit).proceed(
            failure = { e -> Timber.e(e, "Error while getting mnemonic") },
            success = { mnemonic ->
                state.value = State.Mnemonic(mnemonic)
                Any()
            }
        )
    }


    sealed class State {
        object Idle : State()
        data class Mnemonic(val mnemonicPhrase: String, val visible: Boolean = false) : State()
    }

    class Factory @Inject constructor(
        private val getMnemonic: GetMnemonic
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingMnemonicViewModel(
                getMnemonic = getMnemonic
            ) as T
        }
    }

}