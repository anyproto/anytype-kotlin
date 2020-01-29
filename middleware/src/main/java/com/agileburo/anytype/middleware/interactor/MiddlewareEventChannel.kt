package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.data.auth.event.EventRemoteChannel
import com.agileburo.anytype.data.auth.model.EventEntity
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.blocks
import com.agileburo.anytype.middleware.entity
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
        Events.Event.Message.ValueCase.BLOCKDELETE
    )

    override fun observeEvents(): Flow<List<EventEntity>> = events
        .flow()
        .map { event ->
            event.messagesList.filter { message ->
                supportedEvents.contains(message.valueCase)
            }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events ->
            events.mapNotNull { (context, event) ->
                when (event.valueCase) {
                    Events.Event.Message.ValueCase.BLOCKADD -> {
                        EventEntity.Command.AddBlock(
                            blocks = event.blockAdd.blocksList.blocks()
                        )
                    }
                    Events.Event.Message.ValueCase.BLOCKSHOW -> {
                        EventEntity.Command.ShowBlock(
                            rootId = event.blockShow.rootId,
                            blocks = event.blockShow.blocksList.blocks()
                        )
                    }
                    Events.Event.Message.ValueCase.BLOCKSETTEXT -> {
                        EventEntity.Command.GranularChange(
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
                                null
                        )
                    }
                    Events.Event.Message.ValueCase.BLOCKDELETE -> {
                        EventEntity.Command.DeleteBlock(
                            target = event.blockDelete.blockId
                        )
                    }
                    Events.Event.Message.ValueCase.BLOCKSETCHILDRENIDS -> {
                        EventEntity.Command.UpdateStructure(
                            id = event.blockSetChildrenIds.id,
                            children = event.blockSetChildrenIds.childrenIdsList.toList(),
                            context = context
                        )
                    }
                    else -> null
                }
            }
        }
}