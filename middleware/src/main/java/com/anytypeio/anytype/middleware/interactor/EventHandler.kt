package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import service.Service.setEventHandlerMobile
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class EventHandler @Inject constructor(
    private val logger: MiddlewareProtobufLogger,
    private val scope: CoroutineScope,
    private val channel: AppEventChannel,
    private val syncP2PStore: SyncAndP2PStatusEventsStore
) : EventProxy {

    init {
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