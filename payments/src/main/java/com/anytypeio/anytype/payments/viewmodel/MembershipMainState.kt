package com.anytypeio.anytype.payments.viewmodel

import androidx.annotation.StringRes
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.payments.models.TierPreview
import com.anytypeio.anytype.payments.models.Tier
import com.anytypeio.anytype.core_models.membership.TierId


sealed class MembershipMainState {
    data object Loading : MembershipMainState()
    data class Default(
        @StringRes val title: Int,
        @StringRes val subtitle: Int?,
        val showBanner: Boolean,
        val tiersPreview: List<TierPreview>,
        val tiers: List<Tier>,
        val membershipLevelDetails: String,
        val privacyPolicy: String,
        val termsOfService: String,
        val contactEmail: String
    ) : MembershipMainState()

    data class ErrorState(val message: String) : MembershipMainState()
}

sealed class MembershipTierState {
    data object Hidden : MembershipTierState()
    data class Visible(val tier: Tier) : MembershipTierState() {
    }
}

sealed class MembershipNameState {
    data class Default(val name: String) : MembershipNameState()
    data class Validating(val name: String) : MembershipNameState()
    data class Validated(val name: String) : MembershipNameState()
    data class Error(val name: String, val message: String) : MembershipNameState()
}

sealed class MembershipErrorState {
    data object Hidden : MembershipErrorState()
    data class Show(val message: String) : MembershipErrorState()
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
    data class ContactUsError(val error: String): TierAction()
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

sealed class WelcomeState {
    data object Hidden : WelcomeState()
    data class Initial(val tier: Tier) : WelcomeState()
}

sealed class MembershipNavigation(val route: String) {
    data object Main : MembershipNavigation("main")
    data object Tier : MembershipNavigation("tier")
    data object Code : MembershipNavigation("code")
    data object Welcome : MembershipNavigation("welcome")
    data object Dismiss : MembershipNavigation("")
    data class OpenUrl(val url: String?) : MembershipNavigation("")
    data class OpenEmail(val accountId: String?) : MembershipNavigation("")
    data class OpenErrorEmail(val error: String, val accountId: String?) : MembershipNavigation("")
}