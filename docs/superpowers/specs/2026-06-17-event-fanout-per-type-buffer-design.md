# Top-Level Per-Event-Type Buffered Fan-out

- **Date:** 2026-06-17
- **Status:** Proposed (pending approval)
- **Scope:** `EventHandlerChannel` / `EventProxy` + one-line edits at each event consumer
- **Type:** Decoupling fix (one slow consumer must not stall the others) + memory-isolation
- **Builds on:** DROID-4526 (single ordered decode consumer), complements DROID-4525 (subscription parse-once)

## 1. Background & problem

All middleware events funnel through one shared `MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 1, SUSPEND)` in `EventHandlerChannelImpl` (`EventHandlerChannel.kt:15-18`). That is a **single 1-slot buffer shared by every consumer**: `emit()` cannot advance until the slot is freed, and the slot frees only after the **slowest currently-collecting subscriber** consumes it.

Verified consequence (mapped across the codebase): a consumer that does its heavy work *while holding the slot* — i.e. with no buffer between `flow()` and that work — stalls `emit()` for **every** subscriber. After DROID-4526 (one decode consumer awaiting the suspending `emit`), that stall also **parks the single pump** and the unbounded raw-bytes inbox grows. Most consumers are coupled this way:

- **Coupled** (no own buffer): `SyncAndP2PChannelContainer` (p2p/sync status), all subscription containers (`Storeless`/`CrossSpace`/`ObjectSearch`/`ObjectTypes`/`Relations`/`RelationOptions`/`DataView` — their `flowOn(io)` is placed *after* the `scan`, so it buffers downstream of the heavy work, not before it), `ObjectWatcher`, `ChatContainer`, `ChatPreviewContainer`.
- **Decoupled** (only ones): the generic editor/object-set/home path (`InterceptEvents.flowOn`) and account status (`InterceptAccountStatus.flowOn`) — a 64-slot `flowOn` channel, until it fills.

So today a single slow/stuck consumer (e.g. a heavy subscription `scan`, or `DataViewSubscriptionContainer`'s `counter.emit` inside its scan) backpressures the whole event pipeline and starves unrelated features.

## 2. Goals / non-goals

### Goals
- A slow/stuck consumer of one event type **must not** stall consumers of other types, and **must not** park the decode pump or grow the shared inbox.
- **No new drops vs today:** each collector receives every event dispatched *while it is registered* (i.e. while collecting) — parity with today's `replay=0`. This is **not** an absolute never-drop: a consumer that is not currently collecting misses events, exactly as today (see §6 for the SUBSCRIPTION/4525 nuance and the pre-existing open→attach race).
- **Within-type / within-subscription FIFO** preserved.
- Minimal consumer churn: one call-site edit per consumer; consumer bodies unchanged.

### Non-goals
- Cross-type ordering is **dropped by contract** — verified safe: no consumer correlates two type-groups (the subscription containers already re-sort within an Event, proving intra-Event cross-type order is irrelevant).
- Not changing the DROID-4526 inbox/decode pump.
- Not the typed-event bus rewrite (parse-once for all types). That is a larger, separate end-state; this fix is surgical.

## 3. Design

Keep the DROID-4526 single decode pump untouched. Change **only** `EventHandlerChannelImpl`:

1. **Write side — non-blocking demux.** `dispatch(event)` (the producer method, renamed from `emit` so it can't be confused with `FlowCollector.emit` inside the `flow{}` builder) scans `event.messages` once, computes a bitmask over `EventGroup`, and for each present group `trySend`s the **whole Event** into **every registered subscriber channel** of that group. `trySend` into an `UNLIMITED` channel never suspends and never fails-on-full — so the pump never parks on a slow consumer of any group.
2. **Read side — per-collector buffered registry.** `flow(group)` returns a cold flow that, on collection, creates its **own** `Channel<Event>(UNLIMITED)`, registers it under `group`, drains it to the collector, and unregisters on completion/cancellation. Each collector has its own buffer.

Result: a stuck consumer grows **only its own** `UNLIMITED` channel; the pump, the inbox, and all other consumers are unaffected.

### EventGroup

```kotlin
// :middleware, next to EventProxy (verified: all flow(group) call-sites are in :middleware)
enum class EventGroup {
    EDITOR,        // block*, blockDataview*, objectRelations*, objectDetails*
    SUBSCRIPTION,  // objectDetails* + subscriptionAdd/Remove/Position/Counters
    CHAT,          // chat*
    SYNC_P2P,      // p2pStatusUpdate, spaceSyncStatusUpdate
    PROCESS,       // processNew/Update/Done
    ACCOUNT,       // account* (coarse): accountShow (AuthMiddleware/login) + accountUpdate (AccountStatusMiddlewareChannel) + accountConfigUpdate/accountLinkChallenge/…
    FILE,          // fileSpaceUsage/fileLocalUsage/fileLimitReached/fileLimitUpdated
    MEMBERSHIP,    // membershipUpdate, membershipTiersUpdate
    NOTIFICATIONS; // notificationUpdate, notificationSend

    val bit: Int get() = 1 shl ordinal
}
```

`objectDetails*` deliberately sets **both** the EDITOR and SUBSCRIPTION bits — routing is **fan-out, not partition** (the same Event reference is sent to both groups' subscribers; cheap).

### EventHandlerChannelImpl

```kotlin
class EventHandlerChannelImpl : EventHandlerChannel {

    // group -> set of live per-collector inboxes. Thread-safe: dispatch reads on the (single) pump
    // thread; register/unregister run on collector coroutines. Built once from all enum values, so
    // every group key is always present (getValue never throws — invariant).
    private val subscribers: Map<EventGroup, MutableSet<Channel<Event>>> =
        EventGroup.values().associateWith { CopyOnWriteArraySet<Channel<Event>>() }

    // Called by the single decode pump. Renamed from emit() to avoid shadowing FlowCollector.emit
    // inside the flow{} builder below. NON-SUSPENDING (plain fun, enforced by the type system) so a
    // future suspending call can't silently re-couple the pump to a slow consumer (the DROID-4526
    // failure mode). Never parks the pump.
    override fun dispatch(event: Event) {
        val mask = groupMask(event)
        for (group in GROUPS) {                  // cached: EventGroup.values() copies an array per call
            if (mask and group.bit != 0) {
                for (inbox in subscribers.getValue(group)) inbox.trySend(event) // UNLIMITED => never fails while open
            }
        }
    }

    // Per-collector UNLIMITED buffer. A collector receives every event dispatched WHILE IT IS
    // REGISTERED (i.e. while collecting) — parity with today's replay=0, NOT an absolute never-drop.
    override fun flow(group: EventGroup): Flow<Event> = flow {
        val inbox = Channel<Event>(capacity = Channel.UNLIMITED)
        val subs = subscribers.getValue(group)
        subs.add(inbox)
        try {
            for (event in inbox) emit(event)     // FlowCollector.emit (NOT dispatch); FIFO drain to this collector
        } finally {
            subs.remove(inbox)
            inbox.close()
        }
    }

    // FAIL-OPEN routing: returns a SUPERSET of the bits any consumer needs (coarse category
    // predicates), so a newly-added proto message type is over-delivered (consumers re-filter) rather
    // than silently unrouted. Under-routing loses events; over-routing only costs a trySend. A
    // contract test (see Testing) asserts every message type each consumer filter accepts sets that
    // group's bit, turning any omission into a CI failure rather than a production drop.
    //
    // Explicit per-message-field arms (one OR per accepted field — do NOT key on a single prefix):
    //   block* | blockDataview* | objectRelations* | objectDetails*            -> EDITOR
    //   objectDetails* | subscriptionAdd|Remove|Position|Counters              -> SUBSCRIPTION  (subscription* set it even with NO objectDetails)
    //   chat*                                                                  -> CHAT
    //   p2pStatusUpdate | spaceSyncStatusUpdate                                -> SYNC_P2P      (two prefix-DISJOINT types — two arms)
    //   processNew|Update|Done                                                 -> PROCESS
    //   accountShow | accountUpdate | accountConfigUpdate | accountLinkChallenge -> ACCOUNT      (accountShow REQUIRED — AuthMiddleware/login)
    //   fileSpaceUsage|fileLocalUsage|fileLimitReached|fileLimitUpdated        -> FILE
    //   membershipUpdate | membershipTiersUpdate                              -> MEMBERSHIP
    //   notificationUpdate | notificationSend                                 -> NOTIFICATIONS
    private fun groupMask(event: Event): Int { /* fold event.messages over the arms above */ }

    private companion object { private val GROUPS = EventGroup.values() }
}
```

No `shareIn`, no injected `CoroutineScope`, no internal broadcast SharedFlow.

## 4. Considered and rejected

- **Expose each group via `shareIn(scope, Eagerly, replay = 0)`** (the first-pass recommendation). **Rejected — critical defect:** the eager upstream collector drains the `UNLIMITED` channel into a `replay=0`/`extraBufferCapacity=0` SharedFlow and **silently discards** any event that arrives before that group's first downstream collector attaches. Lazily-attached stateful consumers (e.g. `ObjectWatcher` does the `openObject` RPC *then* attaches its `scan`; the `objectDetailsSet` emitted right after `openObject` would be drained-and-dropped) would lose deltas — strictly worse than today's suspending `emit`. (Both adversarial verifiers flagged this independently.)
- **One `Channel` per group + `receiveAsFlow()` (no registry).** Works for single-collector groups but **steals events** for the multi-collector groups (CHAT ×2, PROCESS ×3, MEMBERSHIP ×2) — each event would reach only one of the collectors. Rejected to keep a single uniform, footgun-free path (a group gaining a second collector later would silently break under this approach).
- **Typed-event bus (parse once for all types, route typed streams).** Correct long-term end-state but disproportionate migration (rewrite every `*MiddlewareChannel`, retire `EventProxy`, ~9 typed payloads + DI rewiring). Deferred.

## 5. Taxonomy → consumer mapping (verified, all in :middleware)

| Group | Consumer(s) | Collectors |
|---|---|---|
| EDITOR | `MiddlewareEventChannel` → `InterceptEvents` | 1 |
| SUBSCRIPTION | `MiddlewareSubscriptionEventChannel` — **pre-4525: cold per-`subscribe()` call → N concurrent collectors**; post-4525: one `shareIn` upstream | N (pre-4525) / 1 (post-4525) |
| CHAT | `ChatEventMiddlewareChannel` (`observe` + `subscribe`) | 2 classes |
| SYNC_P2P | `SyncAndP2PStatusEventsStoreImpl` (`SyncAndP2PChannelContainer.kt`) | 1 |
| PROCESS | `EventProcessDropFiles/Import/Migration` MiddlewareChannel | 3 |
| ACCOUNT | **`AuthMiddleware.observeAccounts` (filters `accountShow` — login!)** + `AccountStatusMiddlewareChannel` (filters `accountUpdate`) | 2 classes |
| FILE | `FileLimitsMiddlewareChannel` | 1 |
| MEMBERSHIP | `MembershipMiddlewareChannel` (`observe` + `observeTiers`) | 2 |
| NOTIFICATIONS | `NotificationsMiddlewareChannel` | 1 |

**"Collectors" counts consumer classes, not live collections.** `flow(group)` is cold and registers a fresh `UNLIMITED` inbox on **every** `.collect()`; `MiddlewareEventChannel.observeEvents` is a per-screen `FlowUseCase`, so EDITOR (and CHAT/MEMBERSHIP, and SUBSCRIPTION pre-4525) realistically have **N > 1 concurrent inboxes** at runtime. The per-collector registry handles any cardinality uniformly; size the memory risk (§6/§9) per-collection, not per-class.

## 6. Guarantees

- **Cross-type decoupling:** non-blocking `trySend` per subscriber → a stuck consumer grows only its own `UNLIMITED` channel; pump + inbox + all other consumers unaffected. Also fixes DROID-4526's failure mode (a slow consumer no longer parks the pump or inflates the shared inbox).
- **No new drops (scoped, not absolute):** each registered collector receives every event dispatched while it is registered. `trySend` to an open `UNLIMITED` channel never fails; on cancellation the channel is unregistered then closed (a late `trySend` to a closed channel is a benign `ChannelResult.closed` no-op, verified for coroutines 1.8.1). **Two pre-existing gaps are NOT closed by this design (parity with today, not regressions — do not claim otherwise):**
  - *Open→attach race:* a consumer that runs an RPC then attaches (e.g. `ObjectWatcher.watch` does `openObject` then collects) misses events dispatched before its collector registers. Today's shared flow has the same gap; it is mitigated by the `openObject` snapshot seeding the reducer, not by this design.
  - *SUBSCRIPTION drop is governed by DROID-4525, not the registry:* once 4525 lands, `MiddlewareSubscriptionEventChannel` collects `flow(SUBSCRIPTION)` once via `shareIn(WhileSubscribed(), replay=0)`. When its last downstream `subscribe()` goes away, that single SUBSCRIPTION inbox unregisters; events in the no-subscriber window are dropped (parity with today's `replay=0`). **Integration requirement:** to keep subscription deltas never-dropped while any subscription is active, 4525 must either keep an eagerly-/always-registered SUBSCRIPTION collector (e.g. apply this same per-collector buffered fan-out to its *parsed* output instead of `shareIn`), or explicitly accept the no-subscriber-window parity. The fan-out spec must not assert absolute never-drop for SUBSCRIPTION.
- **Ordering:** single in-order producer → single serial decode consumer → per-collector FIFO channel = within-type/within-subscription FIFO. Cross-type order dropped (contractually safe).
- **Memory:** per-collector `UNLIMITED`, **isolated** to the misbehaving collector. Worst-case retention is **O(N live collectors × whole `Event`)**, not one buffer per group — EDITOR/SUBSCRIPTION (heavy payloads, N concurrent screens) are the realistic OOM candidates, each contained to its own queue. A **per-collector depth gauge with a warn threshold is REQUIRED, not optional** (follow the existing `EventHandler` `AtomicInteger backlog` + `BACKLOG_WARN_THRESHOLD` precedent), and the warning must name the `EventGroup` so a leak is attributable. No safe hard cap exists (SUSPEND re-couples; DROP violates never-drop).

## 7. Interface changes & consumer migration

- New `EventGroup` enum (:middleware).
- `EventProxy.flow()` → `EventProxy.flow(group: EventGroup)`; `EventHandler.flow(group)` delegates to `channel.flow(group)`.
- `EventHandlerChannel`: `flow()` → `flow(group)`; rename the producer method `suspend fun emit(event)` → **`fun dispatch(event)` (drop `suspend` — enforce no-suspend via the type system)** (also removes the `FlowCollector.emit` shadow); drop the unused `trySend(event)` helper. Update the DROID-4526 pump's call site (`EventHandler.handle`: `channel.emit(event)` → `channel.dispatch(event)`).
- `EventHandlerChannelImpl`: implement the demux + registry (no constructor/DI change).
- `EventGroup` lives in `:middleware` next to `EventProxy` — verified all `flow(group)` call-sites are in `:middleware`; `:app` depends on `:middleware` so DI providers that merely inject `EventProxy` still compile. Grep `: EventProxy` and test doubles before merge — every fake/mock must adopt `flow(group)` (see Testing).
- **One line per consumer** (bodies unchanged — whole Event forwarded, so `event.contextId` / `event.messages.mapNotNull{}` keep working):
  - `MiddlewareEventChannel` → `flow(EventGroup.EDITOR)`
  - **`AuthMiddleware.observeAccounts` → `events.flow(EventGroup.ACCOUNT)`** (filters `accountShow`; **was missing from the taxonomy — omitting it silently breaks login**)
  - `MiddlewareSubscriptionEventChannel` → `flow(EventGroup.SUBSCRIPTION)`
  - `ChatEventMiddlewareChannel` (×2) → `flow(EventGroup.CHAT)`
  - `EventProcessDropFiles/Import/Migration` (×3) → `flow(EventGroup.PROCESS)`
  - `AccountStatusMiddlewareChannel` → `flow(EventGroup.ACCOUNT)`
  - `FileLimitsMiddlewareChannel` → `flow(EventGroup.FILE)`
  - `MembershipMiddlewareChannel` (×2) → `flow(EventGroup.MEMBERSHIP)`
  - `NotificationsMiddlewareChannel` → `flow(EventGroup.NOTIFICATIONS)`
  - `SyncAndP2PStatusEventsStoreImpl` → `channel.flow(EventGroup.SYNC_P2P)`
- Test doubles/fakes implementing `EventProxy` must adopt `flow(group)`; `EventHandlerChannelImplTest` updates its `flow()` calls.

## 8. Testing

- **Cross-type decoupling:** a blocked EDITOR collector (never drains) does not stall a SYNC_P2P collector, does not park the pump, and does not grow the inbox — only EDITOR's own channel grows.
- **Fan-out:** an `objectDetailsSet` Event reaches both an EDITOR collector and a SUBSCRIPTION collector.
- **Multi-collector, no theft:** with two CHAT collectors (and three PROCESS collectors), every collector receives every relevant event.
- **Within-group FIFO** under a multi-threaded stress feed.
- **Never-drop from registration:** events emitted after a collector attaches are all delivered in order, even under a slow collector.
- **Lifecycle:** cancelling a collector unregisters + closes its channel; a subsequent event is a no-op for it and still delivered to others.
- **Fail-open routing contract test:** for each consumer, assert that every `Event.Message` type its filter accepts causes `groupMask` to set that consumer's group bit. This converts a forgotten `groupMask` update (when a new proto field is added) into a CI failure rather than a silent production drop.

### Migration hazards to handle (not optional)
- **Mockito "empty flow → vacuous assert" trap:** existing tests stub `on { flow() } doReturn flowOf(event)` (e.g. `MiddlewareEventChannelTest`). After widening, these must stub `flow(<GROUP>)`; a strict mock of `flow(group)` that returns nothing makes `runBlocking { …collect { assert } }` complete **without running the assertion** — a green-but-vacuous test. Update every stub and add at least one assertion that fails if no item is emitted.
- **Message-less test events route nowhere:** `EventHandlerTest` (DROID-4526) feeds bare `Event(contextId=…)` with no messages and collects `flow()`. Under the demux, `groupMask` returns 0 → routed to no group → `flow(group).test{}` would hang on `awaitItem()`. These tests must feed real grouped messages (and collect the matching group); `EventHandlerChannelImplTest` emits `p2pStatusUpdate`/`spaceSyncStatusUpdate` → must collect `flow(SYNC_P2P)`.
- Enumerate updated test doubles: `MiddlewareEventChannelTest`, `EventHandlerTest`, `EventHandlerChannelImplTest`, `SyncAndP2PStatusEventsStoreImplTest`, and any fake `EventProxy` (confirm `EventHandler` is the only non-test impl).

## 9. Risks

- **Unbounded per-collector memory** if a consumer truly never drains — isolated to that collector + observable, but no safe hard cap (SUSPEND would re-couple; DROP violates never-drop).
- **`groupMask` is a parallel taxonomy → keep it FAIL-OPEN.** Today's single firehose is fail-open: a new proto message type automatically reaches every consumer's filter. A hand-maintained exact `groupMask` would be fail-**closed** — a new field not added to it is silently unrouted (reaches no consumer), a real maintainability regression. Mitigate by (a) routing on **coarse category supersets** (e.g. any `block*`/`blockDataview*`/`objectDetails*`/`objectRelations*` → EDITOR) so omissions over-deliver (a cheap extra `trySend`; consumers re-filter) rather than drop, and (b) the fail-open contract test above. Under-routing must be impossible by construction; over-routing is harmless.
- **Cross-group ordering dropped by contract:** a *future* consumer that correlates two groups would silently break. Guard with a comment on `EventGroup` + review note.
- **`EventProxy` signature change** ripples to every fake/mock — enumerate and update test doubles.

## 10. Sequencing

**DROID-4525 is a hard prerequisite, not just "either order."** This fix's SUBSCRIPTION memory/never-drop story assumes the single `shareIn` collector 4525 introduces; on the pre-4525 branch `MiddlewareSubscriptionEventChannel.subscribe()` is cold per-call, so SUBSCRIPTION has **N concurrent `UNLIMITED` whole-`Event` collectors** (O(N) retention, not one buffer). Land 4525 (and 4526) first, then this. 4526's pump is untouched; 4525's channel just sources `flow(SUBSCRIPTION)`.

**SUBSCRIPTION + 4525 is a hard decision, not "either … or" (§6):** pick one — (a) 4525 keeps an eager/always-registered SUBSCRIPTION collector (apply this same per-collector fan-out to 4525's *parsed* output rather than `shareIn(WhileSubscribed, replay=0)`), or (b) explicitly accept the no-subscriber-window drop and document the user-visible consequence (stale subscription data until the next full `Set`). Do not ship the spec with this unresolved.

## 11. Round-2 review: must-honor implementation details

- **`groupMask` contract test must enumerate accepted types per consumer** — explicitly including `accountShow` (ACCOUNT), the SUBSCRIPTION-only `subscriptionAdd/Remove/Position/Counters` (which carry no `objectDetails`), and SYNC_P2P's two prefix-disjoint types `p2pStatusUpdate` + `spaceSyncStatusUpdate`. These are the concrete ways a coarse predicate silently under-routes.
- **Tests must register before the first dispatch.** `flow(group)` runs `subs.add(inbox)` only once the collector coroutine is scheduled; a test that feeds `dispatch`/`onRawEvent` immediately after `.test{}` can race registration and drop the first event (intermittent CI failures; also a real lazy-attach production class broader than the `ObjectWatcher` case). Provide a registration latch / pre-registered hot inbox helper.
- **`MiddlewareEventChannelTest` has ~6 `on { flow() } doReturn …` stub sites** (not one) — all must become `flow(group)` **and** gain a fail-if-empty assertion (a strict mock returning empty makes `collect{ assert }` pass vacuously).
- **`EventHandlerTest` (pump FIFO/burst/poison)** — use a single shared group-wrapping helper so events carry a group, without weakening the pump's ordering assertions.
- **Teardown invariant is hard:** `flow(group)`'s `finally` must `remove` **then** `close` (never close-first); buffered-but-undrained events of a cancelled collector are **intentionally discarded** (do not add `onUndeliveredElement`) — that matches the while-registered contract.
- **`SyncAndP2P` `start()`/`stop()` overlap (pre-existing, slightly widened):** cancelling the old job then launching a new collection is non-atomic; the old `UNLIMITED` inbox can now hold a longer backlog that replays `_syncStatus/_p2pStatus.update{}` concurrently with the new inbox. Self-heals; optionally guard `start()` against overlapping collections.
