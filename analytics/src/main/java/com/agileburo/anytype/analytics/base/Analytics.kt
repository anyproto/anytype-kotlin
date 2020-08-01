package com.agileburo.anytype.analytics.base

import com.agileburo.anytype.analytics.event.Event
import com.agileburo.anytype.analytics.props.UserProperty
import kotlinx.coroutines.flow.Flow

interface Analytics {
    /**
     * Deliver a new [Event] to an analytics tracker.
     */
    suspend fun registerEvent(event: Event)

    /**
     * Update current [UserProperty]
     */
    fun updateUserProperty(property: UserProperty)

    /**
     * Return a stream of [Event]
     */
    fun observeEvents(): Flow<Event>
}