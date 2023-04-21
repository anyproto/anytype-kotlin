package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MiddlewareEventChannel(
    private val events: EventProxy
) : EventRemoteChannel {

    private fun filter(msg: anytype.Event.Message): Boolean {
        // TODO move to class property, also we should log non filtered events
        val events = listOf(
            msg.blockAdd,
            msg.blockSetText,
            msg.blockSetChildrenIds,
            msg.blockSetBackgroundColor,
            msg.objectDetailsSet,
            msg.objectDetailsAmend,
            msg.objectDetailsUnset,
            msg.blockDelete,
            msg.blockSetLink,
            msg.blockSetFile,
            msg.blockSetFields,
            msg.blockSetBookmark,
            msg.blockSetAlign,
            msg.blockSetDiv,
            msg.blockSetRelation,
            msg.blockDataviewRelationSet,
            msg.blockDataviewRelationDelete,
            msg.blockDataviewViewDelete,
            msg.blockDataviewViewSet,
            msg.objectRelationsAmend,
            msg.objectRelationsRemove,
            msg.blockDataviewViewUpdate,
            msg.blockDataviewTargetObjectIdSet,
            msg.blockDataviewIsCollectionSet
        )
        return events.any { it != null }
    }

    override fun observeEvents(
        context: String?
    ): Flow<List<Event>> = events
        .flow()
        .filter { event -> context == null || event.contextId == context }
        .map { event ->
            event.messages.filter { filter(it) }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events -> processEvents(events) }

    private fun processEvents(events: List<Pair<String, anytype.Event.Message>>): List<Event.Command> {
        return events.mapNotNull { (context, event) -> event.toCoreModels(context) }
    }
}