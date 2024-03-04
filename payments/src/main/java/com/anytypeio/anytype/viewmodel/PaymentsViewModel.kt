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
        viewState.value = PaymentsState.Success(
            listOf(
                TierState.Explorer("Free", true),
                TierState.Builder("$9.99/mo", false),
                TierState.CoCreator("$19.99/mo", false),
                TierState.Custom("$29.99/mo", false)
            )
        )
    }

    interface PaymentsNavigation {
        object MembershipMain : PaymentsNavigation
        object MembershipLevel : PaymentsNavigation
    }
}