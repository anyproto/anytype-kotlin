package com.anytypeio.anytype.payments.models

import androidx.annotation.StringRes
import com.anytypeio.anytype.core_models.membership.MembershipPaymentMethod
import com.anytypeio.anytype.presentation.membership.models.TierId

//This is a data class that represents a tier preview view in the main Membership screen
data class TierPreviewView(
    val id: TierId,
    val isActive: Boolean,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val conditionInfo: TierConditionInfo,
)

//This is a data class that represents a tier view when tier is opened
data class TierView(
    val id: TierId,
    val isActive: Boolean,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val conditionInfo: TierConditionInfo,
    val features: List<String>,
    val membershipAnyName: TierAnyName,
    val buttonState: TierButton,
    val email: TierEmail
)

sealed class TierConditionInfo {
    data object Hidden : TierConditionInfo()
    sealed class Visible : TierConditionInfo() {
        data object LoadingBillingClient : Visible()
        data class Valid(val period: TierPeriod, val payedBy : MembershipPaymentMethod) : Visible()
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

    sealed class Change : TierButton() {
        data object Enabled : Submit()
        data object Disabled : Submit()
    }

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
            data class Enabled(val productId: String) : Android()
            data object Disabled : Android()
        }
        sealed class External : Manage() {
            data object Enabled : External()
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
        data object Validated : Visible()
        data class Error(val message: String) : Visible()
    }
}

sealed class TierEmail {
    data object Hidden : TierEmail()
    sealed class Visible : TierEmail() {
        data object Enter : Visible()
        data object Validating : Visible()
        data object Validated : Visible()
        data class Error(val message: String) : Visible()
    }
}

data class BillingPriceInfo(val formattedPrice: String, val period: PeriodDescription)
data class PeriodDescription(val amount: Int, val unit: PeriodUnit)
enum class PeriodUnit { YEARS, MONTHS, DAYS }