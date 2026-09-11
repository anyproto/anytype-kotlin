# DROID-4483 — Thermal/Memory Pressure → Sentry Debug Reports — Design

**Linear:** https://linear.app/anytype/issue/DROID-4483
**Parent:** DROID-4400
**Backend PR:** anyproto/anytype-heart#3115 (GO-7143)
**iOS mirror:** IOS-5994
**Branch:** `droid-4483-detect-thermalmemory-pressure-and-send-debug-reports-to`
**Status:** Design — pending implementation plan

---

## Goal

Integrate Android system-level pressure signals (memory + thermal) with the new Go-side debug reporting pipeline, ship critical reports to Sentry on debug builds, and upgrade the existing "Send logs" debug action and crash/ANR pipeline to use the new `DebugExportReport(full=true)` bundle format with proper cleanup.

## Non-goals

- In-process ANR auto-attach (during a live ANR). Crashes and ANRs both flow through next-launch recovery; live in-process ANR attachment is deferred.
- Replacing the existing share-sheet UX of "Export logs" with a Sentry upload. The action still opens a share-sheet — only the zip's contents change to the new `full=true` bundle.
- Client-side aging/rotation of snapshot files in `<filesDir>/common/profiles/`. Rely on Go middleware's internal rotation.
- Any change to crash flow itself (Sentry's `UncaughtExceptionHandlerIntegration` continues to own crash capture). We add a *next-launch companion event* that carries the debug bundle.

---

## Architecture summary

### Layer responsibilities

| Layer | Responsibility |
|---|---|
| Proto (`protocol/`) | Already up-to-date. Wire regenerates Kotlin at build time. New: `Reason` enum, `reasonDesc`, `ExportReport.Request.full`, `ExportReport.Response.{summary, lastModifiedTs}`, `CleanupReport` RPC. |
| Middleware (`middleware/`) | Add `debugCleanupReport(ts)`. Update `debugRunProfiler` to take `(durationInSeconds, reason, reasonDesc)`. Update `debugExportLogs` → `debugExportReport(dir, full): (path, summary, ts)`. Thread through `MiddlewareService(Implementation)`, `AuthMiddleware`, `AuthRemote`, `AuthRemoteDataStore`, `AuthRepository`. |
| Domain (`domain/debugging/`) | Update `DebugRunProfiler.Params` to add `reason`/`reasonDesc`. Replace `DebugExportLogs` with `DebugExportReport(dir, full): ExportReportResult`. Add `DebugCleanupReport(ts)`. |
| Crash reporting (`crash-reporting/`) | Extend `CrashReporter` with `reportCritical(reason, durationSeconds): Result<Unit>` and `reportFromPriorRun(): Boolean`. `SentryCrashReporter` implements both — orchestrates `DebugExportReport` → `Sentry.captureEvent` w/ attachment → `DebugCleanupReport`. |
| App | New `ThermalMemoryReporter` singleton (DI-provided), new `CrashRecoveryStartupTask`, new `ThermalMemoryModule`. `AndroidApplication` wires `ComponentCallbacks2.onTrimMemory`, registers `PowerManager.OnThermalStatusChangedListener` after `InitialSetParameters`, and runs the recovery task once at startup. `DebugViewModel.onDiagnosticsExportLogClicked` switches to the new `full=true` bundle with cleanup. |

### New code surface (estimated)

- ~6 layers × proto-threading edits (small, mechanical).
- New `DebugCleanupReport` use case + repository chain (small).
- `ThermalMemoryReporter` (~150 lines + tests).
- `CrashRecoveryStartupTask` (~80 lines + tests).
- `SentryCrashReporter.reportCritical` + `reportFromPriorRun` (~120 lines + tests).
- `ThermalMemoryModule` (~30 lines).
- `AndroidApplication` integration (~40 lines).
- `DebugViewModel` update (~20 lines diff).

---

## Component design

### `ThermalMemoryReporter`

Single Dagger `@Singleton` injected into `AndroidApplication`. Implements `ComponentCallbacks2` semantics indirectly (the Application owns the `ComponentCallbacks2` override and forwards to the reporter). Holds the thermal listener.

**Public surface:**

```kotlin
class ThermalMemoryReporter @Inject constructor(
    private val powerManager: PowerManager,
    private val debugRunProfiler: DebugRunProfiler,
    private val crashReporter: CrashReporter,
    private val buildProvider: BuildProvider,
    private val dispatchers: AppCoroutineDispatchers,
) {
    fun start()                                    // idempotent; called once after InitialSetParameters
    fun stop()                                     // tests only
    fun onTrimMemory(level: Int)                   // forwarded from Application
    @VisibleForTesting fun onThermalStatusChanged(status: Int)
}
```

**State machine:** `IDLE → STARTED → STOPPED`. Pressure callbacks no-op while IDLE or STOPPED.

**Threading:** owns a `CoroutineScope(SupervisorJob() + Dispatchers.Default)` (or `dispatchers.computation` if the project's existing `AppCoroutineDispatchers` exposes a Default-equivalent — pick whichever matches the codebase convention at implementation time). Pressure callbacks return immediately after launching a coroutine.

**Rate limiter (debug-only):** `AtomicLong lastReportAtMs`. Single global bucket — one `reportCritical` per 10-min window across all critical signals. Resets on process restart.

**Mapping table:**

| System signal | Reason | duration (s) | Triggers `reportCritical`? |
|---|---|---|---|
| `TRIM_MEMORY_RUNNING_LOW` | `MEMORY_PRESSURE_WARN` | 0 | No |
| `TRIM_MEMORY_RUNNING_CRITICAL` | `MEMORY_PRESSURE_CRITICAL` | 0 | Yes (debug only, rate-limited) |
| `THERMAL_STATUS_SEVERE` | `THERMAL_SERIOUS` | 30 | No |
| `THERMAL_STATUS_CRITICAL` (or higher) | `THERMAL_CRITICAL` | 30 | Yes (debug only, rate-limited) |

`reasonDesc` left null in v1; can be filled later with RSS / thermal-headroom context.

### `SentryCrashReporter` extensions

```kotlin
interface CrashReporter {
    fun init()
    fun setUser(userId: String)
    suspend fun reportCritical(reason: String, durationSeconds: Int): Result<Unit>   // NEW
    suspend fun reportFromPriorRun(): Boolean                                          // NEW
}
```

**`reportCritical`:**
1. `DebugExportReport(cacheDir, full=false)` → `(zipPath, summary, ts)`.
2. Parse `summary` JSON → `Map<String, Any>`. Set on event contexts (`event.contexts["debug_report"]`).
3. `Hint().also { it.addAttachment(Attachment(zipPath, "debug-report.zip")) }`.
4. `Sentry.captureEvent(SentryEvent().apply { level = WARNING; message = "thermal_memory_pressure: $reason" }, hint)`.
5. If returned `SentryId != EMPTY_ID` → `DebugCleanupReport(ts)`. Otherwise return failure.

**`reportFromPriorRun`:**
1. Check `Sentry.isCrashedLastRun() == true`. If false, return `false`.
2. `withTimeout(30_000)` { `DebugExportReport(cacheDir, full=true)` }.
3. Build a new `SentryEvent` and set a custom tag `companion_to_event_id` = `Sentry.getLastEventId()` (best-effort — empty string if unavailable). This is our own correlation tag, not a Sentry-defined one; it gets used for filtering in the Sentry UI.
4. Capture with attachment + summary in contexts.
5. `DebugCleanupReport(ts)` on success.

### `CrashRecoveryStartupTask`

Runs once in `AndroidApplication.onCreate` after Sentry init and after `InitialSetParameters` succeeds. Calls `crashReporter.reportFromPriorRun()` on `Dispatchers.IO`. Single attempt per launch; if it fails, the Sentry "crashed last run" sentinel is not consumed by us (Sentry manages its own state), so a still-pending recovery survives across launches naturally.

### Wiring in `AndroidApplication`

```kotlin
override fun onCreate() {
    super.onCreate()
    setupCrashReporter()
    // ... existing init ...
    setupAnytype()                          // already calls InitialSetParameters
    thermalMemoryReporter.start()           // NEW — registers thermal listener
    crashRecoveryStartupTask.start()        // NEW — recovers prior-run crash bundle
}

override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    thermalMemoryReporter.onTrimMemory(level)
}
```

Application implements the `onTrimMemory` override; it owns the system contract and forwards to the reporter. Thermal listener is registered inside `ThermalMemoryReporter.start()` directly (`PowerManager.addThermalStatusListener`).

### `DebugViewModel.onDiagnosticsExportLogClicked` upgrade

```kotlin
fun onDiagnosticsExportLogClicked() {
    viewModelScope.launch {
        debugExportReport(Params(dir = cacheDir, full = true))
            .fold(
                onSuccess = { (path, _, ts) ->
                    commands.emit(Command.ShareDebugLogs(path))
                    debugCleanupReport(DebugCleanupReport.Params(ts = ts))   // fire-and-forget
                },
                onFailure = { commands.emit(Command.ShowErrorToast(it.message.orEmpty())) }
            )
    }
}
```

Cleanup runs immediately after the share-sheet command is emitted — `DebugCleanupReport` removes the *source files* on the middleware side that made it into the archive. The zip itself in `cacheDir` survives until Android's normal cacheDir cleanup (storage pressure or app uninstall); the user's share is a content-URI copy of that zip.

---

## Data flow diagrams

### Pressure signal path (release builds)

```
System callback                    ThermalMemoryReporter           Middleware
─────────────────                  ─────────────────────           ──────────
onTrimMemory(RUNNING_LOW)          ──► runProfiler(0, WARN)        ──► DebugRunProfiler RPC
                                                                      └► <filesDir>/common/profiles/snapshot_*.zip
onTrimMemory(RUNNING_CRITICAL)     ──► runProfiler(0, CRITICAL)    ──► [snapshot]
onThermalStatusChanged(SEVERE)     ──► runProfiler(30, SERIOUS)    ──► [snapshot]
onThermalStatusChanged(CRITICAL+)  ──► runProfiler(30, CRITICAL)   ──► [snapshot]
```

### Critical → Sentry path (debug builds only)

```
Reporter detects critical signal
  │
  ├── runProfiler(reason)               [always]
  │
  └── if BuildConfig.DEBUG && !rateLimited:
        crashReporter.reportCritical(reason, durationSeconds)
          │
          ├── DebugExportReport(cacheDir, full=false) ──► (zipPath, summary, ts)
          ├── parse summary → event.contexts["debug_report"]
          ├── Sentry.captureEvent(event, Hint with attachment(zipPath))
          ├── if eventId != EMPTY_ID:
          │     DebugCleanupReport(ts)
          └── update lastReportAtMs
```

### Send Logs path

```
DebugFragment "Export logs" tap
  │
  └── DebugViewModel.onDiagnosticsExportLogClicked
        │
        ├── DebugExportReport(cacheDir, full=true) ──► (zipPath, summary, ts)
        ├── emit Command.ShareDebugLogs(zipPath)              [share-sheet opens]
        └── DebugCleanupReport(ts)                            [fire-and-forget]
```

### Crash/ANR next-launch recovery path

```
AndroidApplication.onCreate
  │
  ├── Sentry.init                  [existing]
  ├── signal handler init          [existing]
  ├── setupAnytype                 [existing — calls InitialSetParameters]
  │
  └── CrashRecoveryStartupTask.start
        │
        └── if Sentry.isCrashedLastRun():
              withTimeout(30s) {
                ├── DebugExportReport(cacheDir, full=true) ──► (zipPath, summary, ts)
                ├── Sentry.captureEvent(companion-event, Hint with attachment(zipPath))
                │     event tags: companion_to_event_id = lastEventId
                └── DebugCleanupReport(ts)
              }
```

---

## Error handling

| Failure | Behavior |
|---|---|
| `DebugRunProfiler` RPC fails | `Timber.w` log; no retry; next signal will trigger again. |
| Thermal listener registration fails (API < 29 / OEM weirdness) | Silent skip; memory wiring proceeds. |
| `reportCritical`: `DebugExportReport` fails | No `captureEvent`; do not advance `lastReportAtMs` (allow retry on next signal). |
| `reportCritical`: `Sentry.captureEvent` returns `EMPTY_ID` | Skip cleanup; do not advance `lastReportAtMs`. |
| `reportCritical`: `DebugCleanupReport` fails after successful capture | `Timber.w` log; advance `lastReportAtMs` (data is already in Sentry; orphaned snapshots get rotated by middleware). |
| Send Logs: `DebugExportReport(full=true)` fails | Existing UX — emit `Command.ShowErrorToast` with the error message. No share-sheet. |
| Send Logs: cleanup fails | Swallow + log. |
| Crash recovery: any step fails | `Timber.w`; bail. Single attempt per launch; recovery survives across launches via Sentry's own crash sentinel. |
| Crash recovery: 30s timeout | Abandon for this launch. |

**Crash safety of the listener itself:**
- `OnThermalStatusChangedListener` runs on a system handler thread. Callback launches a coroutine and returns immediately — re-entrant safe.
- Reporter never holds Activity refs; it's Application-scoped.

**ProGuard / R8:** Wire-generated proto classes are already kept; Sentry SDK ships consumer rules. Expected: zero new ProGuard rules.

---

## Testing strategy

### Unit tests

| Component | Test cases |
|---|---|
| `ThermalMemoryReporter` | (1) `onTrimMemory(RUNNING_LOW)` calls `DebugRunProfiler(0, MEMORY_PRESSURE_WARN)`. (2) `onTrimMemory(RUNNING_CRITICAL)` calls profiler AND `reportCritical` on debug. (3) `onThermalStatusChanged(SEVERE)` calls `(30, THERMAL_SERIOUS)`. (4) `onThermalStatusChanged(CRITICAL)` calls profiler AND `reportCritical` on debug. (5) Signals before `start()` dropped silently. (6) Signals after `stop()` dropped silently. (7) Two critical signals within 10 min: only first triggers `reportCritical`. (8) Two critical signals 10 min + 1 ms apart: both trigger. (9) `DebugRunProfiler` failure does not crash, does not block next signal. (10) Release builds (`isDebug=false`) never call `reportCritical`. |
| `SentryCrashReporter.reportCritical` | (1) Success path: `DebugExportReport(full=false)` → `Sentry.captureEvent` w/ attachment → `DebugCleanupReport(ts)`. (2) `DebugExportReport` fail → no capture, no cleanup. (3) `captureEvent` returns `EMPTY_ID` → no cleanup, returns failure. (4) `DebugCleanupReport` fail post-capture → returns success. (5) Summary JSON parsed into event contexts. |
| `SentryCrashReporter.reportFromPriorRun` | (1) `isCrashedLastRun=true` → `DebugExportReport(full=true)` + companion event. (2) `isCrashedLastRun=false` → no-op, returns false. (3) 30s timeout aborts cleanly. |
| `DebugCleanupReport` use case | Calls repo with the right `ts`. |
| `DebugExportReport` use case | Returns `(path, summary, ts)` triple from repo. |
| `DebugViewModel.onDiagnosticsExportLogClicked` | Emits `Command.ShareDebugLogs(zipPath)` AND calls `DebugCleanupReport(ts)`. Failure path emits error toast, no share-sheet. |
| `Middleware.kt` mappers | `debugRunProfiler(reason, desc)`, `debugExportReport(dir, full)`, `debugCleanupReport(ts)` build correct `Rpc.Debug.*.Request` instances. |

Test framework: standard JUnit + Mockito. Robolectric only where Android APIs (`PowerManager`, `BuildConfig`) are needed. Use `TestDispatcher` with virtual time for the 10-min rate-limit assertions.

### Integration / instrumentation tests

None planned. The Sentry network leg and JNI middleware leg are untestable without a live backend; value lies in unit-level state-machine coverage.

### Manual QA checklist

1. Fresh install → `adb shell am send-trim-memory <pid> RUNNING_LOW` → snapshot in `<filesDir>/common/profiles/` tagged `MEMORY_PRESSURE_WARN`.
2. `adb shell am send-trim-memory <pid> RUNNING_CRITICAL` → snapshot tagged `MEMORY_PRESSURE_CRITICAL`. On debug build: Sentry event lands w/ attached zip + parsed summary in event contexts. Source files cleaned from disk.
3. `adb shell cmd thermalservice override-status 5` (Pixel) → snapshot tagged `THERMAL_CRITICAL`. Same Sentry behavior on debug.
4. Tap "Export logs" in Debug settings → share-sheet opens with new `full=true` bundle. Compare zip contents to previous behavior — should now include `logs/bundle.log.gz`, all snapshot zips, `info.json`.
5. `adb shell am crash <pid>` → on next launch, Sentry "companion" event lands with the `full=true` bundle attached, tagged `companion_to_event_id`.
6. On debug build, fire two `RUNNING_CRITICAL` signals within 1 min → only one Sentry event. Wait 10 min, fire again → second event ships.
7. Release build (`./gradlew assembleRelease`) → pressure signals produce snapshots locally but no Sentry uploads from the thermal/memory path. (Crash recovery still ships on next launch.)

---

## Acceptance criteria (from ticket)

- [ ] After fresh install, thermal/memory pressure callbacks produce snapshots in `<filesDir>/common/profiles/` tagged with the correct `reason`. (Covered by manual QA #1–3.)
- [ ] `reportCritical` produces a Sentry event with the archive attached and, on success, cleans up shipped files via `DebugCleanupReport`. (Covered by `reportCritical` unit tests + manual QA #2.)
- [ ] The existing "Send logs" debug action uses `DebugExportReport(full=true)` and cleans up after a successful upload. (Covered by `DebugViewModel` unit tests + manual QA #4.)
- [ ] On dev builds, thermal/memory critical signals auto-report (rate-limited). (Covered by reporter unit tests #2, #4, #7, #8 + manual QA #6.)

---

## Threading model summary

| Operation | Thread |
|---|---|
| `onTrimMemory` callback | main (Application contract) |
| `onThermalStatusChanged` callback | system handler thread |
| `runProfiler` RPC | reporter scope (`Dispatchers.Default`) |
| `reportCritical` chain | reporter scope (`Dispatchers.Default`) |
| Crash recovery chain | startup task scope (`Dispatchers.IO`) |
| Sentry capture | Sentry SDK's own non-blocking thread |
| Send Logs flow | existing `DebugViewModel.viewModelScope` |

---

## Decisions log

| # | Decision | Rationale |
|---|---|---|
| 1 | Send Logs keeps share-sheet UX, only zip contents change | Preserves user choice of destination; matches user pick "B". |
| 2 | Crash/ANR via next-launch recovery (not in-process `BeforeSend`) | Robust — never blocks crashing process or risks losing crash event. Single hook covers both crashes and ANRs. |
| 3 | Listeners register only after `InitialSetParameters` succeeds | Simpler than buffering; cold-launch pre-init window is too short for actionable signals. |
| 4 | Single global 10-min rate limit across all critical signals | Simplest interpretation of "no more than once per 10 minutes". Multiple simultaneous critical signals produce near-identical reports anyway. |
| 5 | `reportCritical` lives on `CrashReporter` interface; `ThermalMemoryReporter` is the source | Two narrow responsibilities: crash reporter talks to Sentry, thermal reporter observes the OS. |
| 6 | Single reporter class (vs split observer/reporter Flow) | Feature is cohesive; Flow split is over-engineered for an Application-scoped singleton. |
| 7 | `reasonDesc` left null in v1 | Can be filled later with RSS/thermal-headroom context; keeps v1 surface minimal. |
| 8 | No client-side snapshot rotation | Rely on Go middleware's internal rotation; non-critical snapshots get bundled into next Send Logs / crash recovery. |
| 9 | Cleanup on Send Logs runs immediately after share-sheet emit | Share-sheet doesn't notify completion; the user's share is a copy of the cacheDir zip, not a reference. |

---

## Open questions deferred to implementation

- Exact Sentry event "level" for the companion crash-recovery event (warning vs error vs info — pick what doesn't pollute the alerting view).
- Whether to set `event.fingerprint` on the companion event so multiple recoveries for the same crash deduplicate.
- Whether `reportCritical`'s event message should include device model / OS version, or rely on Sentry's auto-context.

These are implementation details, not architectural choices.
