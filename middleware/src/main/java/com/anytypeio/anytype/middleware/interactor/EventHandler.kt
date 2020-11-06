package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.middleware.BuildConfig
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import service.Service.setEventHandlerMobile
import timber.log.Timber
import java.io.IOException

class EventHandler(
    private val scope: CoroutineScope = GlobalScope
) : EventProxy {

    private val channel = BroadcastChannel<Event>(1)

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
            Event.ADAPTER.decode(bytes).let {
                logEvent(it)
                channel.send(it)
            }
        } catch (e: IOException) {
            Timber.e(e, "Error while deserializing message")
        }
    }

    private fun logEvent(it: Event?) {
        if (BuildConfig.DEBUG) Timber.d("New event from middleware:\n$it")
    }

    override fun flow(): Flow<Event> = channel.asFlow()
}