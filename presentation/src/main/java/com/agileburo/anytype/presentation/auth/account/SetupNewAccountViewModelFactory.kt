package com.agileburo.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.CreateAccount
import com.agileburo.anytype.presentation.auth.model.Session

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