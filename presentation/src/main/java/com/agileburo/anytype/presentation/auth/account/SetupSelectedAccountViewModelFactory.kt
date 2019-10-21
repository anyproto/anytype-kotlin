package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.SelectAccount
import com.agileburo.anytype.domain.auth.repo.PathProvider

class SetupSelectedAccountViewModelFactory(
    private val selectAccount: SelectAccount,
    private val pathProvider: PathProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetupSelectedAccountViewModel(
            selectAccount = selectAccount,
            pathProvider = pathProvider
        ) as T
    }
}