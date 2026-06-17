# Middleware Subscription Event Fan-out Optimization

- **Date:** 2026-06-16
- **Status:** Approved (design) — pending implementation plan
- **Scope:** `MiddlewareSubscriptionEventChannel` only
- **Type:** Performance refactor (no behavior change)

## 1. Background & problem

A 60s ART CPU profile of a cold start (debuggable `io.anytype.app.dev`, captured via ART
method sampling since `perf_event_open` is blocked on the device) showed that, **after first
frame**, the `Dispatchers.Default` coroutine pool dominates activity for the entire window —
not with business logic, but with coroutine/flow plumbing: `SharedFlowImpl.tryTakeValue`,
`Unsafe.park`/`unpark`, work-queue polling, and atomic-field-updater spinning. In a 57s
post-first-frame window the app performed ~12,279 `SharedFlow` collect-resumptions and ~5,500
worker unparks **while idle**. The top application frame was
`MiddlewareSubscriptionEventChannel$subscribe$…mapNotNull`.

### Root cause

Every middleware event flows through one global `MutableSharedFlow<Event>`
(`EventHandlerChannelImpl`, `replay = 0`, `extraBufferCapacity = 1`) exposed via
`EventProxy.flow()`. `MiddlewareSubscriptionEventChannel.subscribe(subscriptions)` returns a
**cold** flow that, for each caller, independently collects that SharedFlow and re-parses every
event payload.

Many subscription containers each call `subscribe()` concurrently —
`StorelessSubscriptionContainer`, `CrossSpaceSubscriptionContainer`,
`ObjectSearchSubscriptionContainer`, `ObjectTypesSubscriptionContainer`,
`RelationsSubscriptionContainer`, `RelationOptionsSubscriptionContainer`,
`DataViewSubscriptionContainer`, etc. With **N** active subscriptions, each middleware event
payload is parsed **N times**, and each `subscribe()` flow is an independent SharedFlow
collector, so the per-event coroutine-dispatch cost is multiplied by N.

Approximate cost today:

```
cost ≈ (coroutine-dispatch overhead × N collectors)
     + (parse cost per message × messages × N collectors)
```

Additional per-message waste inside the parse:
- `subscriptions.any { it in event.subIds || "$it$DEPENDENT_SUBSCRIPTION_POST_FIX" in event.subIds }`
  allocates a concatenated `"<subId>/dep"` string **per subscription per message**.
- A `Timber.d(...)` call on every matched message.

## 2. Goals / non-goals

### Goals
- Parse each event **once** (subscriber-agnostic) and share the parsed result across all
  `subscribe()` callers.
- Replace the per-message string-allocating membership test with a precomputed `Set` lookup,
  and remove `Timber.d` from the hot path.
- Reduce the global SharedFlow to a **single** upstream collector for subscription events.

### Non-goals
- No change to the public contract:
  `SubscriptionEventRemoteChannel.subscribe(List<Id>): Flow<List<SubscriptionEvent>>`.
- No change to which events any subscriber receives (delivery semantics identical).
- Out of scope: the other middleware channels (`NotificationsMiddlewareChannel`,
  `MembershipMiddlewareChannel`, `AccountStatusMiddlewareChannel`, `FileLimitsMiddlewareChannel`,
  `SyncAndP2PChannelContainer`, `ChatEventMiddlewareChannel`, `EventProcessMiddlewareChannel`,
  `MiddlewareEventChannel`). They were minor in the profile and can adopt the same pattern later
  if measured to matter.
- Out of scope: the per-event `scope.launch { handle(bytes) }` / per-event
  `withContext(Dispatchers.IO)` decode in `EventHandler` (tracked separately).

## 3. Design

A subscriber-agnostic parse stage, shared once, followed by a cheap per-subscriber filter.

```
events.flow()                                   // global SharedFlow<Event> (unchanged)
  └─ map: parse each message ONCE  ────────────▶ Flow<List<ParsedSubEvent>>
       └─ shareIn(appScope, WhileSubscribed(0), replay = 0)   // ONE upstream collector
            ├─ subscribe(subsA): cheap subId-set filter ─▶ List<SubscriptionEvent>
            ├─ subscribe(subsB): cheap subId-set filter ─▶ List<SubscriptionEvent>
            └─ subscribe(subsC): cheap subId-set filter ─▶ List<SubscriptionEvent>
```

New cost:

```
cost ≈ (coroutine-dispatch overhead × 1 upstream collector)
     + (parse cost per message × messages)            // once, not × N
     + (cheap set lookup × messages × N collectors)   // no allocation
```

### 3.1 Parsed wrapper

`Position` and `Counter` `SubscriptionEvent`s do **not** carry their `subId` in the model
(`core-models/.../SubscriptionEvent.kt`) — today the code only uses `event.subId` to *decide*
whether to emit them, then discards it. Parsing once therefore requires keeping the routing key
as separate metadata:

```kotlin
private data class ParsedSubEvent(
    val event: SubscriptionEvent,
    val keys: List<Id>,   // routing keys carried by the raw event
    val dep: Boolean      // true = honors the "/dep" dependent-subscription suffix
)
```

- `keys` = `event.subIds` for Amend/Set/Unset; `listOf(event.subId)` for Remove/Add/Position/Counter.
- `dep` = `true` for Amend/Set/Unset/Remove/Add; `false` for Position/Counter.

### 3.2 Shared parse stream (inside `MiddlewareSubscriptionEventChannel`)

```kotlin
private val parsed: Flow<List<ParsedSubEvent>> =
    events.flow()
        .map { payload -> payload.messages.mapNotNull { parseMessage(it) } }
        .filter { it.isNotEmpty() }
        .shareIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0), replay = 0)
```

`parseMessage(message: Event.Message): ParsedSubEvent?` ports the existing `when` block, but
**subscriber-agnostic**: it builds a `SubscriptionEvent` for every supported message type
(unknown → `null`, mirroring today's `else -> null`), tagging each with `keys` and `dep`. The
`Set` case keeps its existing `data != null` guard.

If backpressure tuning is needed to keep a slow subscriber from stalling the single parse loop,
insert `.buffer(<small capacity>)` immediately before `shareIn`.

### 3.3 Per-subscriber filter (fix #2)

```kotlin
override fun subscribe(subscriptions: List<Id>): Flow<List<SubscriptionEvent>> {
    val exact: Set<Id> = subscriptions.toHashSet()
    val dep: Set<Id> = subscriptions.flatMapTo(HashSet(subscriptions.size * 2)) {
        listOf(it, it + DEPENDENT_SUBSCRIPTION_POST_FIX)
    }
    return parsed
        .map { events ->
            events.mapNotNull { p ->
                val match = if (p.dep) p.keys.any { it in dep } else p.keys.any { it in exact }
                if (match) p.event else null
            }
        }
        .filter { it.isNotEmpty() }
}
```

The sets are computed **once per `subscribe()` call**, not per event/message. No string
concatenation in the hot path; no `Timber.d`.

## 4. Semantics preservation (correctness crux)

The per-type matching rules must remain **byte-for-byte identical** to the current
implementation:

| Event (`Event.Message` field) | Routing key(s) | Current match rule | `dep` |
|---|---|---|---|
| `objectDetailsAmend` → `Amend` | `subIds` (list) | `sub ∈ subIds` **or** `sub/dep ∈ subIds` | `true` |
| `objectDetailsUnset` → `Unset` | `subIds` (list) | `sub ∈ subIds` **or** `sub/dep ∈ subIds` | `true` |
| `objectDetailsSet` → `Set` | `subIds` (list) | `sub ∈ subIds` **or** `sub/dep ∈ subIds` (and `data != null`) | `true` |
| `subscriptionRemove` → `Remove` | `subId` (single) | `sub == subId` **or** `sub/dep == subId` | `true` |
| `subscriptionAdd` → `Add` | `subId` (single) | `sub == subId` **or** `sub/dep == subId` | `true` |
| `subscriptionPosition` → `Position` | `subId` (single) | `sub == subId` (no `/dep`) | `false` |
| `subscriptionCounters` → `Counter` | `subId` (single) | `sub == subId` (no `/dep`) | `false` |

Equivalence: the old `subscriptions.any { s -> s in keys || (s + "/dep") in keys }` (dep case)
equals `keys.any { it in dep }` where `dep = { s, s+"/dep" : s ∈ subscriptions }`. The exact case
(`Position`/`Counter`) equals `keys.any { it in exact }` for single-element `keys`.

`DEPENDENT_SUBSCRIPTION_POST_FIX` (`"/dep"`) is retained as the channel companion constant.

## 5. Component changes

- **`middleware/.../interactor/MiddlewareSubscriptionEventChannel.kt`** — rewrite:
  add `ParsedSubEvent`, `parseMessage(...)`, the `parsed` shared stream, and the new
  `subscribe()` body. Constructor gains a `CoroutineScope` parameter.
- **`app/.../di/main/EventModule.kt`** (`provideSubscriptionEventRemoteChannel`, ~line 106) —
  inject `@Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) CoroutineScope` and pass it to the
  channel constructor. This is an established pattern in the same module: `provideEventHandler`
  (line 129) and `provideSyncAndP2PStatusEventsSubscription` (line 143) already inject that exact
  scope. The constant is defined at `ConfigModule.kt:93` and provided at the app/main component
  level (also injected by `DataModule`, `SubscriptionsModule`, `NotificationsModule`).
- No changes to `SubscriptionDataChannel`, `SubscriptionEventRemoteChannel`, or any domain
  subscription container.

## 6. Lifecycle / backpressure / error handling

- **Sharing policy:** `SharingStarted.WhileSubscribed(stopTimeoutMillis = 0)`, `replay = 0`.
  Property preserved: the parse runs while ≥1 subscriber is collecting (identical delivery window
  to today, where an event reaches any currently-active collector), and parsing stops when no
  subscriber is active (strictly better — no wasted parsing). `replay = 0` matches today's
  drop-when-no-collector behavior.
- **Backpressure:** the single parse collector backpressures into `events.flow()` exactly as one
  collector does today. Optional `.buffer(...)` before `shareIn` decouples the parse loop from a
  slow individual subscriber.
- **Errors:** `parseMessage` stays total and defensive (unknown/empty message → `null`). No new
  exceptions are introduced into the stream; the upstream `EventProxy` error behavior is unchanged.

## 7. Testing

Unit tests (coroutines-test) against a fake `EventProxy` emitting crafted `Event` payloads:
- Each event type routes to the correct subscriber, including `/dep` matching for
  Amend/Set/Unset/Remove/Add and **exact** matching for Position/Counter.
- `Set` with `data == null` is dropped.
- Two concurrent subscribers with different `subscriptions` each receive only their own events
  (cross-subscriber isolation).
- Payloads with no matching messages do not emit (empty lists filtered).
- Multi-message payloads emit a single `List<SubscriptionEvent>` per payload (batching preserved).

Preserve and extend any existing `MiddlewareSubscriptionEventChannel` tests.

## 8. Acceptance criteria

- All existing and new unit tests pass; per-type behavior matches the table in §4 exactly.
- A re-run of the ART cold-start profile shows a material drop in subscription-event parse and
  collector-churn samples on the `Dispatchers.Default` workers versus the baseline capture.

## 9. Risks & mitigations

- **Behavioral drift in matching rules** → §4 table + dedicated per-type unit tests; algebraic
  equivalence stated explicitly.
- **Sharing-lifecycle subtlety** (events between unsubscribe/resubscribe) → `WhileSubscribed`
  keeps upstream active while any subscriber is collecting, matching current delivery semantics;
  `replay = 0` matches current drop semantics.
- **Scope injection** → reuse the existing `ConfigModule.DEFAULT_APP_COROUTINE_SCOPE` (already
  injected by `provideEventHandler` in the same module); no new scope created.

## 10. Future work (not in this spec)

- Apply the parse-once/share pattern to other high-traffic channels if profiling later shows them
  significant (the "Broad" option).
- Reduce per-event coroutine churn in `EventHandler` (avoid `scope.launch` per event;
  single consumer loop / actor; move protobuf decode off the per-event `withContext(Dispatchers.IO)`).
