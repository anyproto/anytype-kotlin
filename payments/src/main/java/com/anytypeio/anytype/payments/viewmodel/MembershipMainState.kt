package com.anytypeio.anytype.payments.viewmodel

import androidx.annotation.StringRes
import com.anytypeio.anytype.presentation.membership.models.TierId


sealed class MembershipMainState {
    data object Loading : MembershipMainState()
    data class Default(
        @StringRes val title: Int,
        @StringRes val subtitle: Int?,
        val showBanner: Boolean,
        val tiers: List<MembershipTierPreviewView>,
        val membershipLevelDetails: String,
        val privacyPolicy: String,
        val termsOfService: String,
        val contactEmail: String
    ) : MembershipMainState()

    data class ErrorState(val message: String) : MembershipMainState()
}

sealed class MembershipTierState {
    data object Hidden : MembershipTierState()

    data class Visible(val tierView: MembershipTierView) : MembershipTierState() {

//        sealed class Inactive : Visible() {
////            data class AvailableToSubscribeOnAndroid(
////                override val tier: Tier,
////                val nameState: MembershipNameState
////            ) : Inactive()
//
//            data class UnavailableToSubscribeOnAndroid(override val tierView: MembershipTierView) : Inactive()
//        }
//
////        sealed class Active : Visible() {
////            data class AvailableToManageOnAndroid(override val tier: Tier) : Active()
////            data class UnavailableToManageOnAndroid(override val tier: Tier) : Active()
////        }
    }
}

sealed class MembershipNameState {
    data class Default(val name: String) : MembershipNameState()
    data class Validating(val name: String) : MembershipNameState()
    data class Validated(val name: String) : MembershipNameState()
    data class Error(val name: String, val message: String) : MembershipNameState()
}

sealed class PaymentsErrorState {
    object Hidden : PaymentsErrorState()
    data class TierNotFound(val tierId: TierId) : PaymentsErrorState()
    data object MembershipStatusEmpty : PaymentsErrorState()
}

sealed class TierAction {
    data class UpdateName(val tierId: TierId, val name: String) : TierAction()
    data class PayClicked(val tierId: TierId) : TierAction()
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