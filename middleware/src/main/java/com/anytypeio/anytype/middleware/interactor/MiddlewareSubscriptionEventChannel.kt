package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
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
                        Timber.d("Subscription AMEND")
                        val event = e.objectDetailsAmend
                        checkNotNull(event)
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds }) {
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
                        Timber.d("Subscription UNSET")
                        val event = e.objectDetailsUnset
                        checkNotNull(event)
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds }) {
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
                        Timber.d("Subscription SET")
                        val event = e.objectDetailsSet
                        checkNotNull(event)
                        val data = event.details
                        if (subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds } && data != null) {
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
                        Timber.d("Subscription REMOVE")
                        val event = e.subscriptionRemove
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" == event.subId }) {
                            SubscriptionEvent.Remove(
                                target = event.id,
                                subscription = payload.contextId
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionAdd != null -> {
                        Timber.d("Subscription ADD")
                        val event = e.subscriptionAdd
                        checkNotNull(event)
                        if (subscriptions.any { it == event.subId || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" == event.subId }) {
                            SubscriptionEvent.Add(
                                target = event.id,
                                afterId = event.afterId,
                                subscription = payload.contextId
                            )
                        } else {
                            null
                        }
                    }
                    e.subscriptionPosition != null -> {
                        Timber.d("Subscription POSITION")
                        val event = e.subscriptionPosition
                        checkNotNull(event)
                        // TODO should I handle here dependent subscriptions?
                        if (subscriptions.any { it == event.subId }) {
                            SubscriptionEvent.Position(
                                target = event.id,
                                afterId = event.afterId
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