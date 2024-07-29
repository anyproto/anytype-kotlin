package com.anytypeio.anytype.core_models.history

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds

data class Version(
    val id: Id,
    val previousIds: List<Id>,
    val spaceMember: Id,
    val spaceMemberName: String,
    val timestamp: TimeInSeconds,
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
