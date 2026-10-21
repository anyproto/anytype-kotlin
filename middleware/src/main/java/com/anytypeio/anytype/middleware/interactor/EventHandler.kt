package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.middleware.EventGroup
import com.anytypeio.anytype.middleware.EventProxy
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

class EventHandler @Inject constructor(
    private val logger: MiddlewareProtobufLogger,
    private val scope: CoroutineScope,
    private val channel: EventHandlerChannel,
    private val syncP2PStore: SyncAndP2PStatusEventsStore,
    private val registrar: MiddlewareEventRegistrar
) : EventProxy {

    // Unbounded FIFO inbox of RAW bytes. Single producer (Go delivers events sequentially) + a
    // single consumer => strict FIFO. UNLIMITED => trySend never suspends and never fails on a full
    // buffer (only when the channel is closed).
    private val inbox = Channel<ByteArray>(capacity = Channel.UNLIMITED)

    // Observability only: approximate number of queued-but-unprocessed events.
    private val backlog = AtomicInteger(0)

    init {
        scope.launch {
            syncP2PStore.start()
        }

        // EXACTLY ONE consumer. A single coroutine running a single for-loop processes items
        // strictly serially. This single consumer — not any dispatcher trick — guarantees ordering.
        // Do NOT add an inner launch or a withContext hop inside this loop; either would re-break FIFO.
        scope.launch {
            try {
                for (bytes in inbox) {
                    backlog.decrementAndGet()
                    handle(bytes)
                }
            } finally {
                // Teardown (app scope cancelled): close the inbox so a late JNI callback hits the
                // drop-and-log path in onRawEvent instead of silently buffering into a dead consumer.
                inbox.close()
            }
        }

        // Register the JNI handler on the app-scope dispatcher (preserves the current registration
        // thread). Correctness does not depend on registration timing: the UNLIMITED inbox buffers
        // anything enqueued before the consumer is scheduled.
        scope.launch {
            registrar.register(::onRawEvent)
        }
    }

    /**
     * Ingress seam. Invoked on the gomobile (Go) thread; must be non-suspending and non-throwing.
     * Exposed (internal) so unit tests can feed crafted bytes without the gomobile native library.
     */
    internal fun onRawEvent(bytes: ByteArray?) {
        if (bytes == null) return
        val result = inbox.trySend(bytes)
        if (result.isSuccess) {
            val depth = backlog.incrementAndGet()
            if (depth >= BACKLOG_WARN_THRESHOLD && depth % BACKLOG_WARN_THRESHOLD == 0) {
                Timber.w("Middleware event backlog high: $depth queued")
            }
        } else {
            // Reachable at teardown: when the app scope is cancelled the consumer closes the inbox,
            // so a late JNI callback lands here. Dropping is correct (no consumer remains); never throws.
            Timber.w("Dropping middleware event: inbox closed")
        }
    }

    // Runs ONLY on the single consumer. suspend only to propagate CancellationException; channel.dispatch
    // is non-blocking, so a slow consumer grows its own UNLIMITED inbox (no backpressure to the pump or Go thread).
    private suspend fun handle(bytes: ByteArray) {
        try {
            val event = Event.ADAPTER.decode(bytes).also { logEvent(it) }
            channel.dispatch(event)
        } catch (e: CancellationException) {
            throw e // never swallow cancellation — the consumer must stop when the scope is cancelled
        } catch (e: Exception) {
            // One malformed/poison event is skipped and logged; the single consumer survives and
            // keeps draining. An uncaught throw here would kill the pump and lose ALL later events.
            Timber.e(e, "Error while processing middleware event")
        }
    }

    private fun logEvent(event: Event) {
        logger.logEvent(event)
    }

    override fun flow(group: EventGroup): Flow<Event> = channel.flow(group)

    companion object {
        private const val BACKLOG_WARN_THRESHOLD = 5_000
    }
}
