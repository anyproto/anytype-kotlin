package com.anytypeio.anytype.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.playbilling.BillingClientLifecycle
import javax.inject.Inject

class PaymentsViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val billingClientLifecycle: BillingClientLifecycle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentsViewModel(
            analytics = analytics,
            billingClientLifecycle = billingClientLifecycle
        ) as T
    }
}