package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.data.auth.model.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRemoteChannel {
    fun observeEvents(context: String? = null): Flow<List<EventEntity>>
}