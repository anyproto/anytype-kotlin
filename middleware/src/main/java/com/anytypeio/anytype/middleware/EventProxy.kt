package com.anytypeio.anytype.middleware

import anytype.Event
import kotlinx.coroutines.flow.Flow

interface EventProxy {
    /** Per-event-type buffered stream — a slow consumer of one group cannot stall the others. */
    fun flow(group: EventGroup): Flow<Event>
}
