package com.anytypeio.anytype.core_models.settings

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.AppDateFormat

data class VaultSettings(
    val showIntroduceVault: Boolean,
    val orderOfSpaces: List<Id> = emptyList(),
    val isRelativeDates: Boolean,
    val dateFormat: AppDateFormat?
)