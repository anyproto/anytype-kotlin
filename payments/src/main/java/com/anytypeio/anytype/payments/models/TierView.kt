package com.anytypeio.anytype.payments.models

import androidx.annotation.StringRes
import com.anytypeio.anytype.core_models.membership.MembershipErrors
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.presentation.membership.models.TierId

//This is a data class that represents a tier preview view in the main Membership screen
data class TierPreviewView(
    val id: TierId,
    val isActive: Boolean,
    val title: String,
    val subtitle: String,
    val conditionInfo: TierConditionInfo,
    val color: String = "red"
)

//This is a data class that represents a tier view when tier is opened
data class TierView(
    val id: TierId,
    val isActive: Boolean,
    val title: String,
    val subtitle: String,
    val conditionInfo: TierConditionInfo,
    val features: List<String>,
    val membershipAnyName: TierAnyName,
    val buttonState: TierButton,
    val email: TierEmail,
    val color: String = "red",
    val urlInfo: String? = null
)

sealed class TierConditionInfo {
    data object Hidden : TierConditionInfo()
    sealed class Visible : TierConditionInfo() {
        data object LoadingBillingClient : Visible()
        data class Valid(val period: TierPeriod, val dateEnds: Long, val payedBy : MembershipPaymentMethod) : Visible()
        data class Price(val price: String, val period: TierPeriod) : Visible()
        data class PriceBilling(val price: BillingPriceInfo) : Visible()
        data class Free(val period: TierPeriod) : Visible()
        data class Error(val message: String) : Visible()
    }
}

sealed class TierPeriod {
    data object Unknown : TierPeriod()
    data object Unlimited : TierPeriod()
    data class Year(val count : Int) : TierPeriod()
    data class Month(val count : Int) : TierPeriod()
    data class Week(val count : Int) : TierPeriod()
    data class Day(val count : Int) : TierPeriod()
}

sealed class TierButton {
    data object Hidden : TierButton()
    sealed class Submit : TierButton() {
        data object Enabled : Submit()
        data object Disabled : Submit()
    }

    data object ChangeEmail : TierButton()

    sealed class Info : TierButton() {
        data class Enabled(val url: String) : Info()
        data object Disabled : Info()
    }

    sealed class Pay : TierButton() {
        data object Enabled : Submit()
        data object Disabled : Submit()
    }

    sealed class Manage : TierButton() {
        sealed class Android : Manage() {
            data class Enabled(val productId: String?) : Android()
            data object Disabled : Android()
        }
        sealed class External : Manage() {
            data class Enabled(val manageUrl: String?) : External()
            data object Disabled : External()
        }
    }
}

sealed class TierAnyName {
    data object Hidden : TierAnyName()
    sealed class Visible : TierAnyName() {
        data object Disabled : Visible()
        data object Enter : Visible()
        data object Validating : Visible()
        data class Validated(val validatedName: String) : Visible()
        data class Error(val membershipErrors: MembershipErrors) : Visible()
        data class ErrorOther(val message: String?) : Visible()
    }
}

sealed class TierEmail {
    data object Hidden : TierEmail()
    sealed class Visible : TierEmail() {
        data object Enter : Visible()
        data object Validating : Visible()
        data object Validated : Visible()
        data class Error(val membershipErrors: MembershipErrors) : Visible()
        data class ErrorOther(val message: String?) : Visible()
    }
}

data class BillingPriceInfo(val formattedPrice: String, val period: PeriodDescription)
data class PeriodDescription(val amount: Int, val unit: PeriodUnit)
enum class PeriodUnit { YEARS, MONTHS, WEEKS, DAYS }