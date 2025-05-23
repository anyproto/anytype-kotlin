package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SpaceChatWidgetContainer @Inject constructor(
    private val widget: Widget,
    private val container: ChatPreviewContainer
) : WidgetContainer {
    override val view: Flow<WidgetView> = flow {
        emitAll(
            container
                .observePreview(space = SpaceId(widget.config.space))
                .map { preview ->
                    preview?.state.let { state ->
                        state?.unreadMessages?.counter ?: 0
                    } to preview?.state.let { state ->
                        state?.unreadMentions?.counter ?: 0
                    }
                }
                .distinctUntilChanged()
                .map { (unreadMessageCount, unreadMentionCount) ->
                    WidgetView.SpaceChat(
                        id = widget.id,
                        source = widget.source,
                        unreadMessageCount = unreadMessageCount,
                        unreadMentionCount = unreadMentionCount
                    )
                }
        )
    }.catch {
        emit(
            WidgetView.SpaceChat(
                id = widget.id,
                source = widget.source
            )
        )
    }
}