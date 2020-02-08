package com.agileburo.anytype.data.auth.event

import com.agileburo.anytype.data.auth.model.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRemoteChannel {
    fun observeEvents(context: String? = null): Flow<List<EventEntity>>
}