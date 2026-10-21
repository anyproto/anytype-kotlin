# Preferred-space-aware cold start — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** On cold start, tell anytype-heart which single space to load eagerly (`AccountSelect.preferredSpaceId`) and, once the route's destination screen is up, signal heart to load the rest (`AccountPreloadRemainingSpaces`), so heart stops bloating disk I/O loading every space up front.

**Architecture:** A new app-singleton `PreferredSpaceIdHolder` is set synchronously and early by whichever cold-start entry knows the route (deeplink via `MainViewModel.handleNewDeepLink`, chat push via `SplashViewModel.onIntentTriggeredByChatPush`). `LaunchAccount` consumes it (falling back to the last-opened space) and passes it through `Command.AccountSelect` into `Rpc.Account.Select.Request`. A new app-singleton `RemainingSpacesPreloader` fires the `AccountPreloadRemainingSpaces` RPC exactly once, 2s after the destination screen is reached, driven from `MainViewModel`.

**Tech Stack:** Kotlin, Clean Architecture (core-models → domain → data → middleware → presentation → app), Dagger 2, Wire-generated protobuf (`anytype.Rpc`), Coroutines, JUnit4 + Mockito-kotlin + kotlinx-coroutines-test.

---

## Spec reference

`docs/superpowers/specs/2026-05-18-preferred-space-cold-start-design.md`

## Conventions for every commit

- Branch: `go-7292-preferred-space-cold-start` (already checked out).
- The repo `commit-msg` hook only accepts `DROID-` keys and will reject `GO-7292`. Per the approved decision, **commit with `git commit --no-verify`** and a message prefixed `GO-7292`.
- Module compile reference (from project memory):
  - Pure-Kotlin modules: `./gradlew :core-models:compileKotlin :domain:compileKotlin :data:compileKotlin`
  - Pure-Kotlin tests: `./gradlew :domain:test --tests "<fqcn>"`
  - Android modules (`middleware`, `presentation`, `feature-*`, `app`): `:module:compileDebugKotlin`. **These may fail locally due to the missing `anytype-heart-android` JNI artifact — that is expected and unrelated to this work.** Correctness-critical units live in pure-Kotlin modules and are covered by domain tests.

## Known unknown (resolved at build time)

The generated JNI binding object `service.Service` is part of `anytype-heart-android` and is not present in this checkout. The binding generates one method per RPC, named as the lowercase-first camelCase of the RPC (observed: `Service.accountSelect`, `Service.accountStop`, `Service.setInitialParams`). For the new `Rpc.Account.PreloadRemainingSpaces` RPC the method is therefore expected to be **`Service.accountPreloadRemainingSpaces`**. Task 4 includes a verification note; if a full build reports it unresolved, inspect the actual `service.Service` API and rename to the matching generated method (same casing rule). This does not block pure-Kotlin tasks.

---

## File structure

| File | Responsibility | Task |
|---|---|---|
| `core-models/.../core_models/Command.kt` | `AccountSelect.preferredSpaceId` field | 1 |
| `middleware/.../interactor/Middleware.kt` | Map `preferredSpaceId` into `Rpc.Account.Select.Request`; add `accountPreloadRemainingSpaces()` | 1, 4 |
| `domain/.../launch/PreferredSpaceIdHolder.kt` | New app-singleton holding the preferred space id | 2 |
| `app/.../di/main/ConfigModule.kt` | Provide `PreferredSpaceIdHolder` + `RemainingSpacesPreloader` singletons | 2, 5 |
| `domain/.../auth/interactor/LaunchAccount.kt` | Resolve preferred space (holder → last-opened → null) | 3 |
| `app/.../di/feature/SplashDi.kt` | Pass holder into `LaunchAccount` provider | 3 |
| `middleware/.../service/MiddlewareService.kt` + `MiddlewareServiceImplementation.kt` | `accountPreloadRemainingSpaces` RPC | 4 |
| `data/.../auth/repo/AuthRemote.kt`, `AuthRemoteDataStore.kt`, `AuthDataRepository.kt` | Repo plumbing for preload | 4 |
| `middleware/.../auth/AuthMiddleware.kt` | `AuthRemote.preloadRemainingSpaces` impl | 4 |
| `domain/.../auth/repo/AuthRepository.kt` | `preloadRemainingSpaces()` contract | 4 |
| `domain/.../auth/interactor/PreloadRemainingSpaces.kt` | New use case | 4 |
| `domain/.../launch/RemainingSpacesPreloader.kt` | One-shot delayed trigger of the preload use case | 5 |
| `domain/.../misc/DeepLinkResolverExtensions.kt` | Extract preferred space id from a `DeepLinkResolver.Action` | 6 |
| `presentation/.../main/MainViewModel.kt` + `MainViewModelFactory.kt`, `app/.../di/feature/MainEntryDI.kt` | Set holder from deeplink; fire preloader | 7 |
| `presentation/.../splash/SplashViewModel.kt` + `SplashViewModelFactory.kt`, `app/.../di/feature/SplashDi.kt` | Set holder from chat push | 8 |
| `domain/src/test/.../*` | Unit tests for holder, use case, preloader, extension, LaunchAccount | 2–6 |

---

## Task 1: Add `preferredSpaceId` to `Command.AccountSelect` and map it in Middleware

**Files:**
- Modify: `core-models/src/main/java/com/anytypeio/anytype/core_models/Command.kt:23-29`
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/Middleware.kt:123-131`

- [ ] **Step 1: Add the field to `Command.AccountSelect`**

Replace lines 23-29 of `Command.kt`:

```kotlin
    data class AccountSelect(
        val id: String,
        val path: String,
        val networkMode: NetworkMode = NetworkMode.DEFAULT,
        val networkConfigFilePath: String? = null,
        val preferYamuxTransport: Boolean? = null,
        val preferredSpaceId: String? = null
    ) : Command()
```

- [ ] **Step 2: Map it into the RPC request**

In `Middleware.kt`, the `accountSelect` function builds `Rpc.Account.Select.Request(...)` (around lines 123-131, between the two `### accountSelect` Timber lines added on this branch — leave those Timber lines untouched). Add the `preferredSpaceId` argument to that constructor call so it reads:

```kotlin
        val request = Rpc.Account.Select.Request(
            id = command.id,
            rootPath = command.path,
            networkMode = networkMode,
            networkCustomConfigFilePath = networkCustomConfigFilePath,
            preferYamuxTransport = command.preferYamuxTransport ?: false,
            enableMembershipV2 = true,
            preferredSpaceId = command.preferredSpaceId.orEmpty()
        )
```

- [ ] **Step 3: Compile pure-Kotlin core**

Run: `./gradlew :core-models:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add core-models/src/main/java/com/anytypeio/anytype/core_models/Command.kt \
        middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/Middleware.kt
git commit --no-verify -m "GO-7292 Pass preferredSpaceId through AccountSelect command"
```

---

## Task 2: `PreferredSpaceIdHolder` app-singleton

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/launch/PreferredSpaceIdHolder.kt`
- Create: `domain/src/test/java/com/anytypeio/anytype/domain/launch/PreferredSpaceIdHolderTest.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/main/ConfigModule.kt` (near the existing `awaitAccountStartedManager` provider, lines ~73-76)

- [ ] **Step 1: Write the failing test**

Create `PreferredSpaceIdHolderTest.kt`:

```kotlin
package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PreferredSpaceIdHolderTest {

    private val holder: PreferredSpaceIdHolder = PreferredSpaceIdHolder.Default

    @Test
    fun `consume returns null when nothing was set`() {
        holder.clear()
        assertNull(holder.consume())
    }

    @Test
    fun `consume returns the set value then clears it`() {
        holder.set("space-1")
        assertEquals("space-1", holder.consume())
        assertNull(holder.consume())
    }

    @Test
    fun `set overwrites previous value`() {
        holder.set("space-1")
        holder.set("space-2")
        assertEquals("space-2", holder.consume())
    }

    @Test
    fun `clear removes the value`() {
        holder.set("space-1")
        holder.clear()
        assertNull(holder.consume())
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolderTest"`
Expected: FAIL — unresolved reference `PreferredSpaceIdHolder`.

- [ ] **Step 3: Implement `PreferredSpaceIdHolder`**

Create `domain/src/main/java/com/anytypeio/anytype/domain/launch/PreferredSpaceIdHolder.kt`:

```kotlin
package com.anytypeio.anytype.domain.launch

/**
 * Holds the space id the user is cold-starting into (deeplink / chat push),
 * so [com.anytypeio.anytype.domain.auth.interactor.LaunchAccount] can pass it
 * as AccountSelect.preferredSpaceId and heart can defer loading other spaces.
 *
 * Set as early and synchronously as possible by the cold-start entry point;
 * consumed exactly once by LaunchAccount. In-memory, app-scoped singleton.
 */
interface PreferredSpaceIdHolder {

    fun set(spaceId: String)

    /** Returns the held value and clears it. Null if nothing was set. */
    fun consume(): String?

    fun clear()

    object Default : PreferredSpaceIdHolder {
        @Volatile
        private var spaceId: String? = null

        override fun set(spaceId: String) {
            this.spaceId = spaceId
        }

        override fun consume(): String? {
            val current = spaceId
            spaceId = null
            return current
        }

        override fun clear() {
            spaceId = null
        }
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolderTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Provide it as a Dagger singleton**

In `ConfigModule.kt`, add an import `import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder` and, next to the existing `awaitAccountStartedManager()` provider, add:

```kotlin
    @JvmStatic
    @Provides
    @Singleton
    fun preferredSpaceIdHolder(): PreferredSpaceIdHolder = PreferredSpaceIdHolder.Default
```

- [ ] **Step 6: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/launch/PreferredSpaceIdHolder.kt \
        domain/src/test/java/com/anytypeio/anytype/domain/launch/PreferredSpaceIdHolderTest.kt \
        app/src/main/java/com/anytypeio/anytype/di/main/ConfigModule.kt
git commit --no-verify -m "GO-7292 Add PreferredSpaceIdHolder app singleton"
```

---

## Task 3: `LaunchAccount` resolves the preferred space

Priority: `PreferredSpaceIdHolder.consume()` → `settings.getCurrentSpace()?.id` → `null`.

**Files:**
- Modify: `domain/src/main/java/com/anytypeio/anytype/domain/auth/interactor/LaunchAccount.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/feature/SplashDi.kt` (provider `provideLaunchAccountUseCase`, lines ~74-90)
- Create: `domain/src/test/java/com/anytypeio/anytype/domain/auth/LaunchAccountTest.kt`

- [ ] **Step 1: Write the failing test**

Create `LaunchAccountTest.kt` (mirrors `CreateAccountTest` mocking style):

```kotlin
package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.core_models.StubAccountSetup
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchAccountTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository
    @Mock lateinit var pathProvider: PathProvider
    @Mock lateinit var configStorage: ConfigStorage
    @Mock lateinit var spaceManager: SpaceManager
    @Mock lateinit var initialParamsProvider: InitialParamsProvider
    @Mock lateinit var settings: UserSettingsRepository
    @Mock lateinit var awaitAccountStartManager: AwaitAccountStartManager

    private lateinit var launchAccount: LaunchAccount

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        PreferredSpaceIdHolder.Default.clear()
        launchAccount = LaunchAccount(
            repository = repo,
            pathProvider = pathProvider,
            configStorage = configStorage,
            spaceManager = spaceManager,
            initialParamsProvider = initialParamsProvider,
            settings = settings,
            awaitAccountStartManager = awaitAccountStartManager,
            preferredSpaceIdHolder = PreferredSpaceIdHolder.Default,
            context = rule.dispatcher
        )
    }

    private suspend fun stubCommon() {
        val setup = StubAccountSetup()
        repo.stub {
            onBlocking { getNetworkMode() } doReturn NetworkModeConfig(NetworkMode.DEFAULT)
            onBlocking { getCurrentAccountId() } doReturn "account-1"
            onBlocking { selectAccount(any()) } doReturn setup
        }
        pathProvider.stub { onBlocking { providePath() } doReturn "/path" }
    }

    private suspend fun capturedCommand(): Command.AccountSelect {
        val captor = argumentCaptor<Command.AccountSelect>()
        verify(repo).selectAccount(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `uses holder value when present`() = runTest {
        stubCommon()
        PreferredSpaceIdHolder.Default.set("deeplink-space")
        settings.stub { onBlocking { getCurrentSpace() } doReturn SpaceId("last-space") }

        launchAccount.run(com.anytypeio.anytype.domain.base.BaseUseCase.None)

        assertEquals("deeplink-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `falls back to last opened space when holder empty`() = runTest {
        stubCommon()
        settings.stub { onBlocking { getCurrentSpace() } doReturn SpaceId("last-space") }

        launchAccount.run(com.anytypeio.anytype.domain.base.BaseUseCase.None)

        assertEquals("last-space", capturedCommand().preferredSpaceId)
    }

    @Test
    fun `preferredSpaceId is null when neither holder nor last space present`() = runTest {
        stubCommon()
        settings.stub { onBlocking { getCurrentSpace() } doReturn null }

        launchAccount.run(com.anytypeio.anytype.domain.base.BaseUseCase.None)

        assertEquals(null, capturedCommand().preferredSpaceId)
    }
}
```

> Note: if `StubAccountSetup()` requires arguments in this codebase, construct the `AccountSetup` the same way `CreateAccountTest`/`ResumeAccountTest` do (check those files and copy the exact stub call). If `getCurrentSpace()` returns a type other than `SpaceId?`, adjust the stub to that type — read `UserSettingsRepository.getCurrentSpace()` signature and match it; `LaunchAccount` already uses `settings.getCurrentSpace()?.id`, so `.id` must resolve.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.auth.LaunchAccountTest"`
Expected: FAIL — `LaunchAccount` has no `preferredSpaceIdHolder` constructor parameter.

- [ ] **Step 3: Implement the resolution in `LaunchAccount`**

Edit `LaunchAccount.kt`. Add import:

```kotlin
import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
```

Add the constructor parameter (after `awaitAccountStartManager`, before `context`):

```kotlin
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val preferredSpaceIdHolder: PreferredSpaceIdHolder,
    context: CoroutineContext = Dispatchers.IO
```

In `run()`, after `val currentAccountId = repository.getCurrentAccountId()` and before building `command`, add:

```kotlin
        val preferredSpaceId = preferredSpaceIdHolder.consume()
            ?: settings.getCurrentSpace()?.id
```

Add `preferredSpaceId = preferredSpaceId` to the `Command.AccountSelect(...)` constructor:

```kotlin
        val command = Command.AccountSelect(
            id = currentAccountId,
            path = pathProvider.providePath(),
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath,
            preferredSpaceId = preferredSpaceId
        )
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.auth.LaunchAccountTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Wire the new dependency into the DI provider**

In `SplashDi.kt`, `provideLaunchAccountUseCase` (lines ~74-90): add an `import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder`, add `preferredSpaceIdHolder: PreferredSpaceIdHolder` to the function parameters, and pass `preferredSpaceIdHolder = preferredSpaceIdHolder` into the `LaunchAccount(...)` constructor call. Final form:

```kotlin
    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        configStorage: ConfigStorage,
        spaceManager: SpaceManager,
        initialParamsProvider: InitialParamsProvider,
        userSettings: UserSettingsRepository,
        awaitAccountStartManager: AwaitAccountStartManager,
        preferredSpaceIdHolder: PreferredSpaceIdHolder
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        configStorage = configStorage,
        spaceManager = spaceManager,
        initialParamsProvider = initialParamsProvider,
        settings = userSettings,
        awaitAccountStartManager = awaitAccountStartManager,
        preferredSpaceIdHolder = preferredSpaceIdHolder
    )
```

- [ ] **Step 6: Compile domain**

Run: `./gradlew :domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/auth/interactor/LaunchAccount.kt \
        domain/src/test/java/com/anytypeio/anytype/domain/auth/LaunchAccountTest.kt \
        app/src/main/java/com/anytypeio/anytype/di/feature/SplashDi.kt
git commit --no-verify -m "GO-7292 Resolve preferred space in LaunchAccount"
```

---

## Task 4: `AccountPreloadRemainingSpaces` RPC + use case

**Files:**
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/service/MiddlewareService.kt` (ACCOUNT region, after `accountSelect`, line ~51)
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/service/MiddlewareServiceImplementation.kt` (after `accountSelect` impl, ~line 130)
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/Middleware.kt` (add a function near `accountSelect`)
- Modify: `data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthRemote.kt`
- Modify: `data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthRemoteDataStore.kt`
- Modify: `data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthDataRepository.kt`
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/auth/AuthMiddleware.kt`
- Modify: `domain/src/main/java/com/anytypeio/anytype/domain/auth/repo/AuthRepository.kt`
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/auth/interactor/PreloadRemainingSpaces.kt`
- Create: `domain/src/test/java/com/anytypeio/anytype/domain/auth/PreloadRemainingSpacesTest.kt`

- [ ] **Step 1: Write the failing use-case test**

Create `PreloadRemainingSpacesTest.kt`:

```kotlin
package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PreloadRemainingSpacesTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository

    private lateinit var dispatchers: AppCoroutineDispatchers
    private lateinit var useCase: PreloadRemainingSpaces

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        useCase = PreloadRemainingSpaces(dispatchers, repo)
    }

    @Test
    fun `delegates to repository on success`() = runTest {
        useCase.async(Unit)
        verify(repo).preloadRemainingSpaces()
    }

    @Test
    fun `surfaces repository failure as Resultat Failure`() = runTest {
        repo.stub { onBlocking { preloadRemainingSpaces() } doThrow RuntimeException("ACCOUNT_IS_NOT_RUNNING") }
        val result = useCase.async(Unit)
        assertTrue(result is Resultat.Failure)
    }
}
```

> Note: confirm the `Resultat` import path matches the codebase (`ResultInteractor.async` returns `Resultat<R>` per `domain/.../base/Interactor.kt`). If `Resultat` lives in a different package, fix the import to match.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.auth.PreloadRemainingSpacesTest"`
Expected: FAIL — unresolved `PreloadRemainingSpaces` / `AuthRepository.preloadRemainingSpaces`.

- [ ] **Step 3: Add the domain contract**

In `AuthRepository.kt`, add after `suspend fun selectAccount(...)`:

```kotlin
    /**
     * Signals heart that the priority screen is up and the remaining spaces
     * (deferred by AccountSelect.preferredSpaceId) may now be loaded.
     */
    suspend fun preloadRemainingSpaces()
```

- [ ] **Step 4: Create the use case**

Create `domain/src/main/java/com/anytypeio/anytype/domain/auth/interactor/PreloadRemainingSpaces.kt`:

```kotlin
package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Tells heart it may now load the spaces that were deferred by
 * AccountSelect.preferredSpaceId. Failures (e.g. ACCOUNT_IS_NOT_RUNNING)
 * are benign — heart has its own 10s timer fallback — so callers treat
 * a Resultat.Failure as a no-op.
 */
class PreloadRemainingSpaces @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repository: AuthRepository
) : ResultInteractor<Unit, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Unit) {
        repository.preloadRemainingSpaces()
    }
}
```

- [ ] **Step 5: Implement the repo plumbing**

`AuthRemote.kt` — add to the interface (near `selectAccount`):

```kotlin
    suspend fun preloadRemainingSpaces()
```

`AuthRemoteDataStore.kt` — add the override (mirrors the existing `selectAccount` delegate):

```kotlin
    override suspend fun preloadRemainingSpaces() = authRemote.preloadRemainingSpaces()
```

`AuthMiddleware.kt` — add the override (near `selectAccount`):

```kotlin
    override suspend fun preloadRemainingSpaces() {
        middleware.accountPreloadRemainingSpaces()
    }
```

`AuthDataRepository.kt` — add the override (near `selectAccount`):

```kotlin
    override suspend fun preloadRemainingSpaces() {
        factory.remote.preloadRemainingSpaces()
    }
```

- [ ] **Step 6: Add the middleware service method**

`MiddlewareService.kt` — in the ACCOUNT region, after the `accountSelect` declaration:

```kotlin
    @Throws(Exception::class)
    fun accountPreloadRemainingSpaces(
        request: Rpc.Account.PreloadRemainingSpaces.Request
    ): Rpc.Account.PreloadRemainingSpaces.Response
```

`MiddlewareServiceImplementation.kt` — after the `accountSelect` override (mirrors the `accountStop` error-handling shape):

```kotlin
    override fun accountPreloadRemainingSpaces(
        request: Rpc.Account.PreloadRemainingSpaces.Request
    ): Rpc.Account.PreloadRemainingSpaces.Response {
        val encoded = Service.accountPreloadRemainingSpaces(
            Rpc.Account.PreloadRemainingSpaces.Request.ADAPTER.encode(request)
        )
        val response = Rpc.Account.PreloadRemainingSpaces.Response.ADAPTER.decode(encoded)
        val error = response.error
        if (error != null && error.code != Rpc.Account.PreloadRemainingSpaces.Response.Error.Code.NULL) {
            throw Exception(error.description)
        } else {
            return response
        }
    }
```

> Verification note: `Service.accountPreloadRemainingSpaces` is the expected generated binding name (lowercase-first camelCase of the RPC, matching `Service.accountSelect`/`Service.accountStop`). It cannot be statically verified in this checkout (`anytype-heart-android` absent). On a full build, if it is unresolved, inspect the real `service.Service` API and rename to the matching generated method.

`Middleware.kt` — add a wrapper function next to `accountSelect` (uses the already-imported `Rpc` and `measureTimedValue`):

```kotlin
    @Throws(Exception::class)
    fun accountPreloadRemainingSpaces() {
        val request = Rpc.Account.PreloadRemainingSpaces.Request()
        logRequestIfDebug(request)
        val (response, time) = measureTimedValue {
            service.accountPreloadRemainingSpaces(request)
        }
        logResponseIfDebug(response, time)
    }
```

- [ ] **Step 7: Run the use-case test to verify it passes**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.auth.PreloadRemainingSpacesTest"`
Expected: PASS (2 tests).

- [ ] **Step 8: Compile pure-Kotlin modules**

Run: `./gradlew :core-models:compileKotlin :domain:compileKotlin :data:compileKotlin`
Expected: `BUILD SUCCESSFUL`
(`middleware` is Android and may not compile locally without the JNI artifact — that is expected; the change there mirrors existing patterns.)

- [ ] **Step 9: Commit**

```bash
git add middleware/src/main/java/com/anytypeio/anytype/middleware/service/MiddlewareService.kt \
        middleware/src/main/java/com/anytypeio/anytype/middleware/service/MiddlewareServiceImplementation.kt \
        middleware/src/main/java/com/anytypeio/anytype/middleware/interactor/Middleware.kt \
        middleware/src/main/java/com/anytypeio/anytype/middleware/auth/AuthMiddleware.kt \
        data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthRemote.kt \
        data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthRemoteDataStore.kt \
        data/src/main/java/com/anytypeio/anytype/data/auth/repo/AuthDataRepository.kt \
        domain/src/main/java/com/anytypeio/anytype/domain/auth/repo/AuthRepository.kt \
        domain/src/main/java/com/anytypeio/anytype/domain/auth/interactor/PreloadRemainingSpaces.kt \
        domain/src/test/java/com/anytypeio/anytype/domain/auth/PreloadRemainingSpacesTest.kt
git commit --no-verify -m "GO-7292 Add AccountPreloadRemainingSpaces RPC and use case"
```

---

## Task 5: `RemainingSpacesPreloader` one-shot trigger

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/launch/RemainingSpacesPreloader.kt`
- Create: `domain/src/test/java/com/anytypeio/anytype/domain/launch/RemainingSpacesPreloaderTest.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/main/ConfigModule.kt`

- [ ] **Step 1: Write the failing test**

Create `RemainingSpacesPreloaderTest.kt`:

```kotlin
package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class RemainingSpacesPreloaderTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository

    private lateinit var preloader: RemainingSpacesPreloader

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        preloader = RemainingSpacesPreloader(PreloadRemainingSpaces(dispatchers, repo))
    }

    @Test
    fun `fires the use case exactly once despite multiple scheduleOnce calls`() = runTest {
        preloader.scheduleOnce(this, delayMillis = 10)
        preloader.scheduleOnce(this, delayMillis = 10)
        preloader.scheduleOnce(this, delayMillis = 10)
        advanceUntilIdle()
        verify(repo, times(1)).preloadRemainingSpaces()
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.launch.RemainingSpacesPreloaderTest"`
Expected: FAIL — unresolved `RemainingSpacesPreloader`.

- [ ] **Step 3: Implement `RemainingSpacesPreloader`**

Create `domain/src/main/java/com/anytypeio/anytype/domain/launch/RemainingSpacesPreloader.kt`:

```kotlin
package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fires AccountPreloadRemainingSpaces exactly once per process, after a short
 * delay following the moment the cold-start route's destination screen is
 * reached. Idempotent: safe to call scheduleOnce from multiple navigation
 * paths. Heart's own 10s timer is the correctness backstop, so a swallowed
 * failure only loses optimization.
 */
@Singleton
class RemainingSpacesPreloader @Inject constructor(
    private val preloadRemainingSpaces: PreloadRemainingSpaces
) {
    private val triggered = AtomicBoolean(false)

    fun scheduleOnce(scope: CoroutineScope, delayMillis: Long = DEFAULT_DELAY_MILLIS) {
        if (!triggered.compareAndSet(false, true)) return
        scope.launch {
            delay(delayMillis)
            preloadRemainingSpaces.async(Unit)
        }
    }

    companion object {
        const val DEFAULT_DELAY_MILLIS = 2_000L
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.launch.RemainingSpacesPreloaderTest"`
Expected: PASS (1 test).

- [ ] **Step 5: Provide it as a Dagger singleton**

In `ConfigModule.kt`, add imports `import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces`, `import com.anytypeio.anytype.domain.auth.repo.AuthRepository`, `import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers`, `import com.anytypeio.anytype.domain.launch.RemainingSpacesPreloader` (only those not already imported), and add:

```kotlin
    @JvmStatic
    @Provides
    @Singleton
    fun remainingSpacesPreloader(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): RemainingSpacesPreloader = RemainingSpacesPreloader(
        PreloadRemainingSpaces(dispatchers, repo)
    )
```

> If `ConfigModule` cannot see `AuthRepository`/`AppCoroutineDispatchers` (different component graph), instead place this `@Provides` in `DataModule.kt`, where `AuthRepository` is already provided. Either module works; pick the one whose graph already has both dependencies.

- [ ] **Step 6: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/launch/RemainingSpacesPreloader.kt \
        domain/src/test/java/com/anytypeio/anytype/domain/launch/RemainingSpacesPreloaderTest.kt \
        app/src/main/java/com/anytypeio/anytype/di/main/ConfigModule.kt
git commit --no-verify -m "GO-7292 Add RemainingSpacesPreloader one-shot trigger"
```

---

## Task 6: `DeepLinkResolver.Action.preferredSpaceId()` extension

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/misc/DeepLinkResolverExtensions.kt`
- Create: `domain/src/test/java/com/anytypeio/anytype/domain/misc/DeepLinkResolverExtensionsTest.kt`

- [ ] **Step 1: Write the failing test**

Create `DeepLinkResolverExtensionsTest.kt`:

```kotlin
package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.preferredSpaceId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkResolverExtensionsTest {

    @Test
    fun `DeepLinkToObject yields its space id`() {
        val action = DeepLinkResolver.Action.DeepLinkToObject(
            obj = "obj-1",
            space = SpaceId("space-1")
        )
        assertEquals("space-1", action.preferredSpaceId())
    }

    @Test
    fun `OsWidget DeepLinkToSpace yields its space id`() {
        val action = DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace(
            space = SpaceId("space-2")
        )
        assertEquals("space-2", action.preferredSpaceId())
    }

    @Test
    fun `OsWidget DeepLinkToObject yields its space id`() {
        val action = DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToObject(
            obj = "obj-2",
            space = SpaceId("space-3")
        )
        assertEquals("space-3", action.preferredSpaceId())
    }

    @Test
    fun `Invite has no preferred space`() {
        assertNull(DeepLinkResolver.Action.Invite("link").preferredSpaceId())
    }

    @Test
    fun `InitiateOneToOneChat has no preferred space`() {
        val action = DeepLinkResolver.Action.InitiateOneToOneChat(
            identity = "id-1",
            metadataKey = "key-1"
        )
        assertNull(action.preferredSpaceId())
    }
}
```

> Note: confirm constructor argument names of the `DeepLinkResolver.Action` variants against `domain/.../misc/DeepLinkResolver.kt` (the spec trace shows `DeepLinkToObject(obj, space, invite?)`, `OsWidgetDeepLink.DeepLinkToSpace(space)`, `OsWidgetDeepLink.DeepLinkToObject(obj, space)`, `InitiateOneToOneChat(identity, metadataKey)`, `Invite(link)`). Adjust the test constructors if names differ.

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.misc.DeepLinkResolverExtensionsTest"`
Expected: FAIL — unresolved `preferredSpaceId`.

- [ ] **Step 3: Implement the extension**

Create `domain/src/main/java/com/anytypeio/anytype/domain/misc/DeepLinkResolverExtensions.kt`:

```kotlin
package com.anytypeio.anytype.domain.misc

/**
 * The space id a cold-start route points into, if any. Used to prime
 * AccountSelect.preferredSpaceId. Routes without an inherent space
 * (invites, one-to-one chat by identity) return null.
 */
fun DeepLinkResolver.Action.preferredSpaceId(): String? = when (this) {
    is DeepLinkResolver.Action.DeepLinkToObject -> space.id
    is DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace -> space.id
    is DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToObject -> space.id
    else -> null
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :domain:test --tests "com.anytypeio.anytype.domain.misc.DeepLinkResolverExtensionsTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/misc/DeepLinkResolverExtensions.kt \
        domain/src/test/java/com/anytypeio/anytype/domain/misc/DeepLinkResolverExtensionsTest.kt
git commit --no-verify -m "GO-7292 Add DeepLinkResolver.Action.preferredSpaceId extension"
```

---

## Task 7: Wire holder-set (deeplink) + preloader trigger into `MainViewModel`

`MainViewModel` gets two new constructor deps: `preferredSpaceIdHolder: PreferredSpaceIdHolder`, `remainingSpacesPreloader: RemainingSpacesPreloader`. It (a) sets the holder synchronously at the top of `handleNewDeepLink`, and (b) fires `remainingSpacesPreloader.scheduleOnce(viewModelScope)` from the existing cold-start `awaitStart` collector (covers all cold starts) and at the end of `proceedWithNewDeepLink` (tighter for the deeplink path; deduped by the one-shot guard).

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/main/MainViewModel.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/main/MainViewModelFactory.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/feature/MainEntryDI.kt`

- [ ] **Step 1: Add the constructor dependencies to `MainViewModel`**

In `MainViewModel.kt`, add imports:

```kotlin
import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
import com.anytypeio.anytype.domain.launch.RemainingSpacesPreloader
import com.anytypeio.anytype.domain.misc.preferredSpaceId
```

Add to the primary constructor, right after `private val setInitialParams: SetInitialParams`:

```kotlin
    private val setInitialParams: SetInitialParams,
    private val preferredSpaceIdHolder: PreferredSpaceIdHolder,
    private val remainingSpacesPreloader: RemainingSpacesPreloader
```

- [ ] **Step 2: Set the holder synchronously in `handleNewDeepLink`**

`handleNewDeepLink(deeplink)` currently starts with `deepLinkJobs.cancel()` then `viewModelScope.launch { ... }`. Add the holder set as the very first statement (synchronous, before the cancel/launch), so it is set as early as possible on the cold-start deeplink path:

```kotlin
    fun handleNewDeepLink(deeplink: DeepLinkResolver.Action) {
        deeplink.preferredSpaceId()?.let { preferredSpaceIdHolder.set(it) }
        deepLinkJobs.cancel()
        viewModelScope.launch {
            // ... existing body unchanged
        }
    }
```

- [ ] **Step 3: Fire the preloader on every cold start**

In the existing cold-start collector block (the `awaitAccountStartManager.awaitStart().take(1).collect { ... }` that reads `pendingIntentStore.getPushSpaceEntry()`), add — as the first statement inside the `collect { }` lambda, before the `pendingPush` logic:

```kotlin
                remainingSpacesPreloader.scheduleOnce(viewModelScope)
```

- [ ] **Step 4: Fire the preloader at the end of deeplink processing**

At the very end of `proceedWithNewDeepLink(deeplink)` (after the closing of the `when (deeplink) { ... }`), add:

```kotlin
        remainingSpacesPreloader.scheduleOnce(viewModelScope)
```

- [ ] **Step 5: Cascade through the factory**

`MainViewModelFactory.kt` — add the same two `private val` parameters to the `@Inject constructor` (after `setInitialParams`) with imports `import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder` and `import com.anytypeio.anytype.domain.launch.RemainingSpacesPreloader`, and pass them into the `MainViewModel(...)` construction in `create()`:

```kotlin
        setInitialParams = setInitialParams,
        preferredSpaceIdHolder = preferredSpaceIdHolder,
        remainingSpacesPreloader = remainingSpacesPreloader
```

- [ ] **Step 6: Cascade through `MainEntryModule`**

`MainEntryDI.kt` — in `provideMainViewModelFactory`, add the two parameters (after `setInitialParams: SetInitialParams`) and pass them into the `MainViewModelFactory(...)` call:

```kotlin
        setInitialParams = setInitialParams,
        preferredSpaceIdHolder = preferredSpaceIdHolder,
        remainingSpacesPreloader = remainingSpacesPreloader
```

Add parameters to the function signature:

```kotlin
        debugRunProfiler: DebugRunProfiler,
        setInitialParams: SetInitialParams,
        preferredSpaceIdHolder: PreferredSpaceIdHolder,
        remainingSpacesPreloader: RemainingSpacesPreloader
```

with imports `import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder` and `import com.anytypeio.anytype.domain.launch.RemainingSpacesPreloader`. Both are provided as `@Singleton` (Tasks 2 and 5) so Dagger resolves them automatically.

- [ ] **Step 7: Compile presentation (best-effort)**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL` if the JNI artifact is available; otherwise verify the diff matches this plan exactly and rely on CI. The pure-Kotlin deps (`:domain`) must still compile: `./gradlew :domain:compileKotlin` → `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/main/MainViewModel.kt \
        presentation/src/main/java/com/anytypeio/anytype/presentation/main/MainViewModelFactory.kt \
        app/src/main/java/com/anytypeio/anytype/di/feature/MainEntryDI.kt
git commit --no-verify -m "GO-7292 Set preferred space from deeplink and trigger preload in MainViewModel"
```

---

## Task 8: Wire holder-set (chat push) into `SplashViewModel`

The chat-push cold start enters via `SplashViewModel.onIntentTriggeredByChatPush(space, chat)` (line ~290), which already has the target space id. Set the holder synchronously at the top of that function (before `viewModelScope.launch`), so it is available to `LaunchAccount` regardless of coroutine ordering.

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/splash/SplashViewModel.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/splash/SplashViewModelFactory.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/feature/SplashDi.kt`

- [ ] **Step 1: Add the dependency to `SplashViewModel`**

Add import `import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder` and a constructor parameter `private val preferredSpaceIdHolder: PreferredSpaceIdHolder` (place it alongside the existing injected use cases in the `SplashViewModel` primary constructor).

- [ ] **Step 2: Set the holder at the top of `onIntentTriggeredByChatPush`**

```kotlin
    fun onIntentTriggeredByChatPush(space: Id, chat: Id) {
        preferredSpaceIdHolder.set(space)
        viewModelScope.launch {
            spaceManager.set(space = space)
                // ... existing body unchanged
        }
    }
```

- [ ] **Step 3: Cascade through `SplashViewModelFactory`**

Add `private val preferredSpaceIdHolder: PreferredSpaceIdHolder` to the `@Inject constructor`, the import, and pass `preferredSpaceIdHolder = preferredSpaceIdHolder` into the `SplashViewModel(...)` construction in `create()`.

- [ ] **Step 4: Cascade through `SplashDi`**

If `SplashViewModelFactory` is constructed via `@Inject` (it is — `@Inject constructor`), no extra `@Provides` is needed; Dagger injects the `@Singleton PreferredSpaceIdHolder` from Task 2 automatically. Verify `SplashDi`'s component graph can see the `ConfigModule` provider (it can — `PreferredSpaceIdHolder` is `@Singleton` in the app graph). No code change in `SplashDi` unless a missing-binding compile error appears, in which case add:

```kotlin
    @JvmStatic
    @PerScreen
    @Provides
    fun providePreferredSpaceIdHolder(holder: PreferredSpaceIdHolder): PreferredSpaceIdHolder = holder
```

(only if required by the graph).

- [ ] **Step 5: Compile (best-effort) + domain sanity**

Run: `./gradlew :domain:compileKotlin`
Expected: `BUILD SUCCESSFUL`
Run: `./gradlew :presentation:compileDebugKotlin` (best-effort; see Task 7 Step 7 caveat).

- [ ] **Step 6: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/splash/SplashViewModel.kt \
        presentation/src/main/java/com/anytypeio/anytype/presentation/splash/SplashViewModelFactory.kt \
        app/src/main/java/com/anytypeio/anytype/di/feature/SplashDi.kt
git commit --no-verify -m "GO-7292 Set preferred space from chat push in SplashViewModel"
```

---

## Task 9: Final verification

- [ ] **Step 1: Run the full domain test suite for the new units**

Run:
```bash
./gradlew :domain:test --tests "com.anytypeio.anytype.domain.launch.*" \
                       --tests "com.anytypeio.anytype.domain.auth.LaunchAccountTest" \
                       --tests "com.anytypeio.anytype.domain.auth.PreloadRemainingSpacesTest" \
                       --tests "com.anytypeio.anytype.domain.misc.DeepLinkResolverExtensionsTest"
```
Expected: `BUILD SUCCESSFUL`, all tests passing.

- [ ] **Step 2: Compile pure-Kotlin layers end-to-end**

Run: `./gradlew :core-models:compileKotlin :domain:compileKotlin :data:compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Full build (best-effort / CI)**

Run: `./gradlew assembleDebug` if the `anytype-heart-android` artifact is available locally; otherwise push the branch and rely on CI. Confirm `Service.accountPreloadRemainingSpaces` resolves (see Task 4 verification note) — if CI reports it unresolved, rename to the actual generated binding method and amend Task 4's commit.

- [ ] **Step 4: Manual smoke test (device/emulator, if available)**

1. Cold-start via a deeplink into a non-default space → confirm (logs) `AccountSelect` is sent with the deeplink's `preferredSpaceId`, and `AccountPreloadRemainingSpaces` is sent ~2s after the destination screen appears.
2. Cold-start with no deeplink → confirm `preferredSpaceId` equals the last-opened space; preload fires ~2s after the home/vault screen appears.
3. Cold-start from a chat push → confirm `preferredSpaceId` equals the push target space.

- [ ] **Step 5: Final commit (only if Step 4 required adjustments)**

```bash
git add -A
git commit --no-verify -m "GO-7292 Verification fixups for preferred-space cold start"
```

---

## Self-review

- **Spec coverage:** Piece A (preferredSpaceId): Tasks 1, 3 (LaunchAccount priority holder→settings→null), 7 (deeplink source), 8 (push source); last-opened source = Task 3 fallback. Piece B (preload): Task 4 (RPC + use case), Task 5 (one-shot delayed trigger), Task 7 (trigger sites). Edge cases: race → Task 3 null-fallback test; onboarding untouched (`SelectAccount` not modified); holder cleared on consume (Task 2 `consume()` semantics + test); `ACCOUNT_IS_NOT_RUNNING` benign (Task 4 use-case doc + failure test, Task 5 swallows). 2s delay = `RemainingSpacesPreloader.DEFAULT_DELAY_MILLIS` (Task 5). All spec sections map to a task.
- **Placeholder scan:** No TBD/TODO; every code step has complete code; the one runtime-resolved name (`Service.accountPreloadRemainingSpaces`) has an explicit derivation rule + verification step, not a placeholder.
- **Type consistency:** `PreferredSpaceIdHolder.{set,consume,clear}` used identically in Tasks 2/3/7/8. `RemainingSpacesPreloader.scheduleOnce(scope, delayMillis)` consistent in Tasks 5/7. `Command.AccountSelect.preferredSpaceId: String?` consistent in Tasks 1/3. `AuthRepository.preloadRemainingSpaces()` consistent across Tasks 4 plumbing. `DeepLinkResolver.Action.preferredSpaceId(): String?` consistent in Tasks 6/7.
- **Assumptions flagged inline:** `StubAccountSetup`/`getCurrentSpace()` type, `Resultat` import path, and `DeepLinkResolver.Action` constructor arg names each carry a "confirm against codebase" note in their task.
