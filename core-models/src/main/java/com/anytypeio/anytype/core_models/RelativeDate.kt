package com.anytypeio.anytype.core_models

sealed class RelativeDate {
    abstract val initialTimeInMillis: TimeInMillis

    data class Today(override val initialTimeInMillis: TimeInMillis) : RelativeDate()
    data class Tomorrow(override val initialTimeInMillis: TimeInMillis) : RelativeDate()
    data class Yesterday(override val initialTimeInMillis: TimeInMillis) : RelativeDate()
    data class Other(
        override val initialTimeInMillis: TimeInMillis,
        val formattedDate: String,
        val formattedTime: String
    ) : RelativeDate()
}