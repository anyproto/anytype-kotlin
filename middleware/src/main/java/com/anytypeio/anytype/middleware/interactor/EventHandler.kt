package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import service.Service.setEventHandlerMobile
import timber.log.Timber
import java.io.IOException

class EventHandler(
    private val scope: CoroutineScope = GlobalScope
) : EventProxy {

    private val channel = MutableSharedFlow<Event>(0, 1)

    init {
        scope.launch {
            setEventHandlerMobile { bytes ->
                scope.launch {
                    handle(bytes)
                }
            }
        }
    }

    private suspend fun handle(bytes: ByteArray) {
        try {
            val event = withContext(Dispatchers.IO) { Event.ADAPTER.decode(bytes) }.also { logEvent(it) }
            channel.emit(event)
        } catch (e: IOException) {
            Timber.e(e, "Error while deserializing message")
        }
    }

    private fun logEvent(event: Event) {
        if (BuildConfig.DEBUG) {
            val message = "[" + "\n" + event::class.java.name + ":" + "\n" + event.toString() + "\n" + "]"
            Timber.d(message)
        }
    }

    override fun flow(): Flow<Event> = channel
}