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

    val viewState = MutableStateFlow<PaymentsMainState>(PaymentsMainState.Loading)
    val codeState = MutableStateFlow<PaymentsCodeState>(PaymentsCodeState.Hidden)
    val tierState = MutableStateFlow<PaymentsTierState>(PaymentsTierState.Hidden)
    val welcomeState = MutableStateFlow<PaymentsWelcomeState>(PaymentsWelcomeState.Hidden)

    val command = MutableStateFlow<PaymentsNavigation?>(null)

    private val _tiers = mutableListOf<Tier>()

    var activeTierName: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        Timber.d("PaymentsViewModel init")

        _tiers.addAll(gertTiers())
        setupActiveTierName()
        viewState.value = PaymentsMainState.Default(_tiers)
    }

    fun onTierClicked(tierId: TierId) {
        Timber.d("onTierClicked: tierId:$tierId")
        tierState.value = PaymentsTierState.Visible.Initial(tier = _tiers.first { it.id == tierId })
        command.value = PaymentsNavigation.Tier
    }

    fun onActionCode(code: String, tierId: TierId) {
        Timber.d("onActionCode: tierId:$tierId, code:$code, _tiers:${_tiers}")
        viewModelScope.launch {
            codeState.value = PaymentsCodeState.Visible.Loading(tierId = tierId)
            delay(2000)
            welcomeState.value =
                PaymentsWelcomeState.Initial(tier = _tiers.first { it.id == tierId })
            val updatedTiers = _tiers.map {
                val isCurrent = it.id == tierId
                when (it) {
                    is Tier.Builder -> it.copy(isCurrent = isCurrent)
                    is Tier.CoCreator -> it.copy(isCurrent = isCurrent)
                    is Tier.Custom -> it.copy(isCurrent = isCurrent)
                    is Tier.Explorer -> it.copy(isCurrent = isCurrent)
                }
            }
            _tiers.clear()
            _tiers.addAll(updatedTiers)
            viewState.value = PaymentsMainState.PaymentSuccess(_tiers)
            command.value = PaymentsNavigation.Welcome
        }
    }

    fun onSubmitEmailButtonClicked(tierId: TierId, email: String) {
        Timber.d("onSubmitEmailButtonClicked: email:$email")
        codeState.value = PaymentsCodeState.Visible.Initial(tierId = tierId)
        command.value = PaymentsNavigation.Code
    }

    fun onPayButtonClicked(tierId: TierId) {
        Timber.d("onPayButtonClicked: tierId:$tierId")
        codeState.value = PaymentsCodeState.Visible.Initial(tierId = tierId)
        command.value = PaymentsNavigation.Code
    }

    fun onDismissTier() {
        Timber.d("onDismissTier")
        command.value = PaymentsNavigation.Dismiss
    }

    fun onDismissCode() {
        Timber.d("onDismissCode")
        command.value = PaymentsNavigation.Dismiss
    }

    fun onDismissWelcome() {
        Timber.d("onDismissWelcome")
        command.value = PaymentsNavigation.Dismiss
    }

    private fun setupActiveTierName() {
        activeTierName.value = _tiers.firstOrNull { it.isCurrent }?.prettyName
    }

    private fun gertTiers(): List<Tier> {
        return listOf(
            Tier.Explorer(id = TierId("idExplorer"), isCurrent = false, validUntil = "Forever"),
            Tier.Builder(id = TierId("idBuilder"), isCurrent = false, validUntil = "2022-12-31"),
            Tier.CoCreator(id = TierId("idCoCreator"), isCurrent = false, validUntil = "2022-12-31"),
            Tier.Custom(id = TierId("idCustom"), isCurrent = false, validUntil = "2022-12-31")
        )
    }
}