package com.anytypeio.anytype.core_models

sealed class RelativeDate {
    data object Today : RelativeDate()
    data object Tomorrow : RelativeDate()
    data object Yesterday : RelativeDate()
    data class Other(val formattedDate: String, val formattedTime: String) : RelativeDate()
}