package com.anytypeio.anytype.payments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.payments.GetMembershipTiers
import com.anytypeio.anytype.payments.playbilling.BillingClientLifecycle
import javax.inject.Inject

class PaymentsViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle,
    private val getAccount: GetAccount,
    private val getMembershipTiers: GetMembershipTiers
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentsViewModel(
            analytics = analytics,
            billingClientLifecycle = billingClientLifecycle,
            getAccount = getAccount,
            getMembershipTiers = getMembershipTiers
        ) as T
    }
}