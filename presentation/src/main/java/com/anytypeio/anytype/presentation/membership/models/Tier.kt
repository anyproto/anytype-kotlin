package com.anytypeio.anytype.presentation.membership.models

import com.anytypeio.anytype.core_models.membership.Membership


sealed class Tier {
    abstract val id: TierId
    abstract val isCurrent: Boolean
    abstract val validUntil: String
    abstract val prettyName: String
    abstract val color: String
    abstract val features: List<String>
    abstract val status: Membership.Status
    abstract val androidTierId: String?

    data class Explorer(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String = "",
        override val prettyName: String = "Explorer",
        override val features: List<String>,
        override val status: Membership.Status,
        override val androidTierId: String?,
        val price: String = "",
        val email: String = "",
        val isChecked: Boolean = true,
        override val color: String
    ) : Tier()

    data class Builder(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String = "",
        override val prettyName: String = "Builder",
        override val features: List<String>,
        override val status: Membership.Status,
        override val androidTierId: String?,
        val price: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false,
        override val color: String
    ) : Tier()

    data class CoCreator(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String = "",
        override val prettyName: String = "Co-Creator",
        override val features: List<String>,
        override val status: Membership.Status,
        override val androidTierId: String?,
        val price: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false,
        override val color: String
    ) : Tier()

    data class Custom(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String = "",
        override val prettyName: String = "Custom",
        override val features: List<String>,
        override val status: Membership.Status,
        override val androidTierId: String?,
        val price: String = "",
        override val color: String
    ) : Tier()
}

sealed class MembershipAnyName {
    data object None : MembershipAnyName()
    data class Some(val minLength: Int) : MembershipAnyName()
}