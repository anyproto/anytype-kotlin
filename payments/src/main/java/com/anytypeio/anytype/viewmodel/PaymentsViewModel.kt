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
    val codeViewState = MutableStateFlow<PaymentsCodeState>(PaymentsCodeState.Empty)
    val command = MutableStateFlow<PaymentsNavigation?>(null)

    val selectedTier = MutableStateFlow<Tier?>(null)

    init {
        Timber.d("PaymentsViewModel created")
        viewState.value = PaymentsState.Default(
            listOf(
                Tier.Explorer("Free", true),
                Tier.Builder("$9.99/mo", false),
                Tier.CoCreator("$19.99/mo", false),
                Tier.Custom("$29.99/mo", false)
            )
        )
    }

    fun onTierClicked(tier: Tier) {
        selectedTier.value = tier
        command.value = PaymentsNavigation.Tier
    }

    fun onActionCode(code: String) {
        Timber.d("onActionCode: $code")
    }

    fun onPayButtonClicked() {
        command.value = PaymentsNavigation.Code
    }

    fun onDismissTier() {
        command.value = PaymentsNavigation.Dismiss
    }

    fun onDismissCode() {
        command.value = PaymentsNavigation.Dismiss
    }
}