package com.anytypeio.anytype.core_models.history

import com.anytypeio.anytype.core_models.Id

data class Version(
    val id: Id,
    val previousIds: List<Id>,
    val authorId: Id,
    val authorName: String,
    val time: Long,
    val groupId: Long
)
