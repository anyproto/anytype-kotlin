package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.keychain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.RecoverWallet
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SaveMnemonic

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