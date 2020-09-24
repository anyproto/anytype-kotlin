package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.auth.interactor.ObserveAccounts
import com.anytypeio.anytype.domain.auth.interactor.StartLoadingAccounts

class SelectAccountViewModelFactory(
    private val startLoadingAccounts: StartLoadingAccounts,
    private val observeAccounts: ObserveAccounts
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectAccountViewModel(
            startLoadingAccounts = startLoadingAccounts,
            observeAccounts = observeAccounts
        ) as T
    }
}