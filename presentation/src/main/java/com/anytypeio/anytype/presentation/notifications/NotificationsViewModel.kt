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
        if (payload is NotificationPayload.GalleryImport) {
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
        } else if (payload is NotificationPayload.RequestToJoin) {
            state.value = NotificationsScreenState.Multiplayer.RequestToJoin(
                space = payload.spaceId,
                identity = payload.identity,
                name = payload.identityName,
                icon = payload.identityIcon
            )
        } else if (payload is NotificationPayload.ParticipantRequestApproved) {
            state.value = NotificationsScreenState.Multiplayer.MemberRequestApproved(
                spaceName = payload.spaceId.id,
                isReadOnly = !payload.permissions.isOwnerOrEditor()
            )
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
        data class RequestToJoin(
            val space: SpaceId,
            val identity: Id,
            val name: String,
            val icon: Id
        ) : Multiplayer()
        data class MemberRequestApproved(
            val spaceName: String,
            val isReadOnly: Boolean
        ) : Multiplayer()
    }
}