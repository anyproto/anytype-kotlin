package com.agileburo.anytype.analytics.base

import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.Props
import com.agileburo.anytype.analytics.props.UserProperty
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
    fun updateUserProperty(property: UserProperty)

    /**
     * Return a stream of [EventAnalytics]
     */
    fun observeEvents(): Flow<EventAnalytics>
}

fun CoroutineScope.sendEvent(
    analytics: Analytics,
    startTime: Long? = null,
    middleTime: Long? = null,
    renderTime: Long? = null,
    eventName: String,
    prettified: String? = null,
    props: Props = Props.empty()
) = this.launch {
    val event = EventAnalytics.Anytype(
        name = eventName,
        prettified = prettified,
        props = props,
        duration = EventAnalytics.Duration(
            start = startTime,
            middleware = middleTime,
            render = renderTime
        )
    )
    analytics.registerEvent(event)
}