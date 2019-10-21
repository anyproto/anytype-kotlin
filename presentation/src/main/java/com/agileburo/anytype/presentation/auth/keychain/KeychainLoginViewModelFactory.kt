package com.agileburo.anytype.presentation.auth.keychain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.RecoverWallet
import com.agileburo.anytype.domain.auth.interactor.SaveMnemonic
import com.agileburo.anytype.domain.auth.repo.PathProvider

class KeychainLoginViewModelFactory(
    private val pathProvider: PathProvider,
    private val recoverWallet: RecoverWallet,
    private val saveMnemonic: SaveMnemonic
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return KeychainLoginViewModel(
            recoverWallet = recoverWallet,
            pathProvider = pathProvider,
            saveMnemonic = saveMnemonic
        ) as T
    }
}