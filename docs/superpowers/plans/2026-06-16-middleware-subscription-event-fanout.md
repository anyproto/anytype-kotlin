# Middleware Subscription Event Fan-out Optimization — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `MiddlewareSubscriptionEventChannel` parse each middleware event once and share it across all `subscribe()` callers with a cheap per-subscriber `subId`-set filter, eliminating O(events × subscribers) re-parsing and collector churn.

**Architecture:** Replace the current per-subscriber cold flow (each re-collects and re-parses the global `EventProxy` SharedFlow) with: (1) a single subscriber-agnostic parse stage that maps each event payload to `List<ParsedSubEvent>` and is shared via `shareIn`; (2) a `subscribe()` that precomputes `subId` sets once and filters the shared parsed stream. Public contract and per-event delivery semantics are unchanged.

**Tech Stack:** Kotlin, kotlinx.coroutines `Flow`/`shareIn`, Wire-generated `anytype.Event` protos, Dagger DI, JUnit4 + kotlinx-coroutines-test (1.8.0) + Turbine (1.1.0) + Mockito-Kotlin.

**Reference spec:** `docs/superpowers/specs/2026-06-16-middleware-subscription-event-fanout-design.md`
**Linear issue:** DROID-4525

---

## File Structure

- **Modify:** `middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannel.kt`
  Full rewrite. Adds a constructor `CoroutineScope`, a private `ParsedSubEvent` data class, a private `parseMessage()` (subscriber-agnostic), the shared `parsed` stream, and a cheap-filter `subscribe()`. Single responsibility: turn the raw middleware event stream into per-subscription `SubscriptionEvent` lists.
- **Modify:** `app/src/main/java/com/anytypeio/anytype/di/main/EventModule.kt` (`provideSubscriptionEventRemoteChannel`, ~line 106)
  Inject the existing `@Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) CoroutineScope` and pass it to the channel.
- **Create (test):** `middleware/src/test/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannelTest.kt`
  Verifies per-type routing (incl. `/dep` for Set, exact-only for Position/Counter), no-match suppression, and cross-subscriber isolation.

### Environment notes
- Use **JDK 17** for Gradle (project targets `jvmToolchain(17)`):
  `export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
- The `:middleware` module declares the `anytype-heart-android` (Go JNI) dependency. These are JVM unit tests using a mocked `EventProxy` (no JNI at test time), but if the artifact is missing locally and blocks the build, run the test task on CI instead. Sibling tests (`MiddlewareEventChannelTest`) run in the same module, so the configuration is supported.

---

## Task 1: Rewrite `MiddlewareSubscriptionEventChannel` (parse-once + shared + cheap filter)

**Files:**
- Test (create): `middleware/src/test/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannelTest.kt`
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannel.kt`

- [ ] **Step 1: Write the failing test**

Create `middleware/src/test/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannelTest.kt`:

```kotlin
package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class MiddlewareSubscriptionEventChannelTest {

    @Mock
    lateinit var proxy: EventProxy

    private lateinit var upstream: MutableSharedFlow<Event>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        upstream = MutableSharedFlow(extraBufferCapacity = 16)
        proxy.stub { on { flow() } doReturn upstream }
    }

    private fun event(vararg messages: Event.Message) = Event(messages = messages.toList())

    @Test
    fun `Set matches by subId and maps to SubscriptionEvent_Set`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            objectDetailsSet = Event.Object.Details.Set(
                id = "obj-1",
                details = mapOf("name" to "Doc"),
                subIds = listOf("sub-1")
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(
                    SubscriptionEvent.Set(
                        target = "obj-1",
                        data = mapOf("name" to "Doc"),
                        subscriptions = listOf("sub-1")
                    )
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Set matches dependent subscription via dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            objectDetailsSet = Event.Object.Details.Set(
                id = "obj-1",
                details = mapOf("k" to "v"),
                subIds = listOf("sub-1/dep")
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(
                    SubscriptionEvent.Set(
                        target = "obj-1",
                        data = mapOf("k" to "v"),
                        subscriptions = listOf("sub-1/dep")
                    )
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Position matches exact subId`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-1",
                afterId = "obj-0",
                subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "obj-1", afterId = "obj-0")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Position does NOT honor dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val depOnly = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-dep", afterId = "", subId = "sub-1/dep"
            )
        )
        val exact = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-exact", afterId = "", subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(depOnly, exact))
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "obj-exact", afterId = "")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Counter is parsed and matched by exact subId`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionCounters = Event.Object.Subscription.Counters(
                total = 10, nextCount = 3, prevCount = 2, subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Counter(SearchResult.Counter(total = 10, prev = 2, next = 3))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Remove matches dependent subscription via dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionRemove = Event.Object.Subscription.Remove(
                id = "obj-1",
                subId = "sub-1/dep"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Remove(target = "obj-1", subscription = "sub-1/dep")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `payload with no matching subscription does not emit`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val other = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(id = "x", afterId = "", subId = "sub-OTHER")
        )
        val mine = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(id = "y", afterId = "", subId = "sub-1")
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(other)) // no match -> suppressed
            upstream.emit(event(mine))  // match -> emitted
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "y", afterId = "")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `two concurrent subscribers each receive only their own events`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val payload = event(
            Event.Message(
                subscriptionPosition = Event.Object.Subscription.Position(id = "a", afterId = "", subId = "sub-A")
            ),
            Event.Message(
                subscriptionPosition = Event.Object.Subscription.Position(id = "b", afterId = "", subId = "sub-B")
            )
        )
        turbineScope {
            val a = channel.subscribe(listOf("sub-A")).testIn(backgroundScope)
            val b = channel.subscribe(listOf("sub-B")).testIn(backgroundScope)
            upstream.emit(payload)
            assertEquals(listOf(SubscriptionEvent.Position(target = "a", afterId = "")), a.awaitItem())
            assertEquals(listOf(SubscriptionEvent.Position(target = "b", afterId = "")), b.awaitItem())
            a.cancel()
            b.cancel()
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails (compilation)**

Run:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew :middleware:testDebugUnitTest --tests "com.anytypeio.anytype.middleware.interactor.MiddlewareSubscriptionEventChannelTest"
```
Expected: **FAIL** — compilation error, because the current `MiddlewareSubscriptionEventChannel` constructor has no `scope` parameter (`No value passed for parameter 'scope'` / `Cannot find a parameter with this name: scope`).

- [ ] **Step 3: Rewrite the implementation**

Replace the entire contents of `middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannel.kt` with:

```kotlin
package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.data.auth.event.SubscriptionEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.parse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class MiddlewareSubscriptionEventChannel(
    private val events: EventProxy,
    scope: CoroutineScope
) : SubscriptionEventRemoteChannel {

    /**
     * Each event payload is parsed once, subscriber-agnostically, into [ParsedSubEvent]s and
     * shared across every [subscribe] caller. This replaces the previous design where each
     * subscriber independently collected [EventProxy.flow] and re-parsed every event.
     */
    private val parsed: Flow<List<ParsedSubEvent>> = events
        .flow()
        .map { payload -> payload.messages.mapNotNull { parseMessage(it) } }
        .filter { it.isNotEmpty() }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            replay = 0
        )

    override fun subscribe(subscriptions: List<Id>): Flow<List<SubscriptionEvent>> {
        // Precomputed once per subscribe() call (not per event/message).
        val exact = HashSet(subscriptions)
        val dep = HashSet<Id>(subscriptions.size * 2)
        for (s in subscriptions) {
            dep.add(s)
            dep.add(s + DEPENDENT_SUBSCRIPTION_POST_FIX)
        }
        return parsed
            .map { parsedEvents ->
                parsedEvents.mapNotNull { parsed ->
                    val matches = if (parsed.dep) {
                        parsed.keys.any { it in dep }
                    } else {
                        parsed.keys.any { it in exact }
                    }
                    if (matches) parsed.event else null
                }
            }
            .filter { it.isNotEmpty() }
    }

    private fun parseMessage(message: Event.Message): ParsedSubEvent? = when {
        message.objectDetailsAmend != null -> {
            val event = message.objectDetailsAmend
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Amend(
                    target = event.id,
                    diff = event.details.associate { it.key to it.value_ },
                    subscriptions = event.subIds
                ),
                keys = event.subIds,
                dep = true
            )
        }
        message.objectDetailsUnset != null -> {
            val event = message.objectDetailsUnset
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Unset(
                    target = event.id,
                    keys = event.keys,
                    subscriptions = event.subIds
                ),
                keys = event.subIds,
                dep = true
            )
        }
        message.objectDetailsSet != null -> {
            val event = message.objectDetailsSet
            checkNotNull(event)
            val data = event.details
            if (data != null) {
                ParsedSubEvent(
                    event = SubscriptionEvent.Set(
                        target = event.id,
                        data = data,
                        subscriptions = event.subIds
                    ),
                    keys = event.subIds,
                    dep = true
                )
            } else {
                null
            }
        }
        message.subscriptionRemove != null -> {
            val event = message.subscriptionRemove
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Remove(
                    target = event.id,
                    subscription = event.subId
                ),
                keys = listOf(event.subId),
                dep = true
            )
        }
        message.subscriptionAdd != null -> {
            val event = message.subscriptionAdd
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Add(
                    target = event.id,
                    afterId = event.afterId,
                    subscription = event.subId
                ),
                keys = listOf(event.subId),
                dep = true
            )
        }
        message.subscriptionPosition != null -> {
            val event = message.subscriptionPosition
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Position(
                    target = event.id,
                    afterId = event.afterId
                ),
                keys = listOf(event.subId),
                dep = false
            )
        }
        message.subscriptionCounters != null -> {
            val event = message.subscriptionCounters
            checkNotNull(event)
            ParsedSubEvent(
                event = SubscriptionEvent.Counter(
                    counter = event.parse()
                ),
                keys = listOf(event.subId),
                dep = false
            )
        }
        else -> null
    }

    private data class ParsedSubEvent(
        val event: SubscriptionEvent,
        val keys: List<Id>,
        val dep: Boolean
    )

    companion object {
        const val DEPENDENT_SUBSCRIPTION_POST_FIX = "/dep"
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew :middleware:testDebugUnitTest --tests "com.anytypeio.anytype.middleware.interactor.MiddlewareSubscriptionEventChannelTest"
```
Expected: **PASS** — all 8 tests green.

- [ ] **Step 5: Commit**

```bash
git add middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannel.kt \
        middleware/src/test/java/com/anytypeio/anytype/middleware/interactor/MiddlewareSubscriptionEventChannelTest.kt
git commit -m "DROID-4525 Parse subscription events once and share across subscribers"
```

---

## Task 2: Inject the app coroutine scope in DI

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/di/main/EventModule.kt` (`provideSubscriptionEventRemoteChannel`, ~line 106)

- [ ] **Step 1: Update the provider**

In `EventModule.kt`, change `provideSubscriptionEventRemoteChannel` from:

```kotlin
    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionEventRemoteChannel(
        proxy: EventProxy
    ): SubscriptionEventRemoteChannel = MiddlewareSubscriptionEventChannel(events = proxy)
```

to:

```kotlin
    @JvmStatic
    @Provides
    @Singleton
    fun provideSubscriptionEventRemoteChannel(
        proxy: EventProxy,
        @Named(ConfigModule.DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope
    ): SubscriptionEventRemoteChannel = MiddlewareSubscriptionEventChannel(
        events = proxy,
        scope = scope
    )
```

Notes:
- `javax.inject.Named`, `kotlinx.coroutines.CoroutineScope`, and `ConfigModule` are already imported and used in this file (`provideEventHandler` at ~line 127 injects the same `@Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope`). If `DEFAULT_APP_COROUTINE_SCOPE` is referenced unqualified elsewhere in this file, match that form instead of `ConfigModule.DEFAULT_APP_COROUTINE_SCOPE`.

- [ ] **Step 2: Compile to verify DI wiring**

Run:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew :app:compileDebugKotlin
```
Expected: **BUILD SUCCESSFUL** — Dagger generates the updated factory for `provideSubscriptionEventRemoteChannel` (the `@Named(DEFAULT_APP_COROUTINE_SCOPE)` `CoroutineScope` binding already exists and is consumed by sibling providers, so no new binding is required).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/di/main/EventModule.kt
git commit -m "DROID-4525 Provide app coroutine scope to MiddlewareSubscriptionEventChannel"
```

---

## Task 3: Verify the performance improvement (manual)

**Files:** none (verification only).

- [ ] **Step 1: Run the full middleware test suite**

Run:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew :middleware:testDebugUnitTest
```
Expected: **PASS** — no regressions in sibling channel/mapper tests.

- [ ] **Step 2: Re-profile cold start and compare against baseline**

With a device connected and `io.anytype.app.dev` installed, capture a 60s ART sampling profile of a cold start (same method as the baseline capture):
```bash
adb shell am force-stop io.anytype.app.dev
adb shell am start -S -W -n io.anytype.app.dev/com.anytypeio.anytype.ui.main.MainActivity \
  --start-profiler /data/local/tmp/anytype-after.trace --streaming --sampling 1000
# wait ~60s, then:
adb shell am profile stop io.anytype.app.dev
adb pull /data/local/tmp/anytype-after.trace /Users/roman/anytype/startup-profile/anytype-after.trace
```
Open in Android Studio Profiler or https://ui.perfetto.dev, or query with the existing
`trace_processor` in `/Users/roman/anytype/startup-profile/`.

Expected: materially fewer leaf samples in `MiddlewareSubscriptionEventChannel$subscribe$…` and in
`Dispatchers.Default` collector-churn frames (`SharedFlowImpl.tryTakeValue`, `Unsafe.park`/`unpark`)
versus the baseline capture. Record the before/after numbers in DROID-4525.

---

## Self-Review

**1. Spec coverage**
- Parse-once (subscriber-agnostic) → Task 1, `parseMessage` + `parsed` shared stream. ✓
- `shareIn` single upstream collector, `WhileSubscribed`, `replay = 0` → Task 1. ✓
- Cheap per-subscriber `subId`-set filter, no per-message string concat, no `Timber.d` (#2) → Task 1, `subscribe`. ✓
- `/dep` semantics table (Amend/Set/Unset/Remove/Add honor `/dep`; Position/Counter exact) → Task 1 impl (`dep` flag) + tests (`Set … dep suffix`, `Remove … dep suffix`, `Position does NOT honor dep suffix`, `Counter … exact`). ✓
- DI scope injection via existing `DEFAULT_APP_COROUTINE_SCOPE` → Task 2. ✓
- Acceptance: tests pass + re-profile shows reduction → Task 3. ✓

**2. Placeholder scan:** No TBD/TODO; every code/test step is complete and copy-pasteable. ✓

**3. Type consistency:**
- `MiddlewareSubscriptionEventChannel(events: EventProxy, scope: CoroutineScope)` — same signature in test (Task 1 Step 1), impl (Step 3), and DI (Task 2). ✓
- `ParsedSubEvent(event: SubscriptionEvent, keys: List<Id>, dep: Boolean)` — defined and consumed consistently in Task 1. ✓
- `SubscriptionEvent.{Amend,Unset,Set,Remove,Add,Position,Counter}` constructors match `core-models/SubscriptionEvent.kt`; `SearchResult.Counter(total, prev, next)` matches `Counters.parse()`. ✓
- `DEPENDENT_SUBSCRIPTION_POST_FIX = "/dep"` retained as the companion constant. ✓

---

## Notes for the implementer

- This is a **behavior-preserving** refactor. The per-type matching rules must stay identical — the tests in Task 1 are the guardrail; do not weaken them.
- `parseMessage` deliberately keeps the exact construction expressions from the previous implementation (e.g., `event.details.associate { it.key to it.value_ }`, `event.parse()`); only *when* parsing happens and *how* filtering happens changed.
- Do not commit the captured `.trace` files or the `startup-profile/` working dir.
