package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

data class Notification(
    val id: String,
    val createTime: Long,
    val status: Status,
    val isLocal: Boolean,
    val payload: Payload,
    val space: String,
    val aclHeadId: String
)

sealed class NotificationPayload {
    data class Import(
        val processId: String,
        val errorCode: ImportErrorCode,
        val importType: ImportType,
        val spaceId: String,
        val name: String
    ) : NotificationPayload()

    data class Export(
        val errorCode: ImportErrorCode,
        val exportType: ExportFormat
    ) : NotificationPayload()

    data class GalleryImport(
        val processId: String,
        val errorCode: ImportErrorCode,
        val spaceId: String,
        val name: String
    ) : NotificationPayload()

    data class RequestToJoin(
        val spaceId: String,
        val identity: String,
        val identityName: String,
        val identityIcon: String
    ) : NotificationPayload()

    object Test : NotificationPayload()

    data class ParticipantRequestApproved(
        val spaceId: String,
        val permissions: SpaceMemberPermissions
    ) : NotificationPayload()

    data class RequestToLeave(
        val spaceId: String,
        val identity: String,
        val identityName: String,
        val identityIcon: String
    ) : NotificationPayload()

    data class ParticipantRemove(
        val identity: String,
        val identityName: String,
        val identityIcon: String,
        val spaceId: String
    ) : NotificationPayload()

    data class ParticipantRequestDecline(
        val spaceId: String
    ) : NotificationPayload()

    data class ParticipantPermissionsChange(
        val spaceId: String,
        val permissions: SpaceMemberPermissions
    ) : NotificationPayload()
}

enum class Status {
    CREATED, SHOWN, READ, REPLIED
}

enum class ActionType {
    CLOSE
}