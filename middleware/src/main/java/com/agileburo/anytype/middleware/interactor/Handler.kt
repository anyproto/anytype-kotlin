package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.middleware.Event
import com.agileburo.anytype.middleware.EventProxy
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import lib.Lib
import timber.log.Timber

class Handler : EventProxy {

    init {
        Timber.d("Subscribing to events")
        Lib.setEventHandlerMobile { bytes ->
            handle(bytes)
        }
    }

    private val channel = Channel<Events.Event>()

    private fun handle(bytes: ByteArray) {
        Timber.d("New event to handle.")
        try {
            Events.Event.parseFrom(bytes).let {
                channel.offer(it)
            }
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Error while deserialize message")
        }
    }

    private fun events(): Flow<Event> = channel
        .consumeAsFlow()
        .onEach { Timber.d(it.toString()) }
        .map { event ->
            Event.AccountAdd(
                id = event.accountAdd.account.id,
                name = event.accountAdd.account.name,
                index = event.accountAdd.index.toInt()
            )
        }

    override fun flow(): Flow<Event> = events()
}