package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session

class CreateAccountViewModelFactory(
    private val session: Session
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateAccountViewModel(
            session = session
        ) as T
    }
}