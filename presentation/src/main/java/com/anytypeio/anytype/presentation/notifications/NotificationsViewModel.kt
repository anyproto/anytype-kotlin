package com.anytypeio.anytype.presentation.notifications

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ImportErrorCode
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val analytics: Analytics,
    private val notificationsProvider: NotificationsProvider
) : BaseViewModel() {

    val state = MutableStateFlow<NotificationsScreenState>(NotificationsScreenState.Hidden)

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