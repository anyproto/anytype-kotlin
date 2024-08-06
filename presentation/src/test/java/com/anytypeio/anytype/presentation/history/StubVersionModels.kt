package com.anytypeio.anytype.presentation.history

import com.anytypeio.anytype.core_models.Id
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