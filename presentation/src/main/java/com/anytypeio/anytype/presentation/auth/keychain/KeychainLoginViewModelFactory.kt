package com.anytypeio.anytype.presentation.auth.keychain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.ConvertWallet
import com.anytypeio.anytype.domain.auth.interactor.RecoverWallet
import com.anytypeio.anytype.domain.auth.interactor.SaveMnemonic
import com.anytypeio.anytype.domain.device.PathProvider

class KeychainLoginViewModelFactory(
    private val pathProvider: PathProvider,
    private val convertWallet: ConvertWallet,
    private val recoverWallet: RecoverWallet,
    private val saveMnemonic: SaveMnemonic,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KeychainLoginViewModel(
            recoverWallet = recoverWallet,
            convertWallet = convertWallet,
            pathProvider = pathProvider,
            saveMnemonic = saveMnemonic,
            analytics = analytics
        ) as T
    }
}