package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SelectAccount

class SetupSelectedAccountViewModelFactory(
    private val selectAccount: SelectAccount
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetupSelectedAccountViewModel(
            selectAccount = selectAccount
        ) as T
    }
}