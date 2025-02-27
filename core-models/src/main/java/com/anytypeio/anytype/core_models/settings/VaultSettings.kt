package com.anytypeio.anytype.core_models.settings

import com.anytypeio.anytype.core_models.DEFAULT_RELATIVE_DATES
import com.anytypeio.anytype.core_models.FALLBACK_DATE_PATTERN
import com.anytypeio.anytype.core_models.Id

data class VaultSettings(
    val orderOfSpaces: List<Id> = emptyList(),
    val isRelativeDates: Boolean,
    val dateFormat: String
) {
    companion object {
        fun default() : VaultSettings = VaultSettings(
            orderOfSpaces = emptyList(),
            isRelativeDates = DEFAULT_RELATIVE_DATES,
            dateFormat = FALLBACK_DATE_PATTERN
        )
    }
}