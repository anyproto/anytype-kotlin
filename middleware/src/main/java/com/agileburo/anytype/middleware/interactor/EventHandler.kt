package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.middleware.EventProxy
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import lib.Lib.setEventHandlerMobile
import timber.log.Timber

class EventHandler(
    private val scope: CoroutineScope = GlobalScope
) : EventProxy {

    private val channel = BroadcastChannel<Events.Event>(1)

    init {
        setEventHandlerMobile { bytes ->
            scope.launch {
                handle(bytes)
            }
        }
    }

    private suspend fun handle(bytes: ByteArray) {
        try {
            Events.Event.parseFrom(bytes).let {
                Timber.d("New event from middleware:\n$it")
                channel.send(it)
            }
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Error while deserializing message")
        }
    }

    override fun flow(): Flow<Events.Event> = channel.asFlow()
}