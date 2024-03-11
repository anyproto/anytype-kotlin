package com.anytypeio.anytype.models

sealed class Tier {
    abstract val id: String
    abstract val isCurrent: Boolean

    data class Explorer(
        override val id: String,
        override val isCurrent: Boolean,
        val price: String = "",
        val email: String = "",
        val isChecked: Boolean = true
    ) : Tier()

    data class Builder(
        override val id: String,
        override val isCurrent: Boolean,
        val price: String = ""
    ) : Tier()

    data class CoCreator(
        override val id: String,
        override val isCurrent: Boolean,
        val price: String = ""
    ) : Tier()

    data class Custom(
        override val id: String,
        override val isCurrent: Boolean,
        val price: String = ""
    ) : Tier()
}