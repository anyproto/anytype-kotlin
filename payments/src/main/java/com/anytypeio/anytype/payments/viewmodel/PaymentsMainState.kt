package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.payments.models.Tier


sealed class PaymentsMainState {
    object Loading : PaymentsMainState()
    data class Default(val tiers: List<Tier>) : PaymentsMainState()
    data class PaymentSuccess(val tiers: List<Tier>) : PaymentsMainState()
}

sealed class PaymentsTierState {
    object Hidden : PaymentsTierState()

    sealed class Visible : PaymentsTierState() {
        abstract val tier: Tier

        data class Initial(override val tier: Tier) : Visible()
        data class Subscribed(override val tier: Tier) : Visible()
    }
}

sealed class PaymentsCodeState {
    object Hidden : PaymentsCodeState()

    sealed class Visible : PaymentsCodeState() {
        abstract val tierId: TierId

        data class Initial(override val tierId: TierId) : Visible()
        data class Loading(override val tierId: TierId) : Visible()
        data class Success(override val tierId: TierId) : Visible()
        data class Error(override val tierId: TierId, val message: String) : Visible()
    }
}

sealed class PaymentsWelcomeState {
    object Hidden : PaymentsWelcomeState()
    data class Initial(val tier: Tier) : PaymentsWelcomeState()
}

sealed class PaymentsNavigation(val route: String) {
    object Main : PaymentsNavigation("main")
    object Tier : PaymentsNavigation("tier")
    object Code : PaymentsNavigation("code")
    object Welcome : PaymentsNavigation("welcome")
    object Dismiss : PaymentsNavigation("")
}

@JvmInline
value class TierId(val value: String)