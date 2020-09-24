package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.StartAccount
import com.anytypeio.anytype.domain.device.PathProvider

class SetupSelectedAccountViewModelFactory(
    private val startAccount: StartAccount,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetupSelectedAccountViewModel(
            startAccount = startAccount,
            pathProvider = pathProvider,
            analytics = analytics
        ) as T
    }
}