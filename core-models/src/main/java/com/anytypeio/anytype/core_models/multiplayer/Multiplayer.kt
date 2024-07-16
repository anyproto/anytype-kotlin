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
    val code: Int
) {
    READER(0),
    WRITER(1),
    OWNER(2),
    NO_PERMISSIONS(3);

    fun isOwner(): Boolean {
        return this == OWNER
    }

    fun isOwnerOrEditor(): Boolean {
        return this == OWNER || this == WRITER
    }

    fun isAtLeastReader(): Boolean {
        return this == OWNER || this == WRITER || this == READER
    }
}

enum class SpaceAccessType(val code: Int) {
    PRIVATE(0),
    DEFAULT(1),
    SHARED(2)
}

sealed class SpaceInviteError : Exception() {
    class SpaceNotFound : SpaceInviteError()
    class SpaceDeleted : SpaceInviteError()
    class InvalidInvite : SpaceInviteError()
}

data class SpaceSyncUpdate(
    val id: String,
    val status: SpaceSyncStatus,
    val network: SpaceSyncNetwork,
    val error: SpaceSyncError?,
    val syncingObjectsCounter: Long
)

enum class SpaceSyncStatus {
    SYNCED,
    SYNCING,
    ERROR,
    OFFLINE
}

enum class SpaceSyncNetwork {
    ANYTYPE,
    SELF_HOST,
    LOCAL_ONLY
}

enum class SpaceSyncError {
    NULL,
    STORAGE_LIMIT_EXCEED,
    INCOMPATIBLE_VERSION,
    NETWORK_ERROR
}

data class P2PStatusUpdate(
    val spaceId: String,
    val status: P2PStatus,
    val devicesCounter: Long
)

enum class P2PStatus {
    NOT_CONNECTED,
    NOT_POSSIBLE,
    CONNECTED
}