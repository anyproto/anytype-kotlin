package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import javax.inject.Inject

class SpacesStorageViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val spacesUsageInfo: SpacesUsageInfo,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val getAccount: GetAccount,
    private val membershipProvider: MembershipProvider,
    private val userPermissionProvider: UserPermissionProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T = SpacesStorageViewModel(
        analytics = analytics,
        spaceManager = spaceManager,
        appCoroutineDispatchers = appCoroutineDispatchers,
        spacesUsageInfo = spacesUsageInfo,
        interceptFileLimitEvents = interceptFileLimitEvents,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        getAccount = getAccount,
        membershipProvider = membershipProvider,
        userPermissionProvider = userPermissionProvider
    ) as T
}