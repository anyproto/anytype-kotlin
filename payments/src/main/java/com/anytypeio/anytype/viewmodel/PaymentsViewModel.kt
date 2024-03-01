package com.anytypeio.anytype.viewmodel

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.analytics.base.Analytics
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class PaymentsViewModel(
    private val analytics: Analytics,
) : ViewModel() {

    val viewState = MutableStateFlow<PaymentsState>(PaymentsState.Loading)

    init {
        Timber.d("PaymentsViewModel created")
    }

    interface PaymentsNavigation {
        object MembershipMain : PaymentsNavigation
        object MembershipLevel : PaymentsNavigation
    }
}