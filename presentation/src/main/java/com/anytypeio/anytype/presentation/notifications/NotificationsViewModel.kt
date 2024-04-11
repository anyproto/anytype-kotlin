package com.anytypeio.anytype.presentation.notifications

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ImportErrorCode
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.GetSpaceMemberByIdentity
import com.anytypeio.anytype.domain.notifications.ReplyNotifications
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationsViewModel(
    private val analytics: Analytics,
    private val notificationsProvider: NotificationsProvider,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val getSpaceMemberByIdentity: GetSpaceMemberByIdentity,
    private val replyNotifications: ReplyNotifications
) : BaseViewModel() {

    val state = MutableStateFlow<NotificationsScreenState>(NotificationsScreenState.Hidden)
    val command = MutableStateFlow<Command?>(null)

    init {
        viewModelScope.launch {
            val notification = notificationsProvider.events.value
            Timber.d("Received notifications in NotificationsViewModel: $notification")
            if (notification.isNotEmpty()) {
                handleNotification(notification.first())
            }
        }
    }

    private fun handleNotification(event: Notification.Event) {
        val notification = event.notification ?: return
        when (val payload = notification.payload) {
            is NotificationPayload.GalleryImport -> {
                if (payload.errorCode != ImportErrorCode.NULL) {
                    state.value = NotificationsScreenState.GalleryInstalledError(
                        errorCode = payload.errorCode
                    )
                } else {
                    state.value = NotificationsScreenState.GalleryInstalled(
                        spaceId = payload.spaceId,
                        galleryName = payload.name
                    )
                }
            }
            is NotificationPayload.RequestToJoin -> {
                state.value = NotificationsScreenState.Multiplayer.RequestToJoin(
                    notification = notification.id,
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    identity = payload.identity,
                    identityName = payload.identityName
                )
            }
            is NotificationPayload.RequestToLeave -> {
                state.value = NotificationsScreenState.Multiplayer.RequestToLeave(
                    notification = notification.id,
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    identity = payload.identity,
                    name = payload.identityName
                )
            }
            is NotificationPayload.ParticipantRequestApproved -> {
                state.value = NotificationsScreenState.Multiplayer.MemberRequestApproved(
                    notification = notification.id,
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    isReadOnly = !payload.permissions.isOwnerOrEditor()
                )
            }
            is NotificationPayload.ParticipantRemove -> {
                state.value = NotificationsScreenState.Multiplayer.MemberSpaceRemove(
                    notification = notification.id,
                    spaceName = payload.spaceName,
                    identityName = payload.identityName
                )
            }
            is NotificationPayload.ParticipantPermissionsChange -> {
                state.value = NotificationsScreenState.Multiplayer.MemberPermissionChanged(
                    notification = notification.id,
                    spaceId = payload.spaceId,
                    spaceName = payload.spaceName,
                    permissions = payload.permissions
                )
            }
            is NotificationPayload.ParticipantRequestDecline -> {
                state.value = NotificationsScreenState.Multiplayer.MemberRequestDeclined(
                    notification = notification.id,
                    spaceId = payload.spaceId,
                    spaceName = payload.spaceName
                )
            }
            else -> {
                Timber.w("Ignored notification: $payload")
            }
        }
    }

    fun onErrorButtonClick() {
        command.value = Command.Dismiss
    }

    fun onNavigateToSpaceClicked(spaceId: SpaceId) {
        viewModelScope.launch {
            Timber.d("Setting space: $spaceId")
            analytics.sendEvent(eventName = EventsDictionary.switchSpace)
            spaceManager.set(spaceId.id).fold(
                onSuccess = {
                    saveCurrentSpace.async(SaveCurrentSpace.Params(spaceId)).fold(
                        onFailure = {
                            Timber.e(it, "Error while saving current space in user settings")
                            command.value = Command.Dismiss
                        },
                        onSuccess = {
                            command.value = Command.Dismiss
                        }
                    )
                },
                onFailure = {
                    Timber.e(it, "Could not select space")
                }
            )
        }
    }

    fun onNotificationAction(action: NotificationAction) {
        when(action) {
            is NotificationAction.Multiplayer.ViewSpaceJoinRequest -> {
                proceedWithSpaceJoinRequest(action)
            }
            is NotificationAction.Multiplayer.ViewSpaceLeaveRequest -> {
                proceedWithSpaceLeaveRequest(action)
            }
        }
    }

    private fun proceedWithSpaceLeaveRequest(action: NotificationAction.Multiplayer.ViewSpaceLeaveRequest) {
        viewModelScope.launch {
            replyNotifications.async(
                params = listOf(action.notification)
            ).fold(
                onSuccess = {
                    Timber.d("Replied notification: ${action.notification}")
                },
                onFailure = {
                    Timber.e(it, "Error while replying notification")
                }
            )
            command.emit(Command.Dismiss)
            command.emit(
                Command.ViewSpaceLeaveRequest(
                    space = action.space,
                )
            )
        }
    }

    private fun proceedWithSpaceJoinRequest(action: NotificationAction.Multiplayer.ViewSpaceJoinRequest) {
        viewModelScope.launch {
            getSpaceMemberByIdentity.async(
                GetSpaceMemberByIdentity.Params(
                    space = action.space,
                    identity = action.identity
                )
            ).fold(
                onSuccess = { member ->
                    if (member != null && member.spaceId == action.space.id) {
                        replyNotifications.async(
                            params = listOf(action.notification)
                        ).fold(
                            onSuccess = {
                                Timber.d("Replied notification: ${action.notification}")
                            },
                            onFailure = {
                                Timber.e(it, "Error while replying notification")
                            }
                        )
                        command.emit(Command.Dismiss)
                        command.emit(
                            Command.ViewSpaceJoinRequest(
                                space = action.space,
                                member = member.id
                            )
                        )
                    } else {
                        Timber.w("Space member not found")
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while searching space member by identity")
                }
            )
        }
    }

    sealed class Command {
        data object Dismiss : Command()
        data class NavigateToSpace(val spaceId: SpaceId) : Command()
        data class ViewSpaceJoinRequest(val space: SpaceId, val member: Id) : Command()
        data class ViewSpaceLeaveRequest(val space: SpaceId) : Command()
    }
}

sealed class NotificationsScreenState {
    data object Hidden : NotificationsScreenState()
    data class GalleryInstalled(
        val spaceId: SpaceId,
        val galleryName: String
    ) : NotificationsScreenState()
    data class GalleryInstalledError(
        val errorCode: ImportErrorCode
    ) : NotificationsScreenState()
    sealed class Multiplayer : NotificationsScreenState() {

        /**
         * Notification id
         */
        abstract val notification: Id

        // Owner
        data class RequestToJoin(
            override val notification: Id,
            val space: SpaceId,
            val spaceName: String,
            val identity: Id,
            val identityName: String
        ) : Multiplayer()
        // Owner
        data class RequestToLeave(
            override val notification: Id,
            val space: SpaceId,
            val spaceName: String,
            val identity: Id,
            val name: String
        ) : Multiplayer()
        // Member
        data class MemberRequestApproved(
            override val notification: Id,
            val space: SpaceId,
            val spaceName: String,
            val isReadOnly: Boolean
        ) : Multiplayer()
        // Member
        data class MemberSpaceRemove(
            override val notification: Id,
            val spaceName: String,
            val identityName: String
        ) : Multiplayer()
        // Member
        data class MemberPermissionChanged(
            override val notification: Id,
            val spaceId: SpaceId,
            val spaceName: String,
            val permissions: SpaceMemberPermissions
        ) : Multiplayer()
        // Member
        data class MemberRequestDeclined(
            override val notification: Id,
            val spaceId: SpaceId,
            val spaceName: String
        ) : Multiplayer()
    }
}

sealed class NotificationAction {
    sealed class Multiplayer : NotificationAction() {
        data class ViewSpaceJoinRequest(
            val notification: Id,
            val space: SpaceId,
            val identity: Id
        ) : Multiplayer()
        data class ViewSpaceLeaveRequest(
            val notification: Id,
            val space: SpaceId
        ) : Multiplayer()
    }
}