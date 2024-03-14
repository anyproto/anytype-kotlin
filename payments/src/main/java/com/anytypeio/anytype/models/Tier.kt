package com.anytypeio.anytype.models

import com.anytypeio.anytype.viewmodel.TierId

sealed class Tier {
    abstract val id: TierId
    abstract val isCurrent: Boolean

    data class Explorer(
        override val id: TierId,
        override val isCurrent: Boolean,
        val price: String = "",
        val email: String = "",
        val isChecked: Boolean = true
    ) : Tier()

    data class Builder(
        override val id: TierId,
        override val isCurrent: Boolean,
        val price: String = "",
        val interval: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false

    ) : Tier()

    data class CoCreator(
        override val id: TierId,
        override val isCurrent: Boolean,
        val price: String = "",
        val interval: String = "",
        val name: String = "",
        val nameIsTaken: Boolean = false,
        val nameIsFree: Boolean = false
    ) : Tier()

    data class Custom(
        override val id: TierId,
        override val isCurrent: Boolean,
        val price: String = ""
    ) : Tier()
}