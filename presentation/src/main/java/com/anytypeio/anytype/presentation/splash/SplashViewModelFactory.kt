package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreatePage

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModelFactory(
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchAccount: LaunchAccount,
    private val launchWallet: LaunchWallet,
    private val analytics: Analytics,
    private val storeObjectTypes: StoreObjectTypes,
    private val getLastOpenedObject: GetLastOpenedObject,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val setDefaultEditorType: SetDefaultEditorType,
    private val appActionManager: AppActionManager,
    private val createPage: CreatePage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        SplashViewModel(
            checkAuthorizationStatus = checkAuthorizationStatus,
            launchAccount = launchAccount,
            launchWallet = launchWallet,
            analytics = analytics,
            storeObjectTypes = storeObjectTypes,
            getLastOpenedObject = getLastOpenedObject,
            getDefaultEditorType = getDefaultEditorType,
            setDefaultEditorType = setDefaultEditorType,
            appActionManager = appActionManager,
            createPage = createPage
        ) as T
}