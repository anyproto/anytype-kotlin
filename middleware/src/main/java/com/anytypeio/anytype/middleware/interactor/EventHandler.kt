package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.data.auth.event.PushKeyRemoteChannel
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.middleware.EventProxy
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import service.Service.setEventHandlerMobile
import timber.log.Timber

class EventHandler @Inject constructor(
    private val logger: MiddlewareProtobufLogger,
    private val scope: CoroutineScope,
    private val channel: EventHandlerChannel,
    private val syncP2PStore: SyncAndP2PStatusEventsStore,
    private val pushKeyRemoteChannel: PushKeyRemoteChannel
) : EventProxy {

    init {
        scope.launch {
            pushKeyRemoteChannel.start()
        }
        scope.launch {
            syncP2PStore.start()
        }
        scope.launch {
            setEventHandlerMobile { bytes ->
                if (bytes != null) {
                    scope.launch {
                        handle(bytes)
                    }
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
        logger.logEvent(event)
    }

    override fun flow(): Flow<Event> = channel.flow()
}