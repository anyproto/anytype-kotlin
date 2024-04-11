package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView
import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceMember
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

fun SpaceView.canAddWriters(participants: List<SpaceMember>): Boolean {
    if (!canAddReaders(participants)) return false
    this.writersLimit?.let {
        return it > activeWriters(participants)
    } ?: return true
}

fun SpaceView.canAddReaders(participants: List<SpaceMember>): Boolean {
    this.readersLimit?.let {
        return it > activeReaders(participants)
    } ?: return true
}

fun SpaceView.canChangeWriterToReader(participants: List<SpaceMember>): Boolean {
    return true
}

fun SpaceView.canChangeReaderToWriter(participants: List<SpaceMember>): Boolean {
    writersLimit?.let {
        return it.toInt() > activeWriters(participants)
    } ?: return true
}

private fun activeReaders(participants: List<SpaceMember>): Int =
    participants.count {
        it.permissions in listOf(
            SpaceMemberPermissions.READER,
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.OWNER
        )
    }

private fun activeWriters(participants: List<SpaceMember>): Int =
    participants.count {
        it.permissions in listOf(
            SpaceMemberPermissions.WRITER,
            SpaceMemberPermissions.OWNER
        )
    }
