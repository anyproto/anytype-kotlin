package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Id

data class TextBlockTarget(
    val context: Id,
    val blockId: Id,
)