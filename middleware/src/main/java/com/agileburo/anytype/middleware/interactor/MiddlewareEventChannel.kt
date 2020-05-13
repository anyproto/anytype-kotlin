package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.data.auth.event.EventRemoteChannel
import com.agileburo.anytype.data.auth.model.EventEntity
import com.agileburo.anytype.middleware.EventProxy
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
        Events.Event.Message.ValueCase.BLOCKSETBACKGROUNDCOLOR,
        Events.Event.Message.ValueCase.BLOCKSETDETAILS,
        Events.Event.Message.ValueCase.BLOCKDELETE,
        Events.Event.Message.ValueCase.BLOCKSETLINK,
        Events.Event.Message.ValueCase.BLOCKSETFILE,
        Events.Event.Message.ValueCase.BLOCKSETFIELDS,
        Events.Event.Message.ValueCase.BLOCKSETBOOKMARK,
        Events.Event.Message.ValueCase.BLOCKSETALIGN
    )

    override fun observeEvents(
        context: String?
    ): Flow<List<EventEntity>> = events
        .flow()
        .filter { event -> context == null || event.contextId == context }
        .map { event ->
            event.messagesList.filter { message ->
                supportedEvents.contains(message.valueCase)
            }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events -> processEvents(events) }

    private fun processEvents(events: List<Pair<String, Events.Event.Message>>): List<EventEntity.Command> {
        return events.mapNotNull { (context, event) -> event.toEntity(context) }
    }
}