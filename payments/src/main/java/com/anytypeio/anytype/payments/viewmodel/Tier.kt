package com.anytypeio.anytype.payments.viewmodel

import com.anytypeio.anytype.presentation.membership.models.TierId

@Deprecated("Use TierView instead")
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

