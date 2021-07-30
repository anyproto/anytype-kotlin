package com.anytypeio.anytype.presentation.auth.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.block.interactor.sets.StoreObjectTypes
import com.anytypeio.anytype.presentation.auth.model.Session

class SetupNewAccountViewModelFactory(
    private val createAccount: CreateAccount,
    private val session: Session,
    private val analytics: Analytics,
    private val storeObjectTypes: StoreObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SetupNewAccountViewModel(
            createAccount = createAccount,
            session = session,
            analytics = analytics,
            storeObjectTypes = storeObjectTypes
        ) as T
    }
}