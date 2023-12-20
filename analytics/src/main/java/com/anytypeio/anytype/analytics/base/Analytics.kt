package com.anytypeio.anytype.analytics.base

import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface Analytics {
    /**
     * Deliver a new [EventAnalytics] to an analytics tracker.
     */
    suspend fun registerEvent(event: EventAnalytics)

    /**
     * Update current [UserProperty]
     */
    suspend fun updateUserProperty(property: UserProperty)

    /**
     * Return a stream of [EventAnalytics]
     */
    fun observeEvents(): Flow<EventAnalytics>

    fun observeUserProperties(): Flow<UserProperty>
}

fun CoroutineScope.sendEvent(
    analytics: Analytics,
    startTime: Long? = null,
    middleTime: Long? = null,
    renderTime: Long? = null,
    eventName: String,
    props: Props = Props.empty()
) = this.launch {
    val event = EventAnalytics.Anytype(
        name = eventName,
        props = props,
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middleTime,
            render = renderTime
        )
    )
    analytics.registerEvent(event)
}

suspend fun Analytics.sendEvent(
    startTime: Long? = null,
    middleTime: Long? = null,
    renderTime: Long? = null,
    eventName: String,
    props: Props = Props.empty()
) {
    val event = EventAnalytics.Anytype(
        name = eventName,
        props = props,
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middleTime,
            render = renderTime
        )
    )
    registerEvent(event)
}

fun CoroutineScope.updateUserProperties(
    analytics: Analytics,
    userProperty: UserProperty
) = this.launch {
    analytics.updateUserProperty(userProperty)
}