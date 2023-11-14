package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.auth.model.Session
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider

@Deprecated("To be deleted")
class SetupNewAccountViewModelFactory(
    private val createAccount: CreateAccount,
    private val session: Session,
    private val analytics: Analytics,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val configStorage: ConfigStorage,
    private val crashReporter: CrashReporter,
    private val setupMobileUseCaseSkip: SetupMobileUseCaseSkip
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SetupNewAccountViewModel(
            createAccount = createAccount,
            session = session,
            analytics = analytics,
            relationsSubscriptionManager = relationsSubscriptionManager,
            objectTypesSubscriptionManager = objectTypesSubscriptionManager,
            spaceGradientProvider = spaceGradientProvider,
            configStorage = configStorage,
            crashReporter = crashReporter,
            setupMobileUseCaseSkip = setupMobileUseCaseSkip
        ) as T
    }
}