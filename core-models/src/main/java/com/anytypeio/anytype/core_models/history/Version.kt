package com.anytypeio.anytype.core_models.history

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView

data class Version(
    val id: Id,
    val previousIds: List<Id>,
    val authorId: Id,
    val authorName: String,
    val timestamp: Long,
    val groupId: Long
)

data class ShowVersionResponse(
    val objectView: ObjectView?,
    val version: Version?,
    val traceId: Id
)

data class DiffVersionResponse(
    val historyEvents: List<Event.Command>,
    val objectView: ObjectView?
)
