package com.anytypeio.anytype.viewmodel

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.models.Tier
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class PaymentsViewModel(
    private val analytics: Analytics,
) : ViewModel() {

    val viewState = MutableStateFlow<PaymentsState>(PaymentsState.Loading)

    val showTier = MutableStateFlow<Tier?>(null)

    init {
        Timber.d("PaymentsViewModel created")
        viewState.value = PaymentsState.Success(
            listOf(
                Tier.Explorer("Free", true),
                Tier.Builder("$9.99/mo", false),
                Tier.CoCreator("$19.99/mo", false),
                Tier.Custom("$29.99/mo", false)
            )
        )
    }

    interface PaymentsNavigation {
        object MembershipMain : PaymentsNavigation
        object MembershipLevel : PaymentsNavigation
    }
}