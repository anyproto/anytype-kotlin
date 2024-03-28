package com.anytypeio.anytype.presentation.notifications

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.ImportErrorCode
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
            notificationsProvider.observe().collect { notification ->
                when (notification?.payload) {
                    is NotificationPayload.GalleryImport -> {
                        val payload = notification.payload as NotificationPayload.GalleryImport
                        if (payload.errorCode != ImportErrorCode.NULL) {
                            state.value = NotificationsScreenState.GalleryInstalledError(
                                errorCode = payload.errorCode
                            )
                            return@collect
                        } else {
                            val spaceId = payload.spaceId
                            state.value = NotificationsScreenState.GalleryInstalled(
                                spaceId = spaceId,
                                galleryName = payload.name
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onErrorButtonClick() {
        command.value = Command.Dismiss
    }

    fun onSpaceClick(spaceId: SpaceId) {
        viewModelScope.launch {
            Timber.d("Setting space: $spaceId")
            analytics.sendEvent(eventName = EventsDictionary.switchSpace)
            spaceManager.set(spaceId.id).fold(
                onSuccess = {
                    saveCurrentSpace.async(SaveCurrentSpace.Params(spaceId)).fold(
                        onFailure = {
                            Timber.e(it, "Error while saving current space in user settings")
                        },
                        onSuccess = {
                            command.value = Command.NavigateToSpace(spaceId)
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
        object Dismiss : Command()
        data class NavigateToSpace(val spaceId: SpaceId) : Command()
    }
}



sealed class NotificationsScreenState {
    object Hidden : NotificationsScreenState()
    data class GalleryInstalled(
        val spaceId: SpaceId,
        val galleryName: String
    ) : NotificationsScreenState()
    data class GalleryInstalledError(
        val errorCode: ImportErrorCode
    ) : NotificationsScreenState()
}