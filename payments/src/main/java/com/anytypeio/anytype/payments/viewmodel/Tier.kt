package com.anytypeio.anytype.payments.viewmodel

import androidx.annotation.StringRes
import com.anytypeio.anytype.presentation.membership.models.TierId

sealed class Tier {
    abstract val id: TierId
    abstract val isCurrent: Boolean
    abstract val prettyName: String
    abstract val color: String
    abstract val features: List<String>

    data class Explorer(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val prettyName: String = "Explorer",
        override val features: List<String>,
        val price: String = "",
        val email: String = "",
        val isChecked: Boolean = true,
        override val color: String
    ) : Tier()

    data class Builder(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val prettyName: String = "Builder",
        override val features: List<String>,
        val price: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false,
        override val color: String
    ) : Tier()

    data class CoCreator(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val prettyName: String = "Co-Creator",
        override val features: List<String>,
        val price: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false,
        override val color: String
    ) : Tier()

    data class Custom(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val prettyName: String = "Custom",
        override val features: List<String>,
        val price: String = "",
        override val color: String
    ) : Tier()
}


data class MembershipTierPreviewView(
    val id: TierId,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val conditionInfo: MembershipTierConditionInfo,
    val isCurrent: Boolean,
)

data class MembershipTierView(
    val id: TierId,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val features: List<String>,
    val membershipAnyName: MembershipTierAnyName,
    val conditionInfo: MembershipTierConditionInfo,
    val buttonState: MembershipTierButton,
)

sealed class MembershipTierConditionInfo {
    data object Hidden : MembershipTierConditionInfo()
    sealed class Visible : MembershipTierConditionInfo() {
        data class Valid(val interval: String) : Visible()
        data class Price(val price: String, val interval: String) : Visible()
        data class Free(val email: String) : Visible()
        data class Email(val email: String) : Visible()
    }
}

sealed class MembershipTierButton {
    data object Hidden : MembershipTierButton()
    sealed class Submit : MembershipTierButton() {
        data object Enabled : Submit()
        data object Disabled : Submit()
    }

    sealed class Pay : MembershipTierButton() {
        data object Enabled : Submit()
        data object Disabled : Submit()
    }
}

sealed class MembershipTierAnyName {
    data object Hidden : MembershipTierAnyName()
    sealed class Editable : MembershipTierAnyName() {
        data object Enter : Editable()
        data object Validating : Editable()
        data object Validated : Editable()
        data class Error(val message: String) : Editable()
    }
}