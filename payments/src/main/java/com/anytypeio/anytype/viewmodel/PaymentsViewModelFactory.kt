package com.anytypeio.anytype.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import javax.inject.Inject

class PaymentsViewModelFactory @Inject constructor(
    private val analytics: Analytics,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentsViewModel(
            analytics = analytics,
        ) as T
    }
}