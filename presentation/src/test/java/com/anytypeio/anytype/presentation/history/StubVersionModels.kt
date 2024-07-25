package com.anytypeio.anytype.presentation.history

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.history.DiffVersionResponse
import com.anytypeio.anytype.core_models.history.ShowVersionResponse
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import kotlin.random.Random
import net.bytebuddy.utility.RandomString

fun StubVersion(
    id: Id = "versionId - ${RandomString.make()}",
    previousIds: List<Id> = emptyList(),
    authorId: Id = "authorId - ${RandomString.make()}",
    authorName: String = "",
    timestamp: TimeInSeconds,
    groupId: Long = Random(100).nextLong()
): Version {
    return Version(
        id = id,
        previousIds = previousIds,
        spaceMember = authorId,
        spaceMemberName = authorName,
        timestamp = timestamp,
        groupId = groupId
    )
}

fun StubShowVersionResponse(
    objectView: ObjectView?,
    version: Version?,
    traceId: Id
): ShowVersionResponse {
    return ShowVersionResponse(
        objectView = objectView,
        version = version,
        traceId = traceId
    )
}

fun StubDiffVersionResponse(
    historyEvents: List<Event.Command>,
    objectView: ObjectView?
): DiffVersionResponse {
    return DiffVersionResponse(
        historyEvents = historyEvents,
        objectView = objectView
    )
}