package com.agileburo.anytype.presentation.keychain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.auth.interactor.GetMnemonic
import timber.log.Timber

class KeychainPhraseViewModel(
    private val getMnemonic: GetMnemonic
) : ViewStateViewModel<ViewState<String>>() {

    init {
        proceedWithGettingMnemonic()
    }

    private fun proceedWithGettingMnemonic() {
        getMnemonic.invoke(viewModelScope, Unit) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting mnemonic") },
                fnR = { stateData.postValue(ViewState.Success(it)) }
            )
        }
    }
}

class KeychainPhraseViewModelFactory(
    private val getMnemonic: GetMnemonic
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return KeychainPhraseViewModel(
            getMnemonic = getMnemonic
        ) as T
    }
}