package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface EventHandlerChannel {
    fun flow(): Flow<Event>
    suspend fun emit(event: Event)
}

class EventHandlerChannelImpl(
    replay: Int = 0,
    extraBufferCapacity: Int = 1,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : EventHandlerChannel {

    private val _channel = MutableSharedFlow<Event>(
        replay = replay,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow
    )

    override fun flow(): Flow<Event> = _channel

    override suspend fun emit(event: Event) {
        _channel.emit(event)
    }

    fun trySend(event: Event) {
        _channel.tryEmit(event)
    }
}