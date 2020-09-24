package com.anytypeio.anytype.presentation.auth.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.device.PathProvider

class StartLoginViewModelFactory(
    private val setupWallet: SetupWallet,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StartLoginViewModel(
            setupWallet = setupWallet,
            pathProvider = pathProvider,
            analytics = analytics
        ) as T
    }
}