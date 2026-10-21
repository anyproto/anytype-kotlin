# EventHandler Single-Consumer Event Pump

- **Date:** 2026-06-17
- **Status:** Proposed (pending approval)
- **Scope:** `EventHandler` (middleware) only
- **Type:** Correctness fix (event ordering) + performance (remove per-event coroutine)

## 1. Background & problem

Every middleware event from the Go backend arrives via the gomobile JNI callback
`service.Service.setEventHandlerMobile { bytes -> ... }`. Today (`EventHandler.kt`):

```kotlin
setEventHandlerMobile { bytes -> if (bytes != null) scope.launch { handle(bytes) } }
// handle(): val event = withContext(Dispatchers.IO) { Event.ADAPTER.decode(bytes) }.also { logEvent(it) }; channel.emit(event)
```

This launches **one coroutine per event** on the app scope
(`DEFAULT_APP_COROUTINE_SCOPE = SupervisorJob() + Dispatchers.Default`, multi-threaded) and adds a
per-event `withContext(Dispatchers.IO)` hop before emitting to `EventHandlerChannel`.

### Confirmed facts (from research)

- **The gomobile callback is synchronous and sequential** (HIGH confidence, sourced from the
  anytype-heart Go side): `MessageHandler.Handle([]byte)` returns `void`; Go calls it inline, one
  event at a time, blocking the Go thread until the JVM lambda returns. So the **source emits in
  strict order, single-producer**. The callback **cannot suspend**, **must return fast**, and
  **must not throw** (an uncaught exception crosses JNI and aborts the Go runtime).
- **Ordering is required and currently broken** (HIGH confidence). Downstream subscription
  consumers (`StorelessSubscriptionContainer`, `CrossSpaceSubscriptionContainer`) apply
  `Add/Remove/Set/Amend/Position` as **stateful incremental deltas** via `scan` over a one-shot
  snapshot (`EventAddProcessor`, `EventRemoveProcessor`, `EventAmendProcessor`,
  `EventPositionProcessor` all do `find()`/`indexOf()` against current state). The `sortedBy` in the
  containers only reorders **within a single emission batch**, not across emissions. The current
  per-event `scope.launch` on multi-threaded `Dispatchers.Default` + the `withContext(IO)` hop means
  events can be decoded/emitted **out of order**, silently corrupting that state (e.g. `Remove(o)`
  then `Amend(o)` reordered → ghost object / stale fields).

### What this is

A latent correctness bug (ordering) plus the per-event coroutine churn flagged in the cold-start
CPU profiling (DROID-4525 line of work). This spec fixes the **producer side**.

## 2. Goals / non-goals

### Goals
- Process middleware events with a **single long-lived consumer** — no per-event coroutine.
- Guarantee **strict FIFO** from the gomobile callback through `channel.emit`.
- Keep the JNI callback body **non-suspending, fast, non-throwing**.
- **Never drop events at ingress** (between the callback and the consumer).
- Preserve all existing behavior: decode via `Event.ADAPTER.decode`, log via the debug-only
  `MiddlewareProtobufLogger`, emit to the existing `EventHandlerChannel`; keep
  `scope.launch { syncP2PStore.start() }`; keep the `provideEventHandler` constructor signature and
  the `EventProxy`/`EventHandlerChannel` contracts unchanged.

### Non-goals (explicit)
- **Not** changing the downstream `EventHandlerChannel`
  (`MutableSharedFlow(replay = 0, extraBufferCapacity = 1)`). Its semantics are unchanged:
  - **Unsubscribed collectors miss events** (`replay = 0`) — events emitted while a given collector
    is not attached are not delivered to it. This is pre-existing and **out of scope**.
  - **Cross-subscriber coupling**: `emit` suspends until all current subscribers accept. Pre-existing.
- Therefore this change does **not** claim "never-drop end-to-end." Its guarantee is scoped to
  **producer-side ordering + ingress no-loss**. End-to-end delivery semantics are whatever the
  unchanged `EventHandlerChannel` already provides.
- **Not** touching the per-`subscribe()` fan-out (that is DROID-4525, a separate branch/PR).

## 3. Design

Replace the per-event launch with **one unbounded ingress `Channel<ByteArray>`** drained by
**exactly one** long-lived consumer coroutine.

```
gomobile callback (Go thread, sequential)
   └─ onRawEvent(bytes)  →  inbox.trySend(bytes)   // non-suspending, non-throwing, O(1)
         │
         ▼  Channel<ByteArray>(UNLIMITED)  — FIFO
   single consumer:  for (bytes in inbox) { handle(bytes) }   // one coroutine ⇒ strict serial
         └─ Event.ADAPTER.decode(bytes)  // inline, CPU-bound
            └─ logEvent(it)
               └─ channel.emit(event)    // SUSPENDING → existing EventHandlerChannel SharedFlow
```

### Full implementation

```kotlin
package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.middleware.EventProxy
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import service.Service.setEventHandlerMobile
import timber.log.Timber

class EventHandler @Inject constructor(
    private val logger: MiddlewareProtobufLogger,
    private val scope: CoroutineScope,            // DEFAULT_APP_COROUTINE_SCOPE (SupervisorJob + Dispatchers.Default)
    private val channel: EventHandlerChannel,
    private val syncP2PStore: SyncAndP2PStatusEventsStore
) : EventProxy {

    // Unbounded FIFO inbox of RAW bytes. Single producer (Go delivers sequentially) + single
    // consumer ⇒ strict FIFO. UNLIMITED ⇒ trySend never suspends and never fails on a full buffer
    // (only when the channel is closed). Buffering raw bytes (~0.1–1 KB) costs less than the old
    // per-event coroutine + decoded-Event accumulation.
    private val inbox = Channel<ByteArray>(capacity = Channel.UNLIMITED)

    // Observability only: approximate number of queued-but-unprocessed events.
    private val backlog = AtomicInteger(0)

    init {
        // Preserved verbatim.
        scope.launch {
            syncP2PStore.start()
        }

        // EXACTLY ONE consumer. A single coroutine running a single for-loop processes items
        // strictly serially (decode → log → emit for event N completes before event N+1 is
        // received). This — not any dispatcher trick — is what guarantees ordering. Do NOT add an
        // inner launch or a withContext hop inside this loop; either would re-break FIFO.
        scope.launch {
            for (bytes in inbox) {
                backlog.decrementAndGet()
                handle(bytes)
            }
        }

        // Register the JNI handler on the app-scope dispatcher (preserves the current registration
        // thread). Correctness does not depend on registration ordering: the UNLIMITED inbox buffers
        // anything enqueued before the consumer is scheduled.
        scope.launch {
            setEventHandlerMobile { bytes ->
                onRawEvent(bytes)
            }
        }
    }

    /**
     * Ingress seam. Called on the gomobile (Go) thread. Must be non-suspending and non-throwing.
     * Exposed for tests to feed crafted bytes without the real gomobile AAR (test-only seam;
     * same-module tests reach it via Kotlin `internal` visibility).
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
            // Only reachable after the channel is closed (app scope cancelled). Benign at teardown.
            Timber.w("Dropping middleware event: inbox closed")
        }
    }

    // Runs ONLY on the single consumer. suspend so channel.emit applies real backpressure to the
    // consumer (never to the Go thread).
    private suspend fun handle(bytes: ByteArray) {
        try {
            val event = Event.ADAPTER.decode(bytes).also { logEvent(it) }
            channel.emit(event)
        } catch (e: CancellationException) {
            throw e // never swallow cancellation — let the consumer stop when the scope is cancelled
        } catch (e: Exception) {
            // One malformed/poison event is skipped and logged; the consumer survives and keeps
            // draining. This is essential: with a single consumer, an uncaught throw would kill the
            // pump permanently and lose ALL subsequent events.
            Timber.e(e, "Error while processing middleware event")
        }
    }

    private fun logEvent(event: Event) {
        logger.logEvent(event)
    }

    override fun flow(): Flow<Event> = channel.flow()

    companion object {
        private const val BACKLOG_WARN_THRESHOLD = 5_000
    }
}
```

### Load-bearing decisions

1. **Ingress = `Channel<ByteArray>(UNLIMITED)`.** Only policy satisfying all three hard constraints
   at once: JNI must not suspend (rules out a `SUSPEND` that can fire), must not drop (rules out
   `DROP_OLDEST/LATEST`), and `trySend` on UNLIMITED never fails on a full buffer. Buffer **raw
   bytes**, not decoded `Event`s, to minimize footprint.
2. **Exactly one consumer; ordering comes from the single `for`-loop.** No `limitedParallelism(1)`,
   no dedicated executor — a single coroutine is already strictly serial, and the research showed
   `limitedParallelism` is unused in this codebase and would add no ordering guarantee (only thread
   confinement). Ordering is enforced by a comment + a stress test, not a dispatcher trick.
3. **Decode inline; remove `withContext(Dispatchers.IO)`.** `Event.ADAPTER.decode` is CPU-bound
   protobuf parsing of an in-memory `ByteArray` — IO was the wrong pool, and the per-event reschedule
   hop was a primary reordering seam. It runs on the consumer (app scope = `Dispatchers.Default`).
4. **Boundary emit stays SUSPENDING (`channel.emit`), not `tryEmit`.** `EventHandlerChannelImpl`
   wraps `MutableSharedFlow(replay = 0, extraBufferCapacity = 1)`; `tryEmit` would silently drop when
   a subscriber is slow and the single slot is full. Suspending `emit` parks only the consumer
   (never the Go thread); the UNLIMITED inbox absorbs the burst. So `handle` is `suspend`.
5. **Broadened `catch (Exception)` with `CancellationException` rethrow.** A single consumer is a
   single point of failure: an uncaught non-`IOException` (e.g. a mapper `RuntimeException`) would
   terminate the consumer and stall the whole pipeline forever. Catching `Exception` (while
   rethrowing `CancellationException` so cancellation still works) keeps the consumer alive and
   isolates a bad event — matching the old per-coroutine resilience.
6. **Register the JNI handler inside `scope.launch`** to preserve the current registration thread
   (`EventHandler` is a `@Singleton` possibly constructed on the main thread during cold start;
   moving the native call onto the constructor thread is an unneeded risk). The UNLIMITED inbox makes
   eager registration unnecessary for correctness.
7. **Backlog gauge** (`AtomicInteger` + threshold `Timber.w`) gives observability into the
   unbounded inbox under sustained overload, which otherwise has no signal. No behavior change.
8. **`internal onRawEvent`** is the test seam (same-module tests reach it via Kotlin `internal`
   visibility; no annotation dependency needed): tests feed bytes directly, no gomobile AAR needed.

## 4. Ordering guarantee

Strict FIFO over three links: (1) Go invokes the callback **synchronously and sequentially** (single
producer, true emission order); (2) `Channel.UNLIMITED` is FIFO (receive order == send order);
(3) **one** consumer coroutine processes items strictly serially (a single `for`-loop body with no
inner `launch`/`withContext`). Both old reordering seams — per-event `scope.launch` on multi-threaded
`Default` and the per-event `withContext(IO)` — are removed.

## 5. Risks (and how they're handled)

- **Unbounded inbox → OOM under a permanently stalled consumer** (e.g. a wedged downstream collector
  blocking `emit`). This is the inherent price of never-dropping at ingress (bounded+drop is
  forbidden; bounded+suspend would deadlock the Go thread). It is **strictly less memory than today**
  (raw bytes vs. accumulated per-event coroutines + decoded events). Mitigated by the backlog gauge;
  not throttleable at the Go source. **Documented, accepted.**
- **Head-of-line blocking**: the single consumer serializes `emit`, so the slowest current subscriber
  gates throughput. This coupling is **pre-existing** (today `MutableSharedFlow.emit` already suspends
  until all subscribers accept); the change does not meaningfully worsen it and adds ordering. The
  real fix for cross-subscriber coupling is the downstream channel — out of scope here.
- **Ordering fragility to future edits**: an inner `launch` or a reintroduced `withContext` in the
  loop would silently re-break FIFO. Guarded by a comment and a multi-threaded stress test.
- **Decode-error hole**: skipping a malformed event preserves FIFO but leaves a gap for stateful
  deltas. This matches today's behavior (the current code already swallows `IOException`). Surfacing a
  resync instead of silent continuation is a possible future improvement, out of scope.

## 6. Testing

New test file `middleware/src/test/java/.../interactor/EventHandlerTest.kt`, driving events through the
`onRawEvent` seam with a real `EventHandlerChannelImpl` sink and Turbine (1.1.0) on `EventHandler.flow()`,
using `runTest` for determinism:

1. **Ordering**: feed N distinguishable encoded `Event`s in order; assert `flow()` emits them in the
   same order. Stress variant: feed rapidly / from multiple threads and assert order holds across
   repetitions (the old code would flake here).
2. **Ingress no-loss under a slow collector**: a collector that `delay`s; feed K events; assert all K
   eventually arrive in order (UNLIMITED inbox absorbs the burst; suspending `emit` doesn't drop).
3. **Decode-failure isolation (IOException)**: feed `[valid1, malformed, valid2]`; assert `flow()`
   emits `valid1` then `valid2`, the malformed is skipped, consumer keeps running.
4. **Poison-event isolation (non-IOException)**: feed bytes that cause a `RuntimeException` during
   decode/log; assert the consumer survives and a following valid event is still delivered. (Guards
   decision #5.)
5. **Non-throw on closed**: cancel the scope (or close the inbox), then call `onRawEvent(bytes)`;
   assert it returns normally (simulates a late JNI callback at teardown).
6. **Cancellation**: cancelling the scope stops the consumer (no leak).

## 7. Acceptance criteria

- All new tests pass; `make test_debug_all` (or `:middleware:testDebugUnitTest` + dependents) green.
- Ordering is deterministic under the multi-threaded stress test.
- `:app:compileDebugKotlin` succeeds (no DI/signature change — `provideEventHandler` unchanged).
- Behavior preserved: decode + debug-log + emit to `EventHandlerChannel`; `syncP2PStore.start()` kept.

## 8. Future work (out of scope)

- Downstream `EventHandlerChannel` semantics (`replay = 0` drop-for-unsubscribed; cross-subscriber
  head-of-line coupling) — would require restructuring fan-out (per-subscriber channels or buffered
  broadcast). Evaluate if profiling/bugs warrant it.
- Backlog circuit-breaker / overload policy if the gauge ever shows sustained growth in the field.
- Surfacing a resync on decode failure instead of silently skipping.
