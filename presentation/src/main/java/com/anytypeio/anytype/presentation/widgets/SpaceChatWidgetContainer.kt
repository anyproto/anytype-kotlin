package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationStateCalculator
import com.anytypeio.anytype.core_models.chats.NotificationState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SpaceChatWidgetContainer @Inject constructor(
    private val widget: Widget,
    private val container: ChatPreviewContainer,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val notificationPermissionManager: NotificationPermissionManager
) : WidgetContainer {
    override val view: Flow<WidgetView.SpaceChat> = flow {
        emitAll(
            combine(
                container.observePreview(space = SpaceId(widget.config.space)),
                spaceViewSubscriptionContainer.observe(),
                notificationPermissionManager.permissionState()
            ) { preview, spaceViews, _ ->
                val unreadMessageCount = preview?.state?.unreadMessages?.counter ?: 0
                val unreadMentionCount = preview?.state?.unreadMentions?.counter ?: 0
                
                // Find the space view for this widget's space
                val spaceView = spaceViews.find { it.targetSpaceId == widget.config.space }
                val isMuted = NotificationStateCalculator.calculateMutedState(spaceView, notificationPermissionManager)
                
                WidgetView.SpaceChat(
                    id = widget.id,
                    source = widget.source,
                    unreadMessageCount = unreadMessageCount,
                    unreadMentionCount = unreadMentionCount,
                    isMuted = isMuted
                )
            }
            .distinctUntilChanged()
            .debounce(DEBOUNCE_DURATION)
            .onStart {
                emit(
                    WidgetView.SpaceChat(
                        id = widget.id,
                        source = widget.source,
                        isMuted = false // Default to unmuted while loading
                    )
                )
            }
        )
    }.catch {
        emit(
            WidgetView.SpaceChat(
                id = widget.id,
                source = widget.source,
                isMuted = false // Default to unmuted on error
            )
        )
    }



    companion object {
        const val DEBOUNCE_DURATION = 500L
    }
}