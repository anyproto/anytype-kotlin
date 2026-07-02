# Preferred-space-aware cold start — Design

Date: 2026-05-18
Status: Approved (pending spec review)

## Problem

On cold start, anytype-heart eagerly loads every space, causing heavy disk
read activity. When the app is cold-opened into a specific route
(deeplink/push into a space/object/chat) — or simply into the last-opened
space — only that one space is needed first. Loading all spaces up front
bloats disk I/O and delays the screen the user actually wants.

The protocol already supports a fix (both fields exist on this branch):

- `Rpc.Account.Select.Request.preferredSpaceId` (field 11): if set and
  resolvable, heart loads only this space + the tech space eagerly; the rest
  are deferred until `AccountPreloadRemainingSpaces`, a heart-side 10s timer,
  or the preferred space failing to load.
- `Rpc.Account.PreloadRemainingSpaces` RPC: client signal that the priority
  screen is up and heart may now load the remaining spaces.

None of the Kotlin plumbing or call sites are wired yet. This design covers
that wiring.

## Goals

- Pass a `preferredSpaceId` into the `AccountSelect` call made on cold start.
- Source it from (in priority order): an incoming deeplink/route, a cold-start
  push target, or the last-opened space.
- After the route's destination screen is navigated to, call
  `AccountPreloadRemainingSpaces` exactly once (after a short delay) so heart
  loads the rest.

## Non-goals

- The `###` Timber instrumentation and debug-server changes already present on
  this branch are profiling scaffolding and are out of scope; this work does
  not touch them.
- No change to onboarding/new-account (`SelectAccount`) behavior.
- No client-side change to heart's deferral policy; we only supply the
  preferred space and the release signal. Heart's own 10s timer and
  "preferred space failed" fallbacks remain the correctness backstop.

## Architecture overview

Two independent pieces:

- **A. Preferred space → AccountSelect.** Resolve a preferred space id before
  `LaunchAccount` runs and pass it through `Command.AccountSelect` into the
  `Rpc.Account.Select.Request`.
- **B. Release the brake.** Once the route's destination screen is navigated
  to, wait a short delay, then call `AccountPreloadRemainingSpaces` exactly
  once.

## Components

### Piece A — preferred space id resolution

**`PreferredSpaceIdHolder`** (new)

- App-singleton, in-memory, synchronous nullable `spaceId` get/set.
- Interface declared in `domain` so `LaunchAccount` can read it; trivial
  implementation provided via DI as a singleton.
- Set as early and synchronously as possible, before `LaunchAccount` runs, by
  whichever cold-start entry knows the route:
  - **Deeplink:** in `MainActivity.onCreate`, when
    `DefaultDeepLinkResolver.resolve(uri)` yields a space id, set the holder
    immediately (synchronous, before the splash VM's async account launch).
  - **Push:** the existing cold-start push path that signals a push space
    entry sets the holder when the push target space is first known.
- Cleared after `LaunchAccount` consumes it, so a later warm relaunch in the
  same process does not reuse a stale id.

**`LaunchAccount`** (modified)

Resolve the preferred space with this priority and pass it into
`Command.AccountSelect`:

1. `PreferredSpaceIdHolder` value (deeplink/push), else
2. last-opened space — `settings.getCurrentSpace()?.id` (`LaunchAccount`
   already reads `settings`, so the common no-deeplink cold start is
   prioritized with zero new plumbing), else
3. `null`/empty → today's eager behavior.

**`Command.AccountSelect`** (modified)

- Add `val preferredSpaceId: String? = null`.
- `Middleware.accountSelect` maps it:
  `Rpc.Account.Select.Request(preferredSpaceId = command.preferredSpaceId.orEmpty(), …)`.
- `SelectAccount` (onboarding login) is unaffected — new account, field stays
  `null`.

### Piece B — preload-remaining-spaces RPC

Middleware/domain plumbing following the existing `accountSelect` pattern:

- `MiddlewareService.accountPreloadRemainingSpaces` + implementation in
  `MiddlewareServiceImplementation`, calling the generated JNI `Service`
  binding. The exact binding method name is verified at implementation time
  against the generated `service.Service` (proto message is
  `Rpc.Account.PreloadRemainingSpaces`).
- `Middleware.accountPreloadRemainingSpaces()` wrapper.
- `AuthRemote.preloadRemainingSpaces()` +
  `AuthRepository.preloadRemainingSpaces()` + `AuthDataRepository` impl.
- Domain use case `PreloadRemainingSpaces : ResultInteractor<Unit, Unit>`.

**`RemainingSpacesPreloader`** (new)

- App-singleton with a one-shot `AtomicBoolean` guard.
- `scheduleOnce(scope)`: if not already fired, mark fired, `delay(N)`, then
  invoke the `PreloadRemainingSpaces` use case once.
- Idempotent and safe to call from multiple navigation paths.

## Data flow

### Cold start with deeplink

```
MainActivity.onCreate
  → DefaultDeepLinkResolver.resolve(uri) → space id
  → PreferredSpaceIdHolder.set(spaceId)        [synchronous, pre-launch]
  → vm.handleNewDeepLink(resolved)             [unchanged]
SplashViewModel: checkAuth → launchWallet → launchAccount
  → LaunchAccount: preferred = holder ?: settings.currentSpace
  → Command.AccountSelect(preferredSpaceId = preferred)
  → heart loads only that space + tech space eagerly
MainViewModel.proceedWithNewDeepLink → emits destination nav command
  → RemainingSpacesPreloader.scheduleOnce(scope)   [delay N, then RPC once]
```

### Cold start, no deeplink (common case)

```
MainActivity.onCreate (no deeplink) → holder stays null
SplashViewModel: launchAccount
  → LaunchAccount: preferred = settings.currentSpace (last-opened)
SplashViewModel.proceedWithVaultNavigation → emits destination nav command
  → RemainingSpacesPreloader.scheduleOnce(scope)
```

### Cold start from push

Push path sets the holder when the target space is first known; otherwise
identical to the deeplink flow.

The `scheduleOnce` guard means whichever navigation path fires first (Splash
for no-deeplink, MainViewModel for deeplink/push) triggers exactly one delayed
`AccountPreloadRemainingSpaces`.

### Delay value

**2 seconds** after the navigation command is emitted: long enough for the
destination screen's primary subscription to be issued and prioritized by
heart, short enough that remaining spaces are not starved. Adjustable; heart's
10s timer is the hard backstop.

## Edge cases

- **Race — deeplink not yet parsed when `LaunchAccount` runs:** holder is
  null → fall back to last-opened space. Acceptable: the deeplink space is
  often the last-opened one anyway, and the later deeplink-driven space switch
  plus heart's timer cover the rest. No blocking/waiting is added.
- **Onboarding / new account:** `SelectAccount` path untouched;
  `preferredSpaceId` stays null.
- **Holder lifetime:** cleared after `LaunchAccount` consumes it; in-memory so
  process death clears it naturally.
- **Preload called when account not running:** RPC returns
  `ACCOUNT_IS_NOT_RUNNING`; the use case treats this as benign (logged, not
  surfaced to the UI).
- **Multiple navigation paths firing:** `RemainingSpacesPreloader` one-shot
  guard ensures a single RPC.

## Testing

- Unit-test `LaunchAccount` preferred-space priority: holder > settings >
  empty.
- Unit-test `PreloadRemainingSpaces` use case: success and error mapping
  (including `ACCOUNT_IS_NOT_RUNNING` treated as benign).
- Unit-test `RemainingSpacesPreloader`: multiple `scheduleOnce` calls →
  exactly one use-case invocation.
- Follow existing `ResultInteractor` / use-case test patterns and DI module
  patterns in the codebase.

## Affected files (indicative, finalized in the plan)

- `core-models/.../Command.kt` — `AccountSelect.preferredSpaceId`
- `domain/.../auth/interactor/LaunchAccount.kt` — preferred-space resolution
- `domain/...` — `PreferredSpaceIdHolder` interface,
  `PreloadRemainingSpaces` use case
- `domain/.../auth/repo/AuthRepository.kt` — `preloadRemainingSpaces`
- `data/.../auth/repo/AuthDataRepository.kt`, `AuthRemote.kt`
- `middleware/.../auth/AuthMiddleware.kt`,
  `middleware/.../interactor/Middleware.kt`,
  `middleware/.../service/MiddlewareService.kt`,
  `MiddlewareServiceImplementation.kt`
- `app/.../ui/main/MainActivity.kt` — set holder from deeplink
- `presentation/.../main/MainViewModel.kt`,
  `presentation/.../splash/SplashViewModel.kt` — `scheduleOnce` call sites
- Push cold-start path — set holder from push target
- DI modules — provide new singletons/use cases
- Test sources for the units above
