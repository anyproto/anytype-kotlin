package com.anytypeio.anytype.core_models.multiplayer

data class SpaceInviteLink(
    val fileKey: String,
    val contentId: String
) {
    val scheme = "anytype://invite/?cid=$contentId&key=$fileKey"
}

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

enum class ParticipantPermissions(
    val code: Int
) {
    READER(0),
    WRITER(1),
    OWNER(2),
    NO_PERMISSIONS(3),
}