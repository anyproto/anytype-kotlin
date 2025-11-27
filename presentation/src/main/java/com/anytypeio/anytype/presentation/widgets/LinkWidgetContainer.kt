package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.primitives.FieldParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LinkWidgetContainer(
    private val space: SpaceId,
    private val widget: Widget,
    private val fieldParser: FieldParser,
    private val chatPreviewContainer: ChatPreviewContainer
) : WidgetContainer {
    override val view: Flow<WidgetView.Link> = run {
        val source = widget.source
        val isChatDerived = source is Widget.Source.Default && 
            source.obj.uniqueKey == ObjectTypeIds.CHAT_DERIVED
        
        if (isChatDerived && source is Widget.Source.Default) {
            chatPreviewContainer
                .observePreviewsBySpaceId(space)
                .distinctUntilChanged()
                .map { previews ->
                    val preview = previews.find { it.chat == source.obj.id }
                    val state = preview?.state
                    WidgetView.Link(
                        id = widget.id,
                        source = widget.source,
                        icon = widget.icon,
                        name = widget.source.getPrettyName(fieldParser),
                        sectionType = widget.sectionType,
                        counter = if (preview != null && state != null) {
                            WidgetView.ChatCounter(
                                unreadMentionCount = state.unreadMentions?.counter ?: 0,
                                unreadMessageCount = state.unreadMessages?.counter ?: 0
                            )
                        } else null
                    )
                }
        } else {
            flowOf(
                WidgetView.Link(
                    id = widget.id,
                    source = widget.source,
                    icon = widget.icon,
                    name = widget.source.getPrettyName(fieldParser),
                    sectionType = widget.sectionType
                )
            )
        }
    }
}
