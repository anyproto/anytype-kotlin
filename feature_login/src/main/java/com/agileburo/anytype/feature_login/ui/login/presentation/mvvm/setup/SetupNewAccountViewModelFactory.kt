package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.CreateAccount

class SetupNewAccountViewModelFactory(
    private val createAccount: CreateAccount,
    private val session: Session
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetupNewAccountViewModel(
            createAccount = createAccount,
            session = session
        ) as T
    }
}