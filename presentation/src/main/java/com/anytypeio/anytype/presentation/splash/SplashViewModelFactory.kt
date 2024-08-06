package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import javax.inject.Inject

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModelFactory @Inject constructor(
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchAccount: LaunchAccount,
    private val launchWallet: LaunchWallet,
    private val analytics: Analytics,
    private val getLastOpenedObject: GetLastOpenedObject,
    private val createObject: CreateObject,
    private val crashReporter: CrashReporter,
    private val localeProvider: LocaleProvider,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val globalSubscriptionManager: GlobalSubscriptionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics,
            getLastOpenedObject = getLastOpenedObject,
            createObject = createObject,
            crashReporter = crashReporter,
            localeProvider = localeProvider,
            spaceManager = spaceManager,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            globalSubscriptionManager = globalSubscriptionManager
        ) as T
}