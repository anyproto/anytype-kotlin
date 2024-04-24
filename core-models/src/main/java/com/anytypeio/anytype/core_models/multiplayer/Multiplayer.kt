package com.anytypeio.anytype.core_models.multiplayer

import com.anytypeio.anytype.core_models.primitives.SpaceId

data class SpaceInviteLink(
    val fileKey: String,
    val contentId: String
) {
    val scheme = "https://invite.any.coop/$contentId#$fileKey"
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
    val code: Int,
    val prettyName: String
) {
    READER(0, "Reader"),
    WRITER(1, "Writer"),
    OWNER(2, "Owner"),
    NO_PERMISSIONS(3, "NoPermissions");

    fun isOwnerOrEditor() : Boolean {
        return this == OWNER || this == WRITER
    }

    fun isAtLeastReader() : Boolean {
        return this == OWNER || this == WRITER || this == READER
    }
}

enum class SpaceAccessType(val code: Int, val prettyName: String) {
    PRIVATE(0, "Private"),
    DEFAULT(1, "Personal"),
    SHARED(2, "Shared")
}

sealed class SpaceInviteError : Exception() {
    class SpaceNotFound : SpaceInviteError()
    class SpaceDeleted: SpaceInviteError()
    class InvalidInvite: SpaceInviteError()
}