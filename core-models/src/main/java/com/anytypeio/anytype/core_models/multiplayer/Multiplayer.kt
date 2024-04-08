package com.anytypeio.anytype.core_models.multiplayer

import com.anytypeio.anytype.core_models.primitives.SpaceId

data class SpaceInviteLink(
    val fileKey: String,
    val contentId: String
) {
    val scheme = "anytype://invite/?cid=$contentId&key=$fileKey"
}

data class SpaceInviteView(
    val space: SpaceId,
    val spaceName: String,
    val creatorName: String,
    val spaceIconContentId: String
)

enum class ParticipantStatus(
    val code: Int
) {
    JOINING(0),
    ACTIVE(1),
    REMOVED(2),
    DECLINED(3),
    REMOVING(4),
    CANCELLED(5),
}

enum class SpaceMemberPermissions(
    val code: Int
) {
    READER(0),
    WRITER(1),
    OWNER(2),
    NO_PERMISSIONS(3);

    fun isOwnerOrEditor() : Boolean {
        return this == OWNER || this == WRITER
    }
}

enum class SpaceAccessType(val code: Int) {
    PRIVATE(0),
    DEFAULT(1),
    SHARED(2)
}

sealed class SpaceInviteError : Exception() {
    class SpaceNotFound : SpaceInviteError()
    class SpaceDeleted: SpaceInviteError()
    class InvalidInvite: SpaceInviteError()
}