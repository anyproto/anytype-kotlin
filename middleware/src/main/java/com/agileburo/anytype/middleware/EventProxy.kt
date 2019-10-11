package com.agileburo.anytype.middleware

import kotlinx.coroutines.flow.Flow

interface EventProxy {
    fun flow(): Flow<Event>
}