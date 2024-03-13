package com.anytypeio.anytype.viewmodel

import com.anytypeio.anytype.models.Tier

sealed class PaymentsState {
    object Loading : PaymentsState()
    data class Default(val tiers: List<Tier>) : PaymentsState()
    data class PaymentSuccess(val tiers: List<Tier>) : PaymentsState()
}

sealed class PaymentsCodeState {
    object Empty : PaymentsCodeState()
    object Loading : PaymentsCodeState()
    object Success : PaymentsCodeState()
    data class Error(val message: String) : PaymentsCodeState()
}