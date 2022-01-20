package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

class MiddlewareSubscriptionEventChannel(
    private val events: EventProxy
) : SubscriptionEventRemoteChannel {

    override fun subscribe(subscriptions: List<Id>) = events
        .flow()
        .mapNotNull { payload ->
            payload.messages.mapNotNull { e ->
                when {
                    e.objectDetailsAmend != null -> {
                        val event = e.objectDetailsAmend
                        checkNotNull(event)
                        if (subscriptions.any { it in event.subIds }) {
                            SubscriptionEvent.Amend(
                                target = event.id,
                                diff = event.details.associate { it.key to it.value }
                            )
                        } else {
                            null
                        }
                    }
                    e.objectDetailsUnset != null -> {
                        val event = e.objectDetailsUnset
                        checkNotNull(event)
                        if (subscriptions.any { it in event.subIds }) {
                            SubscriptionEvent.Unset(
                                target = event.id,
                                keys = event.keys
                            )
                        } else {
                            null
                        }
                    }
                    e.objectDetailsSet != null -> {
                        val event = e.objectDetailsSet
                        checkNotNull(event)
                        val data = event.details
                        if (subscriptions.any { it in event.subIds } && data != null) {
                            SubscriptionEvent.Set(
                                target = event.id,
                                data = data
                            )
                        } else {
                            null
                        }
                    }
                    else -> {
                        null
                    }
                }
            }
        }
        .filter { it.isNotEmpty() }
}