package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Event
import kotlinx.coroutines.flow.Flow

interface EventRemoteChannel {
    fun observeEvents(context: String? = null): Flow<List<Event>>
}