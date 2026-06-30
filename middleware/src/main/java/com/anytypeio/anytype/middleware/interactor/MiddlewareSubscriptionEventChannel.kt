package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.EventGroup
import com.anytypeio.anytype.middleware.mappers.parse
import com.anytypeio.anytype.middleware.mappers.toCoreModelsGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class MiddlewareSubscriptionEventChannel(
    private val events: EventProxy,
    scope: CoroutineScope
) : SubscriptionEventRemoteChannel {

    /**
     * Each event payload is parsed once, subscriber-agnostically, into [ParsedSubEvent]s and
     * shared across every [subscribe] caller. This replaces the previous design where each
     * subscriber independently collected [EventProxy.flow] and re-parsed every event.
     */
    private val parsed: Flow<List<ParsedSubEvent>> = events
        .flow(EventGroup.SUBSCRIPTION)
        .map { payload -> payload.messages.mapNotNull { parseMessage(it) } }
        .filter { it.isNotEmpty() }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            replay = 0
        )

    override fun subscribe(subscriptions: List<Id>): Flow<List<SubscriptionEvent>> {
        // Precomputed once per subscribe() call (not per event/message).
        val exact = HashSet(subscriptions)
        val dep = HashSet<Id>(subscriptions.size * 2)
        for (s in subscriptions) {
            dep.add(s)
            dep.add(s + DEPENDENT_SUBSCRIPTION_POST_FIX)
        }
        return parsed
            .map { parsedEvents ->
                parsedEvents.mapNotNull { parsedEvent ->
                    val matches = if (parsedEvent.dep) {
                        parsedEvent.keys.any { it in dep }
                    } else {
                        parsedEvent.keys.any { it in exact }
                    }
                    if (matches) parsedEvent.event else null
                }
            }
            .filter { it.isNotEmpty() }
    }

    private fun parseMessage(message: Event.Message): ParsedSubEvent? = when {
        message.objectDetailsAmend != null -> {
            val event = message.objectDetailsAmend
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Amend(
                    target = event.id,
                    diff = event.details.associate { it.key to it.value_ },
                    subscriptions = event.subIds
                ),
                keys = event.subIds,
                dep = true
            )
        }
        message.objectDetailsUnset != null -> {
            val event = message.objectDetailsUnset
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Unset(
                    target = event.id,
                    keys = event.keys,
                    subscriptions = event.subIds
                ),
                keys = event.subIds,
                dep = true
            )
        }
        message.objectDetailsSet != null -> {
            val event = message.objectDetailsSet
            checkNotNull(event)
            val data = event.details
            if (data != null) {
                ParsedSubEvent(
                    event = SubscriptionEvent.Set(
                        target = event.id,
                        data = data,
                        subscriptions = event.subIds
                    ),
                    keys = event.subIds,
                    dep = true
                )
            } else {
                null
            }
        }
        message.subscriptionRemove != null -> {
            val event = message.subscriptionRemove
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Remove(
                    target = event.id,
                    subscription = event.subId
                ),
                keys = listOf(event.subId),
                dep = true
            )
        }
        message.subscriptionAdd != null -> {
            val event = message.subscriptionAdd
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Add(
                    target = event.id,
                    afterId = event.afterId,
                    subscription = event.subId
                ),
                keys = listOf(event.subId),
                dep = true
            )
        }
        message.subscriptionPosition != null -> {
            val event = message.subscriptionPosition
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Position(
                    target = event.id,
                    afterId = event.afterId
                ),
                keys = listOf(event.subId),
                dep = false
            )
        }
        message.subscriptionCounters != null -> {
            val event = message.subscriptionCounters
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Counter(
                    counter = event.parse()
                ),
                keys = listOf(event.subId),
                dep = false
            )
        }
        message.subscriptionGroups != null -> {
            val event = message.subscriptionGroups
            checkNotNull(event)
            val group = event.group
            if (group != null) {
                ParsedSubEvent(
                    event = SubscriptionEvent.Group(
                        group = group.toCoreModelsGroup(),
                        remove = event.remove,
                        subscription = event.subId
                    ),
                    keys = listOf(event.subId),
                    dep = false
                )
            } else {
                null
            }
        }
        else -> null
    }

    private data class ParsedSubEvent(
        val event: SubscriptionEvent,
        val keys: List<Id>,
        val dep: Boolean
    )

    companion object {
        const val DEPENDENT_SUBSCRIPTION_POST_FIX = "/dep"
    }
}
