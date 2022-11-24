package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.parse
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

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
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds }) {
                            Timber.d("Subscription AMEND ${event.subIds}")
                            SubscriptionEvent.Amend(
                                target = event.id,
                                diff = event.details.associate { it.key to it.value_ },
                                subscriptions = event.subIds
                            )
                        } else {
                            null
                        }
                    }
                    e.objectDetailsUnset != null -> {
                        val event = e.objectDetailsUnset
                        checkNotNull(event)
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds }) {
                            Timber.d("Subscription UNSET ${event.subIds}")
                            SubscriptionEvent.Unset(
                                target = event.id,
                                keys = event.keys,
                                subscriptions = event.subIds
                            )
                        } else {
                            null
                        }
                    }
                    e.objectDetailsSet != null -> {
                        val event = e.objectDetailsSet
                        checkNotNull(event)
                        val data = event.details
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds } && data != null) {
                            Timber.d("Subscription SET ${event.subIds}")
                            SubscriptionEvent.Set(
                                target = event.id,
                                data = data,
                                subscriptions = event.subIds
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionRemove != null -> {
                        val event = e.subscriptionRemove
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" == event.subId }) {
                            Timber.d("Subscription REMOVE ${event.subId}")
                            SubscriptionEvent.Remove(
                                target = event.id,
                                subscription = event.subId
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionAdd != null -> {
                        val event = e.subscriptionAdd
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" == event.subId }) {
                            Timber.d("Subscription ADD: ${event.subId}")
                            SubscriptionEvent.Add(
                                target = event.id,
                                afterId = event.afterId,
                                subscription = event.subId
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionPosition != null -> {
                        val event = e.subscriptionPosition
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId }) {
                            Timber.d("Subscription POSITION: ${event.subId}")
                            SubscriptionEvent.Position(
                                target = event.id,
                                afterId = event.afterId
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionCounters != null -> {
                        val event = e.subscriptionCounters
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId }) {
                            Timber.d("Subscription COUNTERS: ${event.subId}")
                            SubscriptionEvent.Counter(
                                counter = event.parse()
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

    companion object {
        const val DEPENDENT_SUBSCRIPTION_POST_FIX = "/dep"
    }
}