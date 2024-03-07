package com.anytypeio.anytype.viewmodel

import com.anytypeio.anytype.models.Tier

sealed class PaymentsState {
    object Loading : PaymentsState()
    data class Success(val tiers: List<Tier>) : PaymentsState()
}