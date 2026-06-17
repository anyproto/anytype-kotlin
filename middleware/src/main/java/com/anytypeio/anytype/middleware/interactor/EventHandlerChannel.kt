package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.middleware.EventGroup
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

interface EventHandlerChannel {
    /** @deprecated legacy shared firehose; migrate consumers to [flow] with an [EventGroup]. */
    fun flow(): Flow<Event>

    /** Per-event-type buffered stream backed by a per-collector UNLIMITED channel. */
    fun flow(group: EventGroup): Flow<Event>

    /** @deprecated legacy suspending emit into the shared firehose; use [dispatch]. */
    suspend fun emit(event: Event)

    /** Non-blocking demux into the per-type registry. Called by the single decode pump. */
    fun dispatch(event: Event)
}

class EventHandlerChannelImpl : EventHandlerChannel {

    // --- Legacy shared-flow path (still used by un-migrated consumers; removed once every consumer
    //     moves to flow(group)/dispatch). ---
    private val _channel = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 1
    )

    override fun flow(): Flow<Event> = _channel

    override suspend fun emit(event: Event) {
        _channel.emit(event)
    }

    fun trySend(event: Event) {
        _channel.tryEmit(event)
    }

    // --- Per-collector buffered fan-out.
    //     See docs/superpowers/specs/2026-06-17-event-fanout-per-type-buffer-design.md ---

    // group -> live per-collector inboxes. Thread-safe: dispatch reads on the (single) pump thread;
    // register/unregister run on collector coroutines. Built from all enum values, so getValue never
    // throws (invariant).
    private val subscribers: Map<EventGroup, MutableSet<Channel<Event>>> =
        EventGroup.values().associateWith { CopyOnWriteArraySet<Channel<Event>>() }

    // Observability only: approximate queued-but-undrained events across all collectors.
    private val backlog = AtomicInteger(0)

    // Non-blocking: never suspends, never parks the pump. A slow consumer of group X grows only X's
    // per-collector channels; the pump and every other group keep flowing.
    override fun dispatch(event: Event) {
        val mask = EventGroup.groupBits(event)
        for (group in GROUPS) {
            if (mask and group.bit != 0) {
                for (inbox in subscribers.getValue(group)) {
                    if (inbox.trySend(event).isSuccess) {
                        val depth = backlog.incrementAndGet()
                        if (depth >= BACKLOG_WARN_THRESHOLD && depth % BACKLOG_WARN_THRESHOLD == 0) {
                            Timber.w("Middleware event backlog high for $group: $depth queued")
                        }
                    }
                }
            }
        }
    }

    // Per-collector UNLIMITED buffer. Registers on collect, drains FIFO to this collector, and
    // unregisters (then closes) in finally. `emit(event)` here is the flow{} FlowCollector.emit —
    // NOT the legacy member emit (the flow{} receiver takes precedence).
    override fun flow(group: EventGroup): Flow<Event> = flow {
        val inbox = Channel<Event>(capacity = Channel.UNLIMITED)
        val subs = subscribers.getValue(group)
        subs.add(inbox)
        try {
            for (event in inbox) {
                backlog.decrementAndGet()
                emit(event)
            }
        } finally {
            subs.remove(inbox)
            inbox.close()
        }
    }

    private companion object {
        private val GROUPS = EventGroup.values()
        private const val BACKLOG_WARN_THRESHOLD = 5_000
    }
}
