package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView
import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceMember
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

fun SpaceView.canAddWriters(
    isCurrentUserOwner: Boolean,
    participants: List<SpaceMember>
): Boolean {
    if (!canAddReaders(isCurrentUserOwner, participants)) return false
    if (!isCurrentUserOwner) return false
    if (spaceAccessType != SpaceAccessType.SHARED) return false
    if (participants.none { it.status == ParticipantStatus.JOINING }) return false
    val isWritersLimitReached =
        isSubscriberLimitReached(activeWriters(participants), writersLimit?.toInt())
    return !isWritersLimitReached
}

fun SpaceView.canAddReaders(
    isCurrentUserOwner: Boolean,
    participants: List<SpaceMember>
): Boolean {
    if (!isCurrentUserOwner) return false
    if (spaceAccessType != SpaceAccessType.SHARED) return false
    if (participants.none { it.status == ParticipantStatus.JOINING }) return false
    val isReadersLimitReached =
        isSubscriberLimitReached(activeReaders(participants), readersLimit?.toInt())
    return !isReadersLimitReached
}

fun SpaceView.canChangeWriterToReader(participants: List<SpaceMember>): Boolean {
    return true
}

fun SpaceView.canChangeReaderToWriter(participants: List<SpaceMember>): Boolean {
    return !isSubscriberLimitReached(activeWriters(participants), writersLimit?.toInt())
}

fun activeReaders(participants: List<SpaceMember>): Int =
    participants.count {
        it.permissions in listOf(
            SpaceMemberPermissions.READER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.OWNER
        ) && it.status == ParticipantStatus.ACTIVE
    }

fun activeWriters(participants: List<SpaceMember>): Int =
    participants.count {
        it.permissions in listOf(
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.OWNER
        ) && it.status == ParticipantStatus.ACTIVE
    }

fun isSubscriberLimitReached(currentSubscribers: Int, subscriberLimit: Int?): Boolean {
    return subscriberLimit?.let { currentSubscribers >= it } ?: false
}
