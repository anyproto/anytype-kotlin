package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.ObserveAccounts
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.RecoverAccount

class SelectAccountViewModelFactory(
    private val recoverAccount: RecoverAccount,
    private val observeAccounts: ObserveAccounts
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectAccountViewModel(
            recoverAccount = recoverAccount,
            observeAccounts = observeAccounts
        ) as T
    }
}