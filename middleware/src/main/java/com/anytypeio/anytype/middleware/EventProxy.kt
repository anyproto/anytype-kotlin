package com.anytypeio.anytype.middleware

import anytype.Event
import kotlinx.coroutines.flow.Flow

interface EventProxy {
    fun flow(): Flow<Event>
}