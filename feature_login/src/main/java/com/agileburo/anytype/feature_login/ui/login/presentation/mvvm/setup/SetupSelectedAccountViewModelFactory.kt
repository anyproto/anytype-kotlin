package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SelectAccount

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