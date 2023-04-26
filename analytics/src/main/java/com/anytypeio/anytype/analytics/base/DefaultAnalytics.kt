package com.anytypeio.anytype.analytics.base

import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.UserProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class DefaultAnalytics : Analytics {
    private val events = MutableSharedFlow<EventAnalytics>(replay = 0)
    private val userProps = MutableSharedFlow<UserProperty>(replay = 0)
    override suspend fun registerEvent(event: EventAnalytics) = events.emit(event)
    override suspend fun updateUserProperty(property: UserProperty) = userProps.emit(property)
    override fun observeEvents(): Flow<EventAnalytics> = events
    override fun observeUserProperties(): Flow<UserProperty> = userProps

    private var analyticsContext: String? = null
    private var analyticsOriginalId: String? = null

    override fun setContext(ctx: String?) {
        analyticsContext = ctx
    }

    override fun getContext(): String? = analyticsContext

    override fun setOriginalId(originalId: String?) {
        analyticsOriginalId = originalId
    }

    override fun getOriginalId(): String? = analyticsOriginalId
}