package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.middleware.EventProxy
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import service.Service.setEventHandlerMobile
import timber.log.Timber

class EventHandler @Inject constructor(
    private val logger: MiddlewareProtobufLogger
) : EventProxy {

    private val scope: CoroutineScope = GlobalScope
    private val channel = MutableSharedFlow<Event>(0, 1)

    init {
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

    override fun flow(): Flow<Event> = channel
}