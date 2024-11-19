package com.anytypeio.anytype.core_models

sealed class RelativeDate {
    data class Today(val timestamp: Long) : RelativeDate()
    data class Tomorrow(val timestamp: Long) : RelativeDate()
    data class Yesterday(val timestamp: Long) : RelativeDate()
    data class Other(val date: String, val time: String, val timestamp: Long) : RelativeDate()
}