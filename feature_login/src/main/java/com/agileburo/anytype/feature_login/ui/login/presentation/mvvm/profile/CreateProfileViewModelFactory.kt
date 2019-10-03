package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.feature_login.ui.login.domain.common.Session

class CreateProfileViewModelFactory(
    private val session: Session
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateProfileViewModel(
            session = session
        ) as T
    }
}