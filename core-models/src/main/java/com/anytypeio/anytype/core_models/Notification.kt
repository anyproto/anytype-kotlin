package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId

data class Notification(
    val id: Id,
    val createTime: Long,
    val status: Status,
    val isLocal: Boolean,
    val payload: NotificationPayload,
    val space: SpaceId,
    val aclHeadId: String
)

sealed class NotificationPayload {
    data class Import(
        val processId: String,
        val errorCode: ImportErrorCode,
        val importType: ImportType,
        val spaceId: SpaceId,
        val name: String
    ) : NotificationPayload()

    data class Export(
        val errorCode: ImportErrorCode,
        val exportType: ExportFormat
    ) : NotificationPayload()

    data class GalleryImport(
        val processId: String,
        val errorCode: ImportErrorCode,
        val spaceId: SpaceId,
        val name: String
    ) : NotificationPayload()

    data class RequestToJoin(
        val spaceId: SpaceId,
        val identity: String,
        val identityName: String,
        val identityIcon: String
    ) : NotificationPayload()

    object Test : NotificationPayload()

    data class ParticipantRequestApproved(
        val spaceId: SpaceId,
        val permissions: SpaceMemberPermissions
    ) : NotificationPayload()

    data class RequestToLeave(
        val spaceId: SpaceId,
        val identity: String,
        val identityName: String,
        val identityIcon: String
    ) : NotificationPayload()

    data class ParticipantRemove(
        val identity: String,
        val identityName: String,
        val identityIcon: String,
        val spaceId: SpaceId
    ) : NotificationPayload()

    data class ParticipantRequestDecline(
        val spaceId: SpaceId
    ) : NotificationPayload()

    data class ParticipantPermissionsChange(
        val spaceId: SpaceId,
        val permissions: SpaceMemberPermissions
    ) : NotificationPayload()
}

enum class NotificationStatus {
    CREATED, SHOWN, READ, REPLIED
}

enum class NotificationActionType {
    CLOSE
}