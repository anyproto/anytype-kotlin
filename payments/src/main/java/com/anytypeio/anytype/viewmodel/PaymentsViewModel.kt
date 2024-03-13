package com.anytypeio.anytype.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.models.Tier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PaymentsViewModel(
    private val analytics: Analytics,
) : ViewModel() {

    val viewState = MutableStateFlow<PaymentsState>(PaymentsState.Loading)
    val codeViewState = MutableStateFlow<PaymentsCodeState>(PaymentsCodeState.Empty)
    val command = MutableStateFlow<PaymentsNavigation?>(null)

    val showTier = MutableStateFlow<Tier?>(null)

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

    fun onActionCode(code: String) {
        codeViewState.value = PaymentsCodeState.Loading
        Timber.d("onActionCode: $code")
        viewModelScope.launch {
            codeViewState.value = PaymentsCodeState.Loading
            delay(2000)
            command.value = PaymentsNavigation.MembershipMain
        }
    }

    sealed class PaymentsNavigation {
        object MembershipMain : PaymentsNavigation()
        object MembershipLevel : PaymentsNavigation()
    }
}