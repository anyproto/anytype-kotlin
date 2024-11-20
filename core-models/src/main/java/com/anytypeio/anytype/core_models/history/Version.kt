package com.anytypeio.anytype.core_models.history

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds

data class Version(
    val id: Id,
    val previousIds: List<Id>,
    val spaceMember: Id,
    val spaceMemberName: String,
    val timestamp: TimestampInSeconds,
    val groupId: Long
)

data class ShowVersionResponse(
    val payload: Payload?,
    val version: Version?,
    val traceId: Id
)

data class DiffVersionResponse(
    val historyEvents: List<Event.Command>,
    val objectView: ObjectView?
)
