package com.agileburo.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.agileburo.anytype.domain.auth.interactor.LaunchAccount
import com.agileburo.anytype.domain.auth.interactor.LaunchWallet

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModelFactory(
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchAccount: LaunchAccount,
    private val launchWallet: LaunchWallet,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics
        ) as T
}