package com.anytypeio.anytype.core_models.settings

import com.anytypeio.anytype.core_models.Id

data class VaultSettings(
    val orderOfSpaces: List<Id> = emptyList()
)