package com.agileburo.anytype.middleware

import anytype.Events
import kotlinx.coroutines.flow.Flow

interface EventProxy {
    fun flow(): Flow<Events.Event>
}