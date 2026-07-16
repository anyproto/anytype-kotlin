# Report network changes (`networkId`) and correct the default-network callback contract

- **Date:** 2026-07-16
- **Status:** Proposed (pending approval)
- **Scope:** `NetworkConnectionStatusImpl` + `networkId` plumbing (`commands.proto` → `BlockRepository` → `Middleware`)
- **Type:** Contract compliance + latent-bug fix (sync recovery after network switches)
- **Ticket:** DROID-4548 (Android side of GO-7379 / anytype-heart PR #3211)
- **Heart contract:** `docs/mobile-network-integration.md` @ `go-7379-snappy-sync-connection-recovery`

## 1. Background & problem

Heart runs a connectivity-recovery pipeline (connection-pool flush → responsible-peer rebuild → immediate head-sync of all spaces) driven by `Rpc.Device.NetworkState.Set`. Correct signals make sync recover in ~1–2s after a network switch instead of riding a 30–40s transport timeout.

The ticket asks for an audit against the contract, not just the new field. The audit was done; findings drive this design.

### Audit results (verified against the current tree)

| Contract item | Verdict |
|---|---|
| `NetworkState.Set` from a default-network callback on every capabilities change | **Partly.** `registerDefaultNetworkCallback` + `onCapabilitiesChanged` is correct, and there is no client-side debounce (contract-compliant). But `onUnavailable` is missing, and `onAvailable`/`onLost` discard the `Network` they are handed and re-query `activeNetwork`. |
| `NOT_CONNECTED` reserved for true no-network; **not** derived from `VALIDATED` | **Compliant by accident.** `mapNetworkType` (`NetworkConnectionStatus.kt:101-107`) never checks `VALIDATED`. But its `else -> NOT_CONNECTED` fallback reports **Ethernet, VPN and Bluetooth-tethered networks as offline** — a real bug the ticket does not mention. |
| `SetDeviceState(FOREGROUND/BACKGROUND)` sent promptly | **Compliant. No change.** `AppStateService` is a `DefaultLifecycleObserver` on `ProcessLifecycleOwner`, registered in `AndroidApplication.onCreate` with no gates, conditions, or debounce. |
| Interface getter enumerates cellular | **Compliant. No change.** `DefaultInterfaceProvider` calls `NetworkInterface.getNetworkInterfaces()` with **zero filtering**, so `rmnet`/`ccmni` are enumerated alongside `wlan0`, re-read live on each Go pull (~5s). |
| `networkId` (field 2) | **Missing everywhere.** Not in this repo's proto; not in heart `main`. Exists only on the unmerged `go-7379` branch. |

Additionally: **zero tests** cover any of these three paths.

### The `VALIDATED` reversal

The ticket originally said "map missing `NET_CAPABILITY_VALIDATED` to `NOT_CONNECTED`". That instruction is **superseded**. Heart commit `8a2fa9d91` (2026-07-16 11:28) removed it from the guide:

> reporting NOT_CONNECTED must not degrade local-only P2P sync (the same `manageResponsiblePeers` loop that re-dials nodes also refreshes LAN peers, so the 2min offline cadence delayed mDNS peer pickup) … validation gates internet, not LAN sync.

The Linear description now carries a "Revised 2026-07-16" banner saying the same. Android's own documentation independently confirms the reasoning: *"A carrier's mobile network typically has the `INTERNET` capability, while a local P2P Wi-Fi network typically doesn't."* `VALIDATED` must not feed this RPC. It remains legitimate elsewhere (connectivity UI, upload policies) — see §8.

## 2. Goals / non-goals

### Goals
- Send `networkId` so heart can detect same-type path changes (Wi-Fi→Wi-Fi, cellular PDP re-attach) instantly instead of falling back to its 5s interface poll.
- Report type **by transport only**, per the revised contract.
- Reserve `NOT_CONNECTED` for genuine no-default-network.
- Preserve "no client-side debounce" — heart coalesces bursts.
- Deliver reports to heart **in the order the OS produced them**.
- Cover the callback contract with tests; the file currently has none.

### Non-goals
- `AppStateService` and the interface getter: audited compliant, untouched.
- No use-case layer for this path, no un-commenting `Middleware` logging, no `SetAppState` retry — deliberate scope call (§8).
- No client-side dedup/conflation. Heart dedupes (`!typeChanged && !idChanged → return`); duplicating that here would risk diverging from it.

## 3. Design

### 3.1 Proto (`protocol/src/main/proto/commands.proto:8924`)

Add field 2, copied **verbatim** from heart `go-7379` (`pb/protos/commands.proto:8995-9000`), comments included, so the eventual `make update_mw` overwrite is a zero-diff no-op:

```proto
message Request {
    anytype.model.DeviceNetworkType deviceNetworkType = 1;
    // opaque identity of the current network path as reported by the OS
    // (iOS: NWPath interfaces/gateway digest; Android: Network#getNetworkHandle()).
    // When it changes while the type stays the same (Wi-Fi to Wi-Fi switch,
    // cellular re-attach) the middleware still resets connections and re-syncs.
    // Optional: empty means unknown, only type transitions are used then.
    string networkId = 2;
}
```

**Why hand-editing the proto is safe.** `scripts/mw/update-mw.sh` overwrites `protocol/src/main/proto/` wholesale from the heart release tarball, so this edit is temporary by construction — and idempotent, because the released proto will carry the identical field. `make update_mw_custom` documents manual proto edits as a supported workflow. On the wire, Wire 5.3.5 emits field 2 as a proto3 string; MW `v0.50.12` treats it as an unknown field and ignores it. This is what lets the change ship ahead of heart.

### 3.2 Plumbing — second parameter through six signatures

| File | Line | Change |
|---|---|---|
| `domain/…/block/repo/BlockRepository.kt` | 579 | `suspend fun setDeviceNetworkState(type: DeviceNetworkType, networkId: String)` |
| `data/…/repo/block/BlockDataRepository.kt` | 1257 | pass through |
| `data/…/repo/block/BlockRemote.kt` | 532 | signature |
| `middleware/…/block/BlockMiddleware.kt` | 1234 | pass through |
| `middleware/…/interactor/Middleware.kt` | 3320 | `Rpc.Device.NetworkState.Set.Request(deviceNetworkType = type.mw(), networkId = networkId)` |

`MiddlewareService` / `MiddlewareServiceImplementation` are unchanged — they already take the built `Request`. `ToMiddlewareModelMappers.kt:685` (`DeviceNetworkType.mw()`) is unchanged; the enum is untouched.

No default value on `networkId`. There is exactly one call site, and every caller should be forced to decide what identity it is reporting.

### 3.3 `NetworkConnectionStatusImpl` (rewrite)

```kotlin
private data class Report(val type: DeviceNetworkType, val networkId: String)

private val reports = Channel<Report>(Channel.UNLIMITED)
private var consumer: Job? = null
private var isMonitoring = false

@Volatile
private var currentNetworkType: DeviceNetworkType = DeviceNetworkType.NOT_CONNECTED

private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
        report(mapNetworkType(caps), network.networkHandle.toString())
    }

    // onLost on a default-network callback means "lost default status", not
    // "disconnected": it fires on every Wi-Fi<->cellular handoff. Report the
    // network that is actually default now; only a genuinely absent default
    // yields NOT_CONNECTED. See §3.4.
    override fun onLost(network: Network) = reportCurrent()

    // Never delivered for registerDefaultNetworkCallback (the platform only
    // calls it for requestNetwork-with-timeout). Kept per the heart contract
    // so registration style can change without silently losing offline reports.
    override fun onUnavailable() = report(DeviceNetworkType.NOT_CONNECTED, "")
}

// Transport only. Deliberately NOT gated on NET_CAPABILITY_VALIDATED:
// validation gates internet, but a LAN-only network (or a captive portal)
// still syncs over local P2P, and heart throttles NOT_CONNECTED devices.
// See anytype-heart 8a2fa9d91.
private fun mapNetworkType(caps: NetworkCapabilities?): DeviceNetworkType = when {
    caps == null -> DeviceNetworkType.NOT_CONNECTED
    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> DeviceNetworkType.CELLULAR
    else -> DeviceNetworkType.WIFI   // wifi / ethernet / vpn / tethering, incl. LAN-only
}

private fun report(type: DeviceNetworkType, networkId: String) {
    currentNetworkType = type
    reports.trySend(Report(type, networkId))
}

private fun reportCurrent() {
    val (network, caps) = activeNetworkAndCaps() ?: return report(DeviceNetworkType.NOT_CONNECTED, "")
    report(mapNetworkType(caps), network.networkHandle.toString())
}

/** The default network and its capabilities, or null if there is no usable default. */
private fun activeNetworkAndCaps(): Pair<Network, NetworkCapabilities>? {
    val cm = connectivityManager ?: return null
    val network = cm.activeNetwork ?: return null
    val caps = try {
        cm.getNetworkCapabilities(network)
    } catch (e: Throwable) {
        Timber.w(e, "Failed to get network capabilities")
        null
    } ?: return null
    return network to caps
}

// Unchanged semantics: while monitoring, serve the last reported value (now
// written synchronously by report()); otherwise query on demand.
override fun getCurrentNetworkType(): DeviceNetworkType =
    if (isMonitoring) currentNetworkType else mapNetworkType(activeNetworkAndCaps()?.second)
```

`onAvailable` is **dropped**: `onCapabilitiesChanged` is guaranteed to follow it (immediately, on API 26+, which is our `minSdk`), and the existing override re-queries `activeNetwork` — the racy pattern being removed.

`start()` keeps an explicit `reportCurrent()` baseline: when there is no default network at launch **no callback fires at all**, so without priming heart would never receive the initial `NOT_CONNECTED`.

`currentNetworkType` is assigned synchronously on the callback thread (hence `@Volatile`), so `getCurrentNetworkType()` no longer lags a coroutine hop behind reality.

#### Transport mapping rationale (verified against platform docs)

- **VPN is handled correctly by cellular-first.** Android unions the underlying transports into the VPN network: *"a VPN operating over both Wi-Fi and mobile networks. The VPN has the Wi-Fi, mobile, and VPN transports."* So VPN-over-cellular carries `TRANSPORT_CELLULAR` → `CELLULAR`. A VPN spanning both yields `CELLULAR` — the conservative answer.
- **`else -> WIFI` is the only sane catch-all** for a three-value enum. Ethernet, USB/Bluetooth tethering and Wi-Fi Aware must not land on `NOT_CONNECTED`, which is the state heart throttles.
- **Known caveat, does not apply here.** Google warns `hasTransport` is *"a poor proxy for the bandwidth or meteredness of the network"* and recommends `NET_CAPABILITY_NOT_METERED` for metered decisions. We report path identity, not meteredness. If heart's Wi-Fi-gate hook is semantically a *metered* gate, that is a heart-side question — out of scope.
- **`networkHandle` is a valid identity.** *"Even if the device later reconnects to the same appliance, a new Network object represents the new network"* — so the handle changes across reconnects, which is exactly the semantics `networkId` wants.

### 3.4 Why `onLost` reports the current default rather than `NOT_CONNECTED`

The heart guide's Android snippet reports `NOT_CONNECTED` unconditionally from `onLost`. On a default-network callback that is wrong in a way that costs real UX, and heart does **not** absorb it:

- `networkstate.go:255-262` — `IsOffline()` returns `true` the instant state is `NOT_CONNECTED`, with **no debounce**. Only `triggerRecovery` is debounced (leading-edge + 5s suppression + trailing run).
- `networkstate.go:225-227` — `runOnNetworkUpdateHook` fires on every `typeChanged`, so the Wi-Fi-gate/mDNS hooks tear down and rebuild.

Since `onLost` fires on **every** Wi-Fi↔cellular handoff (documented sequence: `onLost(wifi)` → `onAvailable(cellular)` → `onCapabilitiesChanged(cellular)`), the snippet would flip sync status offline and churn mDNS on every switch.

`reportCurrent()` avoids this: Android keeps cellular warm, so the replacement default is normally already up and we report its real type with no blip. Behaviour by case:

| Case | `activeNetwork` at `onLost` | Reported |
|---|---|---|
| Wi-Fi→cellular handoff | cellular (already up) | `CELLULAR` + handle — no blip |
| True disconnect (airplane mode) | `null` | `NOT_CONNECTED` — the only signal, since no `onCapabilitiesChanged` follows |
| Handoff with a real gap | `null` briefly | `NOT_CONNECTED`, corrected by the next `onCapabilitiesChanged` — correct, we genuinely are offline |

**Follow-up:** send a patch to `docs/mobile-network-integration.md` so the published guide matches, and so iOS is not left following advice Android rejected. Tracked separately; not a blocker.

### 3.5 Ordering — serialize reports through a channel

`updateNetworkState` currently does `coroutineScope.launch { withContext(dispatchers.io) { … } }` on a `Dispatchers.Default` scope. ConnectivityManager delivers callbacks serialized on one thread, but each `launch` is dispatched independently, so **two rapid callbacks can reach the RPC out of order**. Latent today; sharper now, because heart coalesces bursts into one trailing run where the *last* value wins — so a `NOT_CONNECTED` overtaken by a stale `WIFI` leaves heart believing it is online while the device is offline, until the next callback.

One `Channel<Report>(UNLIMITED)` lives for the object's lifetime, drained by a consumer `Job`:

```kotlin
override fun start() {
    if (isMonitoring) return
    // Discard anything a late callback pushed after the previous session's
    // stop() -- a stale value must not become heart's baseline for this one.
    while (reports.tryReceive().isSuccess) { /* drop */ }
    try {
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
        isMonitoring = true
    } catch (e: RuntimeException) {
        Timber.w(e, "Failed to register network callback")
        return   // consumer chain untouched -- nothing was launched
    }
    val previous = consumer
    consumer = coroutineScope.launch(dispatchers.io) {
        previous?.join()   // see the join bullet below
        for (r in reports) {
            try {
                blockRepository.setDeviceNetworkState(r.type, r.networkId)
            } catch (e: Throwable) {
                Timber.w(e, "Failed to update network state")   // per-report; must not kill the loop
            }
        }
    }
    reportCurrent()
}

override fun stop() {
    if (!isMonitoring) return
    try {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    } catch (e: Throwable) {
        Timber.w(e, "Failed to unregister network callback")
    } finally {
        isMonitoring = false
        // Cancel but KEEP the reference: the next start()'s consumer joins it.
        consumer?.cancel()
    }
}
```

Both `isMonitoring` and `currentNetworkType` are `@Volatile`: written on the caller/callback threads, read by `getCurrentNetworkType()` from arbitrary threads. (The un-synchronized `isMonitoring` was a pre-existing hazard; this change touches the line anyway.)

The stale-report drain lives at the top of `start()`, not in `stop()`: ConnectivityManager delivers callbacks asynchronously, so one can land *after* anything `stop()` does — draining on the next `start()` is the only placement that cannot be raced.

Properties:
- **FIFO is guaranteed** by the `Channel` contract, not inferred from a dispatcher's implementation.
- **Never closed**, so `start()`/`stop()` can cycle across login→logout→login. Nothing accumulates while stopped, because the callback is unregistered and `reportCurrent()` only runs from `start()`.
- **Callbacks never block.** `trySend` on an `UNLIMITED` channel never suspends and never fails-on-full, so ConnectivityManager's thread is never held.
- **No conflation** — contract-compliant.
- The drain at the top of `start()` discards reports from the ended session so a stale value cannot become heart's *baseline* (`first`) after the next login.
- The consumer `join()`s its predecessor before processing: cancellation is cooperative and the RPC chain has no suspension points, so a consumer stopped mid-JNI-call keeps running until the call returns — without the join, its stale report could complete *after* the next session's baseline and become heart's final word.

## 4. Data flow

```
onCapabilitiesChanged(net, caps)        [CM thread, serialized]
  → report(type, net.networkHandle)     [sets @Volatile currentNetworkType; trySend, non-blocking]
  → reports: Channel(UNLIMITED)         [FIFO]
  → consumer Job on dispatchers.io      [single consumer; per-report try/catch]
  → blockRepository.setDeviceNetworkState(type, networkId)
  → BlockDataRepository → BlockRemote → BlockMiddleware → Middleware
  → Rpc.Device.NetworkState.Set.Request(deviceNetworkType, networkId)
  → service.deviceNetworkStateSet(...)  [JNI, blocking]
```

## 5. Error handling

- **Per-report `try/catch` inside the loop.** This is load-bearing: a throw inside `for (r in reports)` terminates the loop and silently disables network reporting for the rest of the session. The current code's catch is per-`launch`, so this hazard is introduced by the channel refactor and must not be missed.
- `trySend` failure is impossible for `UNLIMITED`/open; the return is ignored deliberately.
- Registration happens *before* the consumer is launched: a registration failure returns early with `isMonitoring = false` and the consumer chain untouched, so a predecessor still draining is unaffected.
- `getNetworkCapabilities` throwing is caught in `activeNetworkAndCaps()` → treated as no network.

## 6. Testing

New: `device/src/test/java/com/anytypeio/anytype/device/network_type/NetworkConnectionStatusImplTest.kt`. Robolectric 4.11.1, mockito-kotlin and coroutine-testing are already on `device`'s test classpath — no dependency changes. Callbacks are retrieved via `shadowOf(connectivityManager).networkCallbacks` and invoked directly; assertions are `verify` on a mocked `BlockRepository`.

| Test | Asserts |
|---|---|
| `onCapabilitiesChanged` with `TRANSPORT_WIFI` | `WIFI`, id = handle |
| `onCapabilitiesChanged` with `TRANSPORT_CELLULAR` | `CELLULAR`, id = handle |
| `onCapabilitiesChanged` with `TRANSPORT_ETHERNET` | `WIFI` (regression guard for `else -> NOT_CONNECTED`) |
| `onCapabilitiesChanged` with VPN + cellular transports | `CELLULAR` |
| `onCapabilitiesChanged` **without** `NET_CAPABILITY_VALIDATED` | `WIFI`, **not** `NOT_CONNECTED` (regression guard for the reversal) |
| `onLost` with another default already up | that network's type — **no** `NOT_CONNECTED` |
| `onLost` with no active network | `NOT_CONNECTED`, id `""` |
| `onUnavailable` | `NOT_CONNECTED`, id `""` |
| two identical `onCapabilitiesChanged` | **two** RPCs (no client-side debounce) |
| burst of distinct reports | delivered in order (`inOrder` verify) |
| RPC throws on report *n* | report *n+1* still delivered (consumer survives) |
| `start()` with no active network | baseline `NOT_CONNECTED` emitted |
| `stop()` then `start()` | callback re-registered; consumer drains again |

## 7. Risks

1. **Robolectric may not support `Network.networkHandle`.** `ShadowNetwork.newInstance(netId)` exists, but the handle accessor is unverified. **Spike this first.** Fallback: inject a `networkIdOf: (Network) -> String` seam defaulting to `{ it.networkHandle.toString() }` — a testing seam, not a production abstraction.
2. **No end-to-end verification until heart ships.** `networkId` cannot be observed against MW `v0.50.12`. Mitigated by unit tests + wire-level backward compatibility. When GO-7379 releases, verify per the guide: one `connectivity recovery` log per Wi-Fi→Wi-Fi switch; airplane-mode toggle flips offline ~1s and recovers ~2s.
3. **`onLost` deviates from the published guide.** Justified in §3.4 and evidenced against heart's source; the doc patch keeps them from drifting.
4. **`make update_mw` will overwrite the proto edit.** By design, and idempotent — but if heart's released field ever differs in name or number, the Kotlin build breaks loudly at the `Middleware.kt` call site rather than silently. Acceptable.

## 8. Out of scope → follow-up tickets

- **Captive-portal onboarding.** `OnboardingMnemonicViewModel.shouldShowEmail()` (`:88-95`) treats `NOT_CONNECTED` as "no network" and asks a question this enum can't answer ("is there real internet?"). Ethernet/VPN improves for free here (previously `NOT_CONNECTED` → email screen wrongly skipped); captive-portal Wi-Fi keeps today's behaviour (`WIFI` → email screen shown, submit fails). Pre-existing, not a regression. Proper fix: a `VALIDATED`-backed `hasInternet()` — the legitimate use of `VALIDATED`.
- `AppStateService` swallows RPC failures with `Timber.d` and never retries; a FOREGROUND sent before account start is lost until the next lifecycle transition.
- `Middleware.setDeviceNetworkState` has its `logRequestIfDebug`/`logResponseIfDebug` calls commented out (`Middleware.kt:3324,3326`) and discards the response.
- This path bypasses the use-case layer, unlike the sibling `SetAppState`.
- `LocalNetworkAddressProvider.start()` is fire-and-forget on `GlobalScope`, leaving a startup window where Go's `interfaceGetter` is nil and `GetInterfacesAddrs()` errors.
- Patch `docs/mobile-network-integration.md` in anytype-heart for the `onLost` correction (§3.4).

## 9. Sequencing

1. Spike Robolectric `networkHandle` support (risk 1) — decides the test seam.
2. Proto field 2 + Wire regen; confirm `:protocol:compileDebugKotlin`.
3. Plumbing: the six signatures, `Middleware` last.
4. Rewrite `NetworkConnectionStatusImpl` (mapping, callbacks, channel).
5. Tests.
6. `./gradlew :device:testDebugUnitTest`, then `make test_debug_all`.
7. File the §8 follow-ups.
