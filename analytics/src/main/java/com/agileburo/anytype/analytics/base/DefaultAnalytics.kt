package com.agileburo.anytype.analytics.base

import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.UserProperty
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class DefaultAnalytics : Analytics {
    private val events = Channel<EventAnalytics>()
    override suspend fun registerEvent(event: EventAnalytics) = events.send(event)
    override fun observeEvents(): Flow<EventAnalytics> = events.consumeAsFlow()
    override fun updateUserProperty(property: UserProperty) = TODO("Not yet implemented")
}