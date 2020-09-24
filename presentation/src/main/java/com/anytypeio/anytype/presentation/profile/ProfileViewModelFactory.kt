package com.anytypeio.anytype.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetCurrentAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout

class ProfileViewModelFactory(
    private val logout: Logout,
    private val getCurrentAccount: GetCurrentAccount,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            logout = logout,
            getCurrentAccount = getCurrentAccount,
            analytics = analytics
        ) as T
    }
}