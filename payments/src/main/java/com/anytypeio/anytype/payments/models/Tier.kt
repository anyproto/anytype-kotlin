package com.anytypeio.anytype.payments.models

import com.anytypeio.anytype.payments.viewmodel.TierId

sealed class Tier {
    abstract val id: TierId
    abstract val isCurrent: Boolean
    abstract val validUntil: String
    abstract val prettyName: String

    data class Explorer(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String,
        override val prettyName: String = "Explorer",
        val price: String = "",
        val email: String = "",
        val isChecked: Boolean = true
    ) : Tier()

    data class Builder(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String,
        override val prettyName: String = "Builder",
        val price: String = "",
        val interval: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false
    ) : Tier()

    data class CoCreator(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String,
        override val prettyName: String = "Co-Creator",
        val price: String = "",
        val interval: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false
    ) : Tier()

    data class Custom(
        override val id: TierId,
        override val isCurrent: Boolean,
        override val validUntil: String,
        override val prettyName: String = "Custom",
        val price: String = ""
    ) : Tier()
}