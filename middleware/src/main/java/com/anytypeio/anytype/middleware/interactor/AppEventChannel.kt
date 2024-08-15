package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface AppEventChannel {
    fun flow(): Flow<Event>
    suspend fun emit(event: Event)
}

class AppEventChannelImpl(
    replay: Int = 0,
    extraBufferCapacity: Int = 1,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : AppEventChannel {

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