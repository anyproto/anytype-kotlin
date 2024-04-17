package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.presentation.membership.models.Tier
import com.anytypeio.anytype.presentation.membership.models.TierId


sealed class PaymentsMainState {
    data object Loading : PaymentsMainState()
    sealed class Default : PaymentsMainState() {
        data class WithBanner(val tiers: List<Tier>) : Default()
        data class WithoutBanner(val tiers: List<Tier>) : Default()
    }
    data class ErrorState(val message: String) : PaymentsMainState()
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