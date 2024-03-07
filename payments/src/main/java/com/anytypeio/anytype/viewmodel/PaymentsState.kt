package com.anytypeio.anytype.viewmodel

sealed class PaymentsState {
    object Loading : PaymentsState()
    data class Success(val tiers: List<TierState>) : PaymentsState()
}

sealed class TierState {
    abstract val isCurrent: Boolean

    data class Explorer(
        val price: String,
        override val isCurrent: Boolean
    ) : TierState()

    data class Builder(
        val price: String,
        override val isCurrent: Boolean
    ) : TierState()

    data class CoCreator(
        val price: String,
        override val isCurrent: Boolean
    ) : TierState()

    data class Custom(
        val price: String,
        override val isCurrent: Boolean
    ) : TierState()
}