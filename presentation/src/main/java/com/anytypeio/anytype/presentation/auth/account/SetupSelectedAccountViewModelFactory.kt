package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.SelectAccount
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager

class SetupSelectedAccountViewModelFactory(
    private val selectAccount: SelectAccount,
    private val pathProvider: PathProvider,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val crashReporter: com.anytypeio.anytype.CrashReporter
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SetupSelectedAccountViewModel(
            selectAccount = selectAccount,
            pathProvider = pathProvider,
            analytics = analytics,
            relationsSubscriptionManager = relationsSubscriptionManager,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager,
            crashReporter = crashReporter
        ) as T
    }
}