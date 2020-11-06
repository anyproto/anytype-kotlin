package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.data.auth.model.EventEntity
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MiddlewareEventChannel(
    private val events: EventProxy
) : EventRemoteChannel {

    private fun filter(msg: Event.Message): Boolean {
        val events = listOf(
            msg.blockShow,
            msg.blockAdd,
            msg.blockSetText,
            msg.blockSetChildrenIds,
            msg.blockSetBackgroundColor,
            msg.blockSetDetails,
            msg.blockDelete,
            msg.blockSetLink,
            msg.blockSetFile,
            msg.blockSetFields,
            msg.blockSetBookmark,
            msg.blockSetAlign
        )
        return events.any { it != null }
    }

    override fun observeEvents(
        context: String?
    ): Flow<List<EventEntity>> = events
        .flow()
        .filter { event -> context == null || event.contextId == context }
        .map { event ->
            event.messages.filter { filter(it) }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events -> processEvents(events) }

    private fun processEvents(events: List<Pair<String, Event.Message>>): List<EventEntity.Command> {
        return events.mapNotNull { (context, event) -> event.toEntity(context) }
    }
}