package com.agileburo.anytype.data.auth.event

import com.agileburo.anytype.data.auth.model.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRemoteChannel {
    fun observeEvents(): Flow<List<EventEntity>>
}