package com.anytypeio.anytype.presentation.notifications

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ImportErrorCode
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
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
    private val saveCurrentSpace: SaveCurrentSpace
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
        val payload = event.notification?.payload
        when (payload) {
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
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    identity = payload.identity,
                    identityName = payload.identityName
                )
            }
            is NotificationPayload.RequestToLeave -> {
                state.value = NotificationsScreenState.Multiplayer.RequestToLeave(
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    identity = payload.identity,
                    name = payload.identityName
                )
            }
            is NotificationPayload.ParticipantRequestApproved -> {
                state.value = NotificationsScreenState.Multiplayer.MemberRequestApproved(
                    space = payload.spaceId,
                    spaceName = payload.spaceName,
                    isReadOnly = !payload.permissions.isOwnerOrEditor()
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

    sealed class Command {
        data object Dismiss : Command()
        data class NavigateToSpace(val spaceId: SpaceId) : Command()
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
        // Owner
        data class RequestToJoin(
            val space: SpaceId,
            val spaceName: String,
            val identity: Id,
            val identityName: String
        ) : Multiplayer()
        // Owner
        data class RequestToLeave(
            val space: SpaceId,
            val spaceName: String,
            val identity: Id,
            val name: String
        ) : Multiplayer()
        data class MemberRequestApproved(
            val space: SpaceId,
            val spaceName: String,
            val isReadOnly: Boolean
        ) : Multiplayer()
    }
}