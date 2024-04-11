package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView
import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceMember
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus

fun SpaceView.getTitle(): String = name.orEmpty()

fun SpaceView.canBeShared(isOwner: Boolean): Boolean =
    isOwner && (spaceAccessType == SpaceAccessType.SHARED || spaceAccessType == SpaceAccessType.PRIVATE)

fun SpaceView.canStopSharing(isOwner: Boolean): Boolean =
    isOwner && (spaceAccessType == SpaceAccessType.SHARED)

fun SpaceView.isShared(): Boolean = spaceAccessType == SpaceAccessType.SHARED

fun SpaceView.canBeDelete(): Boolean = spaceAccessType == SpaceAccessType.PRIVATE

fun SpaceView.canBeArchive(): Boolean = spaceAccountStatus == SpaceStatus.SPACE_REMOVING

fun SpaceView.canCancelJoinRequest(): Boolean = spaceAccountStatus == SpaceStatus.SPACE_JOINING

fun SpaceView.canStopShare(): Boolean = this.isShared()

//val SpaceView.isActive: Boolean
//    get() = this.localStatus == LocalStatus.OK && this.accountStatus != AccountStatus.SPACE_REMOVING && this.accountStatus != AccountStatus.SPACE_DELETED

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
    readersLimit?.let {
        return it.toInt() >= activeReaders(participants)
    } ?: return true
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
