package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.data.auth.event.EventRemoteChannel
import com.agileburo.anytype.data.auth.model.EventEntity
import com.agileburo.anytype.middleware.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MiddlewareEventChannel(
    private val events: EventProxy
) : EventRemoteChannel {

    /**
     * List of currently supported events.
     * Other events will be ignored.
     */
    private val supportedEvents = listOf(
        Events.Event.Message.ValueCase.BLOCKSHOW,
        Events.Event.Message.ValueCase.BLOCKADD,
        Events.Event.Message.ValueCase.BLOCKSETTEXT,
        Events.Event.Message.ValueCase.BLOCKSETCHILDRENIDS,
        Events.Event.Message.ValueCase.BLOCKDELETE,
        Events.Event.Message.ValueCase.BLOCKSETLINK
    )

    override fun observeEvents(
        context: String?
    ): Flow<List<EventEntity>> = events
        .flow()
        .filter { context == null || it.contextId == context }
        .map { event ->
            event.messagesList.filter { message ->
                supportedEvents.contains(message.valueCase)
            }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events -> processEvents(events) }

    private fun processEvents(events: List<Pair<String, Events.Event.Message>>): List<EventEntity.Command> {
        return events.mapNotNull { (context, event) ->
            when (event.valueCase) {
                Events.Event.Message.ValueCase.BLOCKADD -> {
                    EventEntity.Command.AddBlock(
                        context = context,
                        blocks = event.blockAdd.blocksList.blocks()
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSHOW -> {
                    EventEntity.Command.ShowBlock(
                        context = context,
                        rootId = event.blockShow.rootId,
                        blocks = event.blockShow.blocksList.blocks()
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSETTEXT -> {
                    EventEntity.Command.GranularChange(
                        context = context,
                        id = event.blockSetText.id,
                        text = if (event.blockSetText.hasText())
                            event.blockSetText.text.value
                        else null,
                        style = if (event.blockSetText.hasStyle())
                            event.blockSetText.style.value.entity()
                        else
                            null,
                        color = if (event.blockSetText.hasColor())
                            event.blockSetText.color.value
                        else
                            null,
                        marks = if (event.blockSetText.hasMarks())
                            event.blockSetText.marks.value.marksList.marks()
                        else
                            null
                    )
                }
                Events.Event.Message.ValueCase.BLOCKDELETE -> {
                    EventEntity.Command.DeleteBlock(
                        context = context,
                        target = event.blockDelete.blockId
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSETCHILDRENIDS -> {
                    EventEntity.Command.UpdateStructure(
                        context = context,
                        id = event.blockSetChildrenIds.id,
                        children = event.blockSetChildrenIds.childrenIdsList.toList()
                    )
                }
                Events.Event.Message.ValueCase.BLOCKSETLINK -> {
                    EventEntity.Command.LinkGranularChange(
                        context = context,
                        id = event.blockSetLink.id,
                        target = event.blockSetLink.targetBlockId.value,
                        fields = if (event.blockSetLink.hasFields())
                            event.blockSetLink.fields.value.fields()
                        else
                            null
                    )
                }
                else -> null
            }
        }
    }
}