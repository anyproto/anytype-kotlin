package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.data.auth.event.EventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber

class MiddlewareEventChannel(
    private val events: EventProxy,
    private val featureToggles: FeatureToggles
) : EventRemoteChannel {

    override fun observeEvents(
        context: String?
    ): Flow<List<Event>> = events
        .flow()
        .filter { event -> context == null || event.contextId == context }
        .map { event ->
            event.messages.filter { it.isAccepted() }.map { message -> Pair(event.contextId, message) }
        }
        .filter { it.isNotEmpty() }
        .map { events -> processEvents(events) }

    private fun processEvents(events: List<Pair<String, anytype.Event.Message>>): List<Event.Command> {
        return events.mapNotNull { (context, event) -> event.toCoreModels(context) }
    }

    private fun anytype.Event.Message.isAccepted() : Boolean = when {
        blockAdd != null -> true
        blockSetText != null -> true
        blockSetChildrenIds != null -> true
        blockSetBackgroundColor != null -> true
        objectDetailsSet != null -> true
        objectDetailsAmend != null -> true
        objectDetailsUnset != null -> true
        blockDelete != null -> true
        blockSetLink != null -> true
        blockSetFile != null -> true
        blockSetFields != null -> true
        blockSetBookmark != null -> true
        blockSetAlign != null -> true
        blockSetDiv != null -> true
        blockSetRelation != null -> true
        blockDataviewRelationSet != null -> true
        blockDataviewRelationDelete != null -> true
        blockDataviewViewDelete != null -> true
        blockDataviewViewOrder != null -> true
        blockDataviewViewSet != null -> true
        objectRelationsAmend != null -> true
        objectRelationsRemove != null -> true
        blockDataviewViewUpdate != null -> true
        blockDataviewTargetObjectIdSet != null -> true
        blockDataviewIsCollectionSet != null -> true
        blockSetWidget != null -> true
        else -> false.also {
            if (featureToggles.isLogMiddlewareInteraction && threadStatus == null)
                Timber.w("Ignored event: $this")
        }
    }
}