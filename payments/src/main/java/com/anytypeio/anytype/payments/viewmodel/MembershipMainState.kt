package com.anytypeio.anytype.payments.viewmodel

import androidx.annotation.StringRes
import com.anytypeio.anytype.core_models.membership.MembershipErrors
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
    data object SubmitClicked : TierAction()
    data class ManagePayment(val tierId: TierId) : TierAction()
    data class OpenUrl(val url: String?) : TierAction()
    data object OpenEmail : TierAction()
    data object OnResendCodeClicked : TierAction()
    data class OnVerifyCodeClicked(val code: String) : TierAction()
    data object ChangeEmail : TierAction()
}

sealed class MembershipEmailCodeState {
    data object Hidden : MembershipEmailCodeState()

    sealed class Visible : MembershipEmailCodeState() {

        data object Initial : Visible()
        data object Loading : Visible()
        data object Success : Visible()
        data class Error(val error: MembershipErrors.VerifyEmailCode) : Visible()
        data class ErrorOther(val message: String?) : Visible()
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