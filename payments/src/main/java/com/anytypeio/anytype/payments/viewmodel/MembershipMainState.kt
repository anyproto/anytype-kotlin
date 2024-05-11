package com.anytypeio.anytype.payments.viewmodel

import androidx.annotation.StringRes
import com.anytypeio.anytype.payments.models.TierPreviewView
import com.anytypeio.anytype.payments.models.TierView
import com.anytypeio.anytype.presentation.membership.models.TierId


sealed class MembershipMainState {
    data object Loading : MembershipMainState()
    data class Default(
        @StringRes val title: Int,
        @StringRes val subtitle: Int?,
        val showBanner: Boolean,
        val tiers: List<TierPreviewView>,
        val membershipLevelDetails: String,
        val privacyPolicy: String,
        val termsOfService: String,
        val contactEmail: String
    ) : MembershipMainState()

    data class ErrorState(val message: String) : MembershipMainState()
}

sealed class MembershipTierState {
    data object Hidden : MembershipTierState()
    data class Visible(val tierView: TierView) : MembershipTierState() {
    }
}

sealed class MembershipNameState {
    data class Default(val name: String) : MembershipNameState()
    data class Validating(val name: String) : MembershipNameState()
    data class Validated(val name: String) : MembershipNameState()
    data class Error(val name: String, val message: String) : MembershipNameState()
}

sealed class PaymentsErrorState {
    data object Hidden : PaymentsErrorState()
    data class TierNotFound(val message: String) : PaymentsErrorState()
    data object MembershipStatusEmpty : PaymentsErrorState()
}

sealed class TierAction {
    data class PayClicked(val tierId: TierId) : TierAction()
    data class ManagePayment(val tierId: TierId) : TierAction()
    data class OpenUrl(val url: String?) : TierAction()
    data object OpenEmail : TierAction()
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
    data object Hidden : PaymentsWelcomeState()
    data class Initial(val tier: TierView) : PaymentsWelcomeState()
}

sealed class PaymentsNavigation(val route: String) {
    data object Main : PaymentsNavigation("main")
    data object Tier : PaymentsNavigation("tier")
    data object Code : PaymentsNavigation("code")
    data object Welcome : PaymentsNavigation("welcome")
    data object Dismiss : PaymentsNavigation("")
    data class OpenUrl(val url: String?) : PaymentsNavigation("")
    data class OpenEmail(val accountId: String?) : PaymentsNavigation("")
}