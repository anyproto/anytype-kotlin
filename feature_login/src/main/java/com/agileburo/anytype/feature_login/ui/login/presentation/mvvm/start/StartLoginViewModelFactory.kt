package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SetupWallet

class StartLoginViewModelFactory(
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StartLoginViewModel(
            setupWallet = setupWallet,
            pathProvider = pathProvider
        ) as T
    }
}