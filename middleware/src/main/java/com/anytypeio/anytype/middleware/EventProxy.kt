package com.anytypeio.anytype.middleware

import anytype.Event
import kotlinx.coroutines.flow.Flow

interface EventProxy {
    /** @deprecated legacy shared firehose; migrate consumers to [flow] with an [EventGroup]. */
    fun flow(): Flow<Event>

    /** Per-event-type buffered stream — a slow consumer of one group cannot stall the others. */
    fun flow(group: EventGroup): Flow<Event>
}
