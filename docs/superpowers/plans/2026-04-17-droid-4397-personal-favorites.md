# DROID-4397 Personal Favorites Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split channel-wide pinning into two independent mechanisms — a per-user "My Favorites" Unread-style section, and an Owner/Admin-only "Pinned" flat-row block under the Home widget.

**Architecture:** GO-6962 introduces a **per-user, per-space virtual object** at ID `_personalWidgets_<encodedSpaceId>` (first `.` in spaceId replaced with `_`), stored in the user's Tech Space as a CRDT-synced block tree. It uses the **same Widgets API as the shared widgets document** (`Rpc.Block.CreateWidget`, `Rpc.Block.ListDelete`, `Rpc.Block.ListMoveToExistingObject`, `Rpc.Object.Open`, `Rpc.Object.Close`) — just with a different `contextId`. No dedicated "favorite" RPCs exist. Personal favorites are modeled as Link-layout widgets in this virtual doc whose inner `targetBlockId` is the favorited object's ID; the order of wrapper children under root determines display order. A new widget container (`PersonalFavoritesWidgetContainer`) opens the personal-widgets doc the same way `HomeScreenViewModel` opens the shared widgets doc (via `OpenObject` + `InterceptEvents`), walks the block tree, filters to real-object link targets, and emits Unread-style compact rows. The existing Pinned-widget-section mechanism is re-used for channel pins, with rendering changed to flat object rows (no section header) and its mutating actions gated to Owner/Admin via `UserPermissionProvider`. All action surfaces (object `...` menu, widget long-press menu) get a Favorite/Unfavorite action (star icon, all roles) and Pin/Unpin-to-channel (pin icon, Owner/Admin only).

**Desktop reference:** `anyproto/anytype-ts#2161` (merged 2026-04-21). Key patterns: `U.Object.getPersonalWidgetsId(space)` helper; `C.ObjectOpen(personalId, '', space)` on space init; `createWidgetFromObjectIn(personalId, personalId, objectId, '', BlockPosition.InnerFirst)` for adding a favorite (inserts at TOP — matches desktop, contradicts the older Linear-spec line "appended to the end"; we match desktop); `C.BlockListDelete(personalId, wrapperIds)` for unfavorite; block moves for reorder.

**Tech Stack:** Kotlin, Dagger 2, Jetpack Compose, Coroutines + Flow, `sh.calvin.reorderable` for drag-and-drop, Mockito + Turbine for tests, protobuf via Go middleware.

**Middleware status:** GO-6962 merged into `anytype-heart` as v0.50.0-rc02 (the merge commit IS the tag). This branch already pins `middlewareVersion = "v0.50.0-rc02"` in `gradle/libs.versions.toml`, so the full RPC surface is available. No `make update_mw` needed; no new `MiddlewareService` wrappers needed — `objectOpen`, `blockCreateWidget`, `blockListDelete`, `blockListMoveToExistingObject`, `objectClose` already exist.

**Vocabulary:** The Linear issue says "channel"; in this repo it is a **Space** (`SpaceId`). The widget screen is scoped by `SpaceId`. There is no per-Chat scoping — favorites and pins live at the Space level.

---

## File Structure

**Create:**
- `domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalWidgetsId.kt` — helper `fun personalWidgetsId(space: SpaceId): Id` that returns `"_personalWidgets_" + spaceId.replaceFirst('.', '_')`
- `domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalFavoritesRepository.kt` — repo interface (3 methods: add, remove, reorder). Observation is container-owned — see Task 12.
- `domain/src/main/java/com/anytypeio/anytype/domain/favorites/AddPersonalFavorite.kt` — use case
- `domain/src/main/java/com/anytypeio/anytype/domain/favorites/RemovePersonalFavorite.kt` — use case
- `domain/src/main/java/com/anytypeio/anytype/domain/favorites/ReorderPersonalFavorites.kt` — use case
- `data/src/main/java/com/anytypeio/anytype/data/auth/repo/favorites/PersonalFavoritesDataRepository.kt` — `PersonalFavoritesRepository` impl built on top of `BlockRepository` (`createWidget` / `openObject` / `unlink` / `move`). No separate remote-data-source or middleware class is needed; the existing `BlockMiddleware` → `Middleware` → `MiddlewareService` chain already covers all four RPCs.
- `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/PersonalFavoritesWidgetContainer.kt` — new container, Unread-style compact rows. Opens the personal-widgets doc via `OpenObject` + `InterceptEvents` (mirroring the shared-widgets pipeline in `HomeScreenViewModel`), walks the block tree, emits ordered rows.
- `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/Widget.kt` — add `Widget.PersonalFavorites` data class (modified, see below)
- `app/src/main/java/com/anytypeio/anytype/ui/home/PersonalFavoritesSection.kt` — `MyFavoritesSectionHeader` + compact row rendering
- Tests mirroring each new domain/presentation file

**Modify:**
- `core-models/src/main/java/com/anytypeio/anytype/core_models/SectionSettings.kt` — add `MY_FAVORITES` to `WidgetSectionType`
- `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/Widget.kt` — add `SectionType.MY_FAVORITES`
- `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetContainerDelegate.kt` — instantiate `PersonalFavoritesWidgetContainer`
- `presentation/src/main/java/com/anytypeio/anytype/presentation/home/HomeScreenViewModel.kt` — wire personal-favorites observation
- `presentation/src/main/java/com/anytypeio/anytype/presentation/objects/menu/ObjectMenuOptionsProviderImpl.kt` — emit Favorite/Unfavorite/Pin/Unpin flags
- `presentation/src/main/java/com/anytypeio/anytype/presentation/objects/menu/ObjectMenuViewModel.kt` — handlers for 4 new actions + permission gating
- `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt` — render Pinned as flat rows (no header) + inject My Favorites
- `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetsScreen.kt` — ordering: Home → Pinned rows → Unread → My Favorites → rest
- `app/src/main/java/com/anytypeio/anytype/ui/widgets/menu/WidgetDropDownMenu.kt` — add Favorite/Unfavorite + Pin/Unpin items, gated by role
- `di/` — bind `PersonalFavoritesRepository`, `PersonalFavoritesRemoteDataSource`, use cases, container
- `core-ui/src/main/res/drawable/` — add `ic_star_outline_18.xml`, `ic_star_filled_18.xml`, `ic_pin_18.xml`, `ic_unpin_18.xml` **only if not already present** (check first)

---

## Phase 0 — Personal-Widgets ID Helper

> **Status:** The original Phase 0 was "integrate GO-6962 RPCs". That turned out to be unnecessary — GO-6962 did NOT introduce dedicated personal-favorite RPCs. Instead it added a virtual per-user object (`_personalWidgets_<encodedSpaceId>`) that reuses the existing widgets API. The middleware is already available at v0.50.0-rc02. All we need in Phase 0 is a Kotlin helper to derive the personal-widgets doc ID from a `SpaceId`.

### Task 1: `PersonalWidgetsId` helper

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalWidgetsId.kt`
- Test: `domain/src/test/java/com/anytypeio/anytype/domain/favorites/PersonalWidgetsIdTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.primitives.SpaceId
import org.junit.Test
import kotlin.test.assertEquals

class PersonalWidgetsIdTest {

    @Test
    fun `replaces the first dot with an underscore and prepends the prefix`() {
        val result = personalWidgetsId(SpaceId("bafyreig5abc.2f7dexample"))
        assertEquals("_personalWidgets_bafyreig5abc_2f7dexample", result)
    }

    @Test
    fun `only replaces the first dot when multiple are present`() {
        val result = personalWidgetsId(SpaceId("aaa.bbb.ccc"))
        assertEquals("_personalWidgets_aaa_bbb.ccc", result)
    }

    @Test
    fun `returns prefix plus raw id when spaceId has no dot`() {
        val result = personalWidgetsId(SpaceId("nodot"))
        assertEquals("_personalWidgets_nodot", result)
    }
}
```

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew :domain:testDebugUnitTest --tests "*PersonalWidgetsIdTest*"`
Expected: FAIL (function not found).

- [ ] **Step 3: Implement**

```kotlin
package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

/**
 * Derives the ID of the per-user personal-widgets virtual object for [space].
 *
 * Middleware exposes a per-user, per-space block-tree document stored in the user's
 * Tech Space. Its ID is `_personalWidgets_<encodedSpaceId>`, where the first `.` in
 * the SpaceId is replaced with `_` (the same encoding used for participant IDs).
 * See anytype-heart PR #3092 (GO-6962).
 */
fun personalWidgetsId(space: SpaceId): Id =
    PREFIX + space.id.replaceFirst('.', '_')

private const val PREFIX = "_personalWidgets_"
```

- [ ] **Step 4: Run test — should pass**

Run: `./gradlew :domain:testDebugUnitTest --tests "*PersonalWidgetsIdTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalWidgetsId.kt \
         domain/src/test/java/com/anytypeio/anytype/domain/favorites/PersonalWidgetsIdTest.kt
git commit -m "DROID-4397 Add PersonalWidgetsId helper"
```

---

## Phase 1 — Domain: Repository + Use Cases (TDD)

### Task 2: Define `PersonalFavoritesRepository`

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalFavoritesRepository.kt`

- [ ] **Step 1: Write the interface**

```kotlin
package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface PersonalFavoritesRepository {

    /**
     * Add [target] as a personal favorite in [space].
     * Inserts at the TOP of the list (matches desktop `BlockPosition.InnerFirst`).
     */
    suspend fun add(space: SpaceId, target: Id)

    /**
     * Remove [target] from the user's personal favorites in [space]. No-op if absent.
     */
    suspend fun remove(space: SpaceId, target: Id)

    /**
     * Reorder the full favorites list for [space] to [orderedTargets].
     * Caller owns the complete new order; repository diffs internally and issues
     * block-move RPCs as needed.
     */
    suspend fun reorder(space: SpaceId, orderedTargets: List<Id>)
}
```

> **Why no `observe` method?** Personal favorites live in a virtual block-tree document (`_personalWidgets_<encodedSpaceId>`) that is observed via the same `OpenObject` + `InterceptEvents` pipeline that `HomeScreenViewModel` already uses for the shared widgets doc. The widget container (Task 12) owns that observation and extracts link targets directly from the block tree — there's no separate favorites subscription to wrap in a Flow at the data layer.

- [ ] **Step 2: Build**

Run: `./gradlew :domain:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/favorites/PersonalFavoritesRepository.kt
git commit -m "DROID-4397 Add PersonalFavoritesRepository interface"
```

### Task 3: `AddPersonalFavorite` use case (TDD)

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/favorites/AddPersonalFavorite.kt`
- Test: `domain/src/test/java/com/anytypeio/anytype/domain/favorites/AddPersonalFavoriteTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class AddPersonalFavoriteTest {

    @Mock
    lateinit var repo: PersonalFavoritesRepository

    private lateinit var usecase: AddPersonalFavorite

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        usecase = AddPersonalFavorite(repo)
    }

    @Test
    fun `invokes repo add with params`() = runTest {
        val space = SpaceId(MockDataFactory.randomUuid())
        val target = MockDataFactory.randomUuid()

        usecase.run(AddPersonalFavorite.Params(space, target))

        verify(repo).add(space, target)
    }
}
```

- [ ] **Step 2: Run test — should fail to compile**

Run: `./gradlew :domain:testDebugUnitTest --tests "*AddPersonalFavoriteTest*"`
Expected: FAIL, `AddPersonalFavorite` unresolved reference.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.ResultInteractor
import kotlinx.coroutines.CoroutineDispatcher

class AddPersonalFavorite(
    private val repo: PersonalFavoritesRepository,
    dispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
) : ResultInteractor<AddPersonalFavorite.Params, Unit>(dispatcher) {

    override suspend fun doWork(params: Params) {
        repo.add(params.space, params.target)
    }

    data class Params(val space: SpaceId, val target: Id)
}
```

Note: If `ResultInteractor` doesn't match the project's pattern, use `BaseUseCase` — follow the pattern of `SetObjectListIsFavorite` in `/domain/src/main/java/com/anytypeio/anytype/domain/dashboard/interactor/SetObjectListIsFavorite.kt`.

- [ ] **Step 4: Run test — should pass**

Run: `./gradlew :domain:testDebugUnitTest --tests "*AddPersonalFavoriteTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/favorites/AddPersonalFavorite.kt \
         domain/src/test/java/com/anytypeio/anytype/domain/favorites/AddPersonalFavoriteTest.kt
git commit -m "DROID-4397 Add AddPersonalFavorite use case"
```

### Task 4: `RemovePersonalFavorite` use case (TDD)

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/favorites/RemovePersonalFavorite.kt`
- Test: `domain/src/test/java/com/anytypeio/anytype/domain/favorites/RemovePersonalFavoriteTest.kt`

- [ ] **Step 1: Write the failing test**

Mirror `AddPersonalFavoriteTest` but verify `repo.remove(space, target)`.

```kotlin
@Test
fun `invokes repo remove with params`() = runTest {
    val space = SpaceId(MockDataFactory.randomUuid())
    val target = MockDataFactory.randomUuid()

    usecase.run(RemovePersonalFavorite.Params(space, target))

    verify(repo).remove(space, target)
}
```

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew :domain:testDebugUnitTest --tests "*RemovePersonalFavoriteTest*"`
Expected: FAIL.

- [ ] **Step 3: Implement**

```kotlin
class RemovePersonalFavorite(
    private val repo: PersonalFavoritesRepository,
    dispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
) : ResultInteractor<RemovePersonalFavorite.Params, Unit>(dispatcher) {
    override suspend fun doWork(params: Params) { repo.remove(params.space, params.target) }
    data class Params(val space: SpaceId, val target: Id)
}
```

- [ ] **Step 4: Run test — should pass**

Run: `./gradlew :domain:testDebugUnitTest --tests "*RemovePersonalFavoriteTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/favorites/RemovePersonalFavorite.kt \
         domain/src/test/java/com/anytypeio/anytype/domain/favorites/RemovePersonalFavoriteTest.kt
git commit -m "DROID-4397 Add RemovePersonalFavorite use case"
```

### Task 5: `ReorderPersonalFavorites` use case (TDD)

**Files:**
- Create: `domain/src/main/java/com/anytypeio/anytype/domain/favorites/ReorderPersonalFavorites.kt`
- Test: `domain/src/test/java/com/anytypeio/anytype/domain/favorites/ReorderPersonalFavoritesTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `invokes repo reorder with full ordered list`() = runTest {
    val space = SpaceId(MockDataFactory.randomUuid())
    val order = listOf("a", "b", "c")

    usecase.run(ReorderPersonalFavorites.Params(space, order))

    verify(repo).reorder(space, order)
}
```

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew :domain:testDebugUnitTest --tests "*ReorderPersonalFavoritesTest*"`
Expected: FAIL.

- [ ] **Step 3: Implement**

```kotlin
class ReorderPersonalFavorites(
    private val repo: PersonalFavoritesRepository,
    dispatcher: CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
) : ResultInteractor<ReorderPersonalFavorites.Params, Unit>(dispatcher) {
    override suspend fun doWork(params: Params) { repo.reorder(params.space, params.order) }
    data class Params(val space: SpaceId, val order: List<Id>)
}
```

- [ ] **Step 4: Run test — should pass**
- [ ] **Step 5: Commit**

```bash
git add domain/src/main/java/com/anytypeio/anytype/domain/favorites/ReorderPersonalFavorites.kt \
         domain/src/test/java/com/anytypeio/anytype/domain/favorites/ReorderPersonalFavoritesTest.kt
git commit -m "DROID-4397 Add ReorderPersonalFavorites use case"
```

### Task 6: REMOVED — `ObservePersonalFavorites` use case no longer needed

This task previously defined a Flow-emitting use case wrapping `PersonalFavoritesRepository.observe()`. Both are removed in the revised design: personal favorites are observed by the widget container directly via the shared-widget-object pipeline (`OpenObject` + `InterceptEvents` on the personal-widgets doc, block-tree walk). The container already has access to the full block tree — re-projecting it through a repo-level Flow adds no value and would duplicate the widget-tree reduction logic.

If the existing `ObservePersonalFavorites.kt` + test files are still present in the branch from earlier scaffolding, delete them:

```bash
git rm domain/src/main/java/com/anytypeio/anytype/domain/favorites/ObservePersonalFavorites.kt \
       domain/src/test/java/com/anytypeio/anytype/domain/favorites/ObservePersonalFavoritesTest.kt
./gradlew :domain:compileDebugKotlin :domain:testDebugUnitTest
git commit -m "DROID-4397 Drop ObservePersonalFavorites (container owns personal-widgets block-tree observation)"
```

---

## Phase 2 — Data + Middleware Wiring

### Task 7: Extend `Position` enum with `INNER_FIRST`

> **Why a new enum value?** Desktop inserts new personal favorites at the TOP via `BlockPosition.InnerFirst`. The proto has `Inner = 5` (insert as LAST child) and `InnerFirst = 7` (insert as FIRST child), but the Kotlin `Position` enum in `core-models` only maps `Inner`. We need `INNER_FIRST` to express "insert at top" via the existing `BlockRepository.createWidget` API. The rest of Task 7's work (a dedicated `PersonalFavoritesRemoteDataSource` + `PersonalFavoritesMiddleware`) is collapsed into Task 8 — `PersonalFavoritesDataRepository` depends directly on `BlockRepository`, which already exposes `createWidget`/`openObject`/`unlink`/`move`.

**Files:**
- Modify: `core-models/src/main/java/com/anytypeio/anytype/core_models/Position.kt`
- Modify: `middleware/src/main/java/com/anytypeio/anytype/middleware/mappers/ToMiddlewareModelMappers.kt`

- [ ] **Step 1: Add `INNER_FIRST` to the enum**

```kotlin
enum class Position { NONE, TOP, BOTTOM, LEFT, RIGHT, INNER, INNER_FIRST, REPLACE }
```

- [ ] **Step 2: Map to proto `InnerFirst`**

```kotlin
fun Position.toMiddlewareModel(): MBPosition = when (this) {
    Position.NONE -> MBPosition.None
    Position.TOP -> MBPosition.Top
    Position.BOTTOM -> MBPosition.Bottom
    Position.LEFT -> MBPosition.Left
    Position.RIGHT -> MBPosition.Right
    Position.INNER -> MBPosition.Inner
    Position.INNER_FIRST -> MBPosition.InnerFirst
    Position.REPLACE -> MBPosition.Replace
}
```

- [ ] **Step 3: Build**

Run: `./gradlew :core-models:compileKotlin :middleware:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. No other `when(Position)` expressions exist elsewhere in the codebase, so the change is safe.

- [ ] **Step 4: Commit**

```bash
git add core-models/src/main/java/com/anytypeio/anytype/core_models/Position.kt \
         middleware/src/main/java/com/anytypeio/anytype/middleware/mappers/ToMiddlewareModelMappers.kt
git commit -m "DROID-4397 Add Position.INNER_FIRST for personal-favorites top insert"
```

> **Context (retained for reference):** The middleware layer already wraps `blockCreateWidget`, `blockListDelete`, `blockListMoveToExistingObject`, and `objectOpen` via `MiddlewareService`. These are already exposed through the existing `BlockRepository` interface (`createWidget`, `unlink`, `move`, `openObject`). The data-layer repository consumes `BlockRepository` directly — no new `PersonalFavoritesRemoteDataSource`/`PersonalFavoritesMiddleware` layer is introduced. Task 7 is now the `Position.INNER_FIRST` extension (above). The historical "remote data source + middleware impl" steps below are obsolete — **skip them** and go straight to Task 8.

- [ ] **Step 1: Define remote data source interface**

```kotlin
// data/.../PersonalFavoritesRemoteDataSource.kt
package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

interface PersonalFavoritesRemoteDataSource {

    /** Create a Link-layout widget at the TOP of the personal-widgets doc for [space]. */
    suspend fun add(space: SpaceId, target: Id)

    /**
     * Remove every wrapper whose inner link targets [target]. No-op if absent.
     * Implementation: open the personal-widgets doc, find matching inner-link block IDs,
     * issue `Rpc.Block.ListDelete` (middleware unlinks both link + parent wrapper).
     */
    suspend fun remove(space: SpaceId, target: Id)

    /**
     * Reorder the personal-widgets doc so wrappers for [orderedTargets] appear in that
     * order. Targets not currently present in the doc are ignored. Implementation:
     * open doc, resolve target → wrapper IDs, issue block moves.
     */
    suspend fun reorder(space: SpaceId, orderedTargets: List<Id>)
}
```

- [ ] **Step 2: Implement in middleware module**

```kotlin
// middleware/.../PersonalFavoritesMiddleware.kt
package com.anytypeio.anytype.middleware.favorites

import anytype.Rpc
import anytype.model.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.data.auth.repo.favorites.PersonalFavoritesRemoteDataSource
import com.anytypeio.anytype.domain.favorites.personalWidgetsId
import com.anytypeio.anytype.middleware.service.MiddlewareService

class PersonalFavoritesMiddleware(
    private val service: MiddlewareService
) : PersonalFavoritesRemoteDataSource {

    override suspend fun add(space: SpaceId, target: Id) {
        val ctx = personalWidgetsId(space)
        val request = Rpc.Block.CreateWidget.Request.newBuilder()
            .setContextId(ctx)
            .setTargetId(ctx)                         // insert relative to root
            .setBlock(linkBlockTo(target))
            .setPosition(anytype.model.Block.Position.InnerFirst)  // desktop match: TOP
            .setWidgetLayout(anytype.model.Block.Content.Widget.Layout.Link)
            .setObjectLimit(0)
            .build()
        service.blockCreateWidget(request)
        // MiddlewareService impls throw on non-NULL errors (see BlockMiddleware); match that.
    }

    override suspend fun remove(space: SpaceId, target: Id) {
        val tree = openPersonalWidgets(space)
        val linkIds = tree.innerLinkBlockIdsFor(target)
        if (linkIds.isEmpty()) return
        val request = Rpc.Block.ListDelete.Request.newBuilder()
            .setContextId(personalWidgetsId(space))
            .addAllBlockIds(linkIds)
            .build()
        service.blockListDelete(request)
    }

    override suspend fun reorder(space: SpaceId, orderedTargets: List<Id>) {
        if (orderedTargets.isEmpty()) return
        val ctx = personalWidgetsId(space)
        val tree = openPersonalWidgets(space)
        // Move each wrapper, in order, to the END of root children.
        // Net effect: the final child-order under root == orderedTargets.
        orderedTargets.forEach { target ->
            val wrapperId = tree.wrapperIdFor(target) ?: return@forEach
            val request = Rpc.Block.ListMoveToExistingObject.Request.newBuilder()
                .setContextId(ctx)
                .addBlockIds(wrapperId)
                .setTargetContextId(ctx)
                .setDropTargetId(ctx)
                .setPosition(anytype.model.Block.Position.InnerLast)
                .build()
            service.blockListMoveToExistingObject(request)
        }
    }

    private fun openPersonalWidgets(space: SpaceId): PersonalWidgetsTree {
        val request = Rpc.Object.Open.Request.newBuilder()
            .setObjectId(personalWidgetsId(space))
            .setSpaceId(space.id)
            .build()
        val response = service.objectOpen(request)
        return PersonalWidgetsTree.from(response.objectView)
    }

    private fun linkBlockTo(target: Id): anytype.model.Block { /* see impl */ }
}
```

> **Exact proto field names:** confirm from the generated Kotlin stubs before copy-pasting. Consult `MiddlewareService.kt` for the current signatures — `blockCreateWidget`, `blockListDelete`, `blockListMoveToExistingObject`, `objectOpen` are all already wrapped. Enums for position/layout may come via project mappers (`middleware/src/main/java/com/anytypeio/anytype/middleware/mappers/`) — use those if available rather than raw proto enum refs.
>
> `PersonalWidgetsTree` is a small helper class (same file or adjacent): given a decoded `ObjectView`, walk the `blocks` list to find wrapper blocks whose child is a Link block, and build two maps: wrapperId → targetId and targetId → List<linkBlockId>. Expose `wrapperIdFor(target): Id?` and `innerLinkBlockIdsFor(target): List<Id>`. Filter out built-in targets (`"favorite"`, `"set"`, `"recent"`, `"recentOpen"`, `"collection"`, `"allObjects"`).

- [ ] **Step 3: Build**

Run: `./gradlew :data:compileDebugKotlin :middleware:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add data/src/main/java/com/anytypeio/anytype/data/auth/repo/favorites/ \
         middleware/src/main/java/com/anytypeio/anytype/middleware/favorites/
git commit -m "DROID-4397 Wire personal-favorites via existing block-widget RPCs"
```

### Task 8: `PersonalFavoritesDataRepository` on top of `BlockRepository`

**Files:**
- Create: `data/src/main/java/com/anytypeio/anytype/data/auth/repo/favorites/PersonalFavoritesDataRepository.kt`
- Test: `data/src/test/java/com/anytypeio/anytype/data/auth/repo/favorites/PersonalFavoritesDataRepositoryTest.kt`

> **Why this shape:** `BlockRepository` already exposes `createWidget(ctx, source, layout, target, position)`, `openObject(id, space)`, `unlink(Command.Unlink)`, and `move(Command.Move)`. Those are exactly the primitives we need for personal-favorites add/remove/reorder. Consuming them directly avoids a redundant "remote data source" layer that would be pure pass-through to another pure pass-through.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun `add creates a Link widget at INNER_FIRST in the personal-widgets doc`() = runTest {
    val space = SpaceId("space-1")
    repo.add(space, "obj-1")

    verify(blocks).createWidget(
        ctx = personalWidgetsId(space),
        source = "obj-1",
        layout = WidgetLayout.LINK,
        target = personalWidgetsId(space),
        position = Position.INNER_FIRST
    )
}

@Test
fun `remove unlinks every wrapper whose inner link targets the removed id`() = runTest {
    val space = SpaceId("space-1")
    val ctx = personalWidgetsId(space)
    given(blocks.openObject(ctx, space)).willReturn(
        stubPersonalWidgetsObjectView(
            wrappers = listOf(
                stubWrapper(id = "w-1", innerLinkId = "l-1", target = "obj-1"),
                stubWrapper(id = "w-2", innerLinkId = "l-2", target = "obj-2")
            )
        )
    )

    repo.remove(space, "obj-1")

    verify(blocks).unlink(Command.Unlink(context = ctx, targets = listOf("w-1")))
}

@Test
fun `remove is a no-op when the target is not present`() = runTest { /* ... */ }

@Test
fun `reorder moves each wrapper to INNER at the root in order`() = runTest { /* ... */ }

@Test
fun `reorder skips targets that are not present`() = runTest { /* ... */ }
```

- [ ] **Step 2: Run test — should fail**

Expected: FAIL (class not found).

- [ ] **Step 3: Implement**

```kotlin
package com.anytypeio.anytype.data.auth.repo.favorites

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.favorites.PersonalFavoritesRepository
import com.anytypeio.anytype.domain.favorites.personalWidgetsId

class PersonalFavoritesDataRepository(
    private val blocks: BlockRepository
) : PersonalFavoritesRepository {

    override suspend fun add(space: SpaceId, target: Id) {
        val ctx = personalWidgetsId(space)
        blocks.createWidget(
            ctx = ctx,
            source = target,
            layout = WidgetLayout.LINK,
            target = ctx,
            position = Position.INNER_FIRST
        )
    }

    override suspend fun remove(space: SpaceId, target: Id) {
        val ctx = personalWidgetsId(space)
        val wrappers = blocks.openObject(ctx, space).wrapperIdsTargeting(target)
        if (wrappers.isEmpty()) return
        blocks.unlink(Command.Unlink(context = ctx, targets = wrappers))
    }

    override suspend fun reorder(space: SpaceId, orderedTargets: List<Id>) {
        if (orderedTargets.isEmpty()) return
        val ctx = personalWidgetsId(space)
        val view = blocks.openObject(ctx, space)
        orderedTargets.forEach { target ->
            val wrapperId = view.wrapperIdFor(target) ?: return@forEach
            blocks.move(
                Command.Move(
                    ctx = ctx,
                    targetContextId = ctx,
                    blockIds = listOf(wrapperId),
                    targetId = ctx,
                    position = Position.INNER // proto `Inner` == "last child"
                )
            )
        }
    }
}

// Helpers (same file or adjacent):
// - ObjectView.wrapperIdsTargeting(target): finds wrapper blocks whose inner link's
//   targetBlockId == target. Matches desktop's getWidgetsForTargetIn.
// - ObjectView.wrapperIdFor(target): first wrapper ID whose inner link targets target.
```

- [ ] **Step 4: Run test — should pass**
- [ ] **Step 5: Commit**

```bash
git add data/src/main/java/com/anytypeio/anytype/data/auth/repo/favorites/PersonalFavoritesDataRepository.kt \
         data/src/test/java/com/anytypeio/anytype/data/auth/repo/favorites/PersonalFavoritesDataRepositoryTest.kt
git commit -m "DROID-4397 Personal favorites data repository on BlockRepository"
```

### Task 9: DI bindings

**Files:**
- Modify: Dagger module(s) binding repos (likely `di/src/main/java/com/anytypeio/anytype/di/main/DataModule.kt` or equivalent — grep first)
- Modify: module binding home/widget-screen use cases (likely `di/src/main/java/com/anytypeio/anytype/di/feature/home/HomescreenDI.kt` — grep first)

- [ ] **Step 1: Locate the module binding repos**

Run: `grep -rn "fun provideBlockRepository\|BlockDataRepository(" di/ app/ data/`
Expected: matches that show how `BlockRepository` is wired (`BlockDataRepository` ← `BlockRemote` ← `BlockMiddleware(service)`). Mirror this pattern for personal favorites. Note the `@Singleton` scope and whether providers use `@JvmStatic`.

- [ ] **Step 2: Add provides for the repository**

```kotlin
@JvmStatic
@Provides
@Singleton
fun providePersonalFavoritesRepository(
    blocks: BlockRepository
): PersonalFavoritesRepository = PersonalFavoritesDataRepository(blocks)
```

(No `PersonalFavoritesRemoteDataSource` binding — the repository depends on `BlockRepository` directly, which is already wired.)

- [ ] **Step 3: Add provides for the 3 use cases in HomescreenDI (scoped to the home-screen component)**

```kotlin
@Provides
fun provideAddPersonalFavorite(
    repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) = AddPersonalFavorite(repo, dispatchers)

@Provides
fun provideRemovePersonalFavorite(
    repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) = RemovePersonalFavorite(repo, dispatchers)

@Provides
fun provideReorderPersonalFavorites(
    repo: PersonalFavoritesRepository,
    dispatchers: AppCoroutineDispatchers
) = ReorderPersonalFavorites(repo, dispatchers)
```

(Check the actual use-case constructor signatures before wiring — the Phase 1 scaffolding may use `@Inject` constructor injection instead, in which case only the repo + remote data source provides are needed.)

- [ ] **Step 4: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add di/ app/ data/   # whichever module was actually modified
git commit -m "DROID-4397 DI bindings for personal favorites"
```

---

## Phase 3 — SectionType + Widget Model

### Task 10: Add `MY_FAVORITES` to `WidgetSectionType` and `SectionType`

**Files:**
- Modify: `core-models/src/main/java/com/anytypeio/anytype/core_models/SectionSettings.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/Widget.kt`

- [ ] **Step 1: Add `MY_FAVORITES` enum value**

In `SectionSettings.kt`, extend `WidgetSectionType`:

```kotlin
enum class WidgetSectionType { UNREAD, PINNED, MY_FAVORITES, OBJECTS, RECENTLY_EDITED, BIN }
```

- [ ] **Step 2: Add `MY_FAVORITES` to presentation `SectionType`**

In `Widget.kt`:

```kotlin
enum class SectionType { PINNED, UNREAD, MY_FAVORITES, TYPES, RECENTLY_EDITED, NONE }
```

- [ ] **Step 3: Handle the new case in any `when(sectionType)` blocks**

Run: `grep -rn "SectionType\." presentation/ app/`
For every exhaustive `when`, add a branch for `MY_FAVORITES`. If the section should behave like `UNREAD` in that location, copy the `UNREAD` branch.

- [ ] **Step 4: Build**

Run: `./gradlew :core-models:compileDebugKotlin :presentation:compileDebugKotlin :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL with no `when` exhaustiveness errors.

- [ ] **Step 5: Commit**

```bash
git add core-models/ presentation/ app/
git commit -m "DROID-4397 Introduce MY_FAVORITES section type"
```

### Task 11: Add `Widget.PersonalFavorites` model

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/Widget.kt`

- [ ] **Step 1: Add data class**

Next to the existing `Widget.UnreadChatList` declaration, add:

```kotlin
sealed class Widget { ...
    data class PersonalFavorites(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val sectionType: SectionType = SectionType.MY_FAVORITES
    ) : Widget()
}
```

Match existing field set exactly — reference `Widget.UnreadChatList`. The `source` can be a synthetic `Source.Bundled.PersonalFavorites`; if it needs a new `Source.Bundled` variant, add it.

- [ ] **Step 2: Build**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/Widget.kt
git commit -m "DROID-4397 Add Widget.PersonalFavorites model"
```

---

## Phase 4 — Widget Container

### Task 12: `PersonalFavoritesWidgetContainer` (TDD)

**Files:**
- Create: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/PersonalFavoritesWidgetContainer.kt`
- Test: `presentation/src/test/java/com/anytypeio/anytype/presentation/widgets/PersonalFavoritesWidgetContainerTest.kt`

> **Architecture:** The container observes the personal-widgets virtual doc directly — **no `ObservePersonalFavorites` use case exists** (see Task 6 — removed). Open the doc with `OpenObject` and listen for block events via `InterceptEvents`, mirroring `HomeScreenViewModel`'s existing treatment of the shared widgets doc (see `HomeScreenViewModel.kt:648` and `observeExternalPayloadEvents()` at line 907). Reduce block events against the doc state, walk the root's children to extract ordered target IDs (filter out built-in targets `favorite`, `set`, `recent`, `recentOpen`, `collection`, `allObjects`), then join with object subscriptions to produce `WidgetView.SetOfObjects.Element` rows.

- [ ] **Step 1: Write the failing test — emits WidgetView with ordered objects**

```kotlin
@Test
fun `emits ordered object rows from block tree`() = runTest {
    val spaceId = SpaceId("space-1")
    val widget = stubPersonalFavoritesWidget()

    // Stub OpenObject for the personal-widgets doc to return an ObjectView whose
    // root has two wrapper->link children targeting obj-1 then obj-2, in that order.
    given(openObject.stream(eq(OpenObject.Params(
        obj = personalWidgetsId(spaceId),
        saveAsLastOpened = false,
        spaceId = spaceId
    )))).willReturn(flowOf(Resultat.Success(
        stubPersonalWidgetsObjectView(orderedTargets = listOf("obj-1", "obj-2"))
    )))
    // No events after initial state
    given(interceptEvents.build(InterceptEvents.Params(personalWidgetsId(spaceId))))
        .willReturn(flowOf(emptyList()))
    // Object subscription returns the two objects
    given(storage.subscribe(any<StoreSearchParams>())).willReturn(
        flowOf(listOf(stubObject("obj-1"), stubObject("obj-2")))
    )

    val container = PersonalFavoritesWidgetContainer(
        space = spaceId,
        widget = widget,
        openObject = openObject,
        interceptEvents = interceptEvents,
        storage = storage,
        urlBuilder = urlBuilder,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes,
        isSessionActiveFlow = flowOf(true)
    )

    container.view.test {
        val emitted = awaitItem() as WidgetView.SetOfObjects
        assertEquals(2, emitted.elements.size)
        assertEquals("obj-1", emitted.elements[0].obj.id)
        assertEquals("obj-2", emitted.elements[1].obj.id)
    }
}
```

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew :presentation:testDebugUnitTest --tests "*PersonalFavoritesWidgetContainerTest*"`
Expected: FAIL (class not found).

- [ ] **Step 3: Implement minimal container**

Use `HomeScreenViewModel.observeExternalPayloadEvents()` and `proceedWithObjectViewStatePipeline()` as the reference. Key points:
- `OpenObject.stream(Params(obj = personalWidgetsId(space), ...))` gives initial `ObjectView` (block tree).
- `InterceptEvents.build(Params(personalWidgetsId(space)))` gives live block events.
- Merge the two with `scan { state, payload -> reduce(state, payload) }` — `reduce()` already exists for the shared widgets doc; reuse it.
- From the reduced `ObjectView`, walk root's children, collect `(childBlockId → targetBlockId)` pairs. Filter out built-in target IDs.
- Subscribe to the `[id IN orderedTargets]` object store to fetch the full object details.
- Emit `WidgetView.SetOfObjects(widget.id, widget.source, elements, isCompact = true)`.

```kotlin
class PersonalFavoritesWidgetContainer(
    private val space: SpaceId,
    private val widget: Widget.PersonalFavorites,
    private val openObject: OpenObject,
    private val interceptEvents: InterceptEvents,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    isSessionActiveFlow: Flow<Boolean>
) : WidgetContainer {

    override val view: Flow<WidgetView> = isSessionActiveFlow.flatMapLatest { active ->
        if (!active) flowOf(emptyView())
        else personalWidgetsBlockTree(space)
            .map { tree -> tree.orderedRealTargetIds() }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyView())
                else joinWithObjects(ids)
            }
    }.catch { Timber.e(it, "personal favorites container failed") }

    private fun personalWidgetsBlockTree(space: SpaceId): Flow<ObjectView> { /* open + reduce events */ }
    private fun joinWithObjects(ids: List<Id>): Flow<WidgetView> { /* storage.subscribe + map */ }
    private fun emptyView() = WidgetView.SetOfObjects(widget.id, widget.source, emptyList(), isCompact = true)
}
```

`isCompact = true` is the UI flag instructing `WidgetSection.kt` to render Unread-style rows, not card widgets (`WidgetView.SetOfObjects.isCompact` — added in Task 13, already shipped).

**Built-in targets to filter out** (copied from the GO-6962 PR description — these are valid link `targetBlockId` values that represent views, not real objects):
```kotlin
private val BUILTIN_TARGETS = setOf("favorite", "set", "recent", "recentOpen", "collection", "allObjects")
```

- [ ] **Step 4: Run test — should pass**

Run: `./gradlew :presentation:testDebugUnitTest --tests "*PersonalFavoritesWidgetContainerTest*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/PersonalFavoritesWidgetContainer.kt \
         presentation/src/test/java/com/anytypeio/anytype/presentation/widgets/PersonalFavoritesWidgetContainerTest.kt
git commit -m "DROID-4397 PersonalFavoritesWidgetContainer emits compact row view"
```

### Task 13: Add `isCompact` flag to `WidgetView.SetOfObjects` (if missing)

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetView.kt`

- [ ] **Step 1: Check whether the flag exists**

Run: `grep -n "isCompact" presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetView.kt`
If present → skip this task. Otherwise continue.

- [ ] **Step 2: Add the flag**

```kotlin
data class SetOfObjects(
    override val id: Id,
    override val source: Source,
    val elements: List<Element>,
    val isCompact: Boolean = false,
    ...
) : WidgetView()
```

- [ ] **Step 3: Build**

Run: `./gradlew :presentation:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetView.kt
git commit -m "DROID-4397 Add isCompact flag for Unread-style rendering"
```

### Task 14: Wire the container into `WidgetContainerDelegateImpl`

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetContainerDelegate.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/home/HomeScreenViewModel.kt`

- [ ] **Step 1: Inject use case into delegate**

Constructor-inject `ObservePersonalFavorites` into `WidgetContainerDelegateImpl`. Emit a `Widget.PersonalFavorites` whenever the user has ≥1 personal favorite in the active `SpaceId`.

- [ ] **Step 2: Instantiate `PersonalFavoritesWidgetContainer`**

In the `when` that maps `Widget` → `WidgetContainer` (same block currently handling `Widget.UnreadChatList`), add:

```kotlin
is Widget.PersonalFavorites -> PersonalFavoritesWidgetContainer(
    space = spaceId,
    widget = widget,
    observePersonalFavorites = observePersonalFavorites,
    storage = subscriptionContainer,
    urlBuilder = urlBuilder,
    fieldParser = fieldParser,
    storeOfObjectTypes = storeOfObjectTypes,
    isSessionActiveFlow = isSessionActiveFlow
)
```

- [ ] **Step 3: Produce the `Widget.PersonalFavorites` from ViewModel**

In `HomeScreenViewModel`, when the widget list is composed for a space, inject a synthetic `Widget.PersonalFavorites` (in the correct section-order slot) *only if* `ObservePersonalFavorites` has emitted at least one ID. Hide otherwise (spec: "My Favorites section is hidden when the user has no favorites in this channel").

- [ ] **Step 4: Build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add presentation/
git commit -m "DROID-4397 Wire PersonalFavoritesWidgetContainer into home screen"
```

---

## Phase 5 — UI: My Favorites Section + Flat Pinned

### Task 15: `MyFavoritesSectionHeader` + compact row rendering

**Files:**
- Create: `app/src/main/java/com/anytypeio/anytype/ui/home/PersonalFavoritesSection.kt`

- [ ] **Step 1: Create the header composable**

Copy the pattern of `UnreadSectionHeader` from `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt:609`. Render plain text "My Favorites", no icon, no counter badge. Same padding + typography.

```kotlin
@Composable
fun MyFavoritesSectionHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text = stringResource(R.string.widget_section_my_favorites),
            style = Title2,
            color = colorResource(R.color.text_transparent_secondary),
            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp).align(Alignment.BottomStart)
        )
    }
}
```

Add `widget_section_my_favorites` to `strings.xml` with value `"My Favorites"` (and localized variants if the team has an i18n workflow — grep for `widget_section_unread` and mirror).

- [ ] **Step 2: Create compact row composable**

Reuse the existing Unread-style row composable from `ListWidget.kt:163–207`. If not already generic, extract it into a shared `CompactObjectRow` and have both Unread and My Favorites call it.

- [ ] **Step 3: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/home/PersonalFavoritesSection.kt \
         app/src/main/res/values/strings.xml
git commit -m "DROID-4397 MyFavoritesSectionHeader + compact row"
```

### Task 16: Render My Favorites in `WidgetSection.kt`

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt`

- [ ] **Step 1: Add the `MY_FAVORITES` case**

In `renderWidgetSection()`, add a branch mirroring `UNREAD`:

```kotlin
SectionType.MY_FAVORITES -> {
    item { MyFavoritesSectionHeader() }
    // iterate widget.elements and render CompactObjectRow with drag handle + long-press menu
}
```

- [ ] **Step 2: Hide the section when elements are empty**

If `personalFavoritesWidget.elements.isEmpty()`, skip emission entirely. (Should already be prevented upstream in the ViewModel, but double-guard here.)

- [ ] **Step 3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt
git commit -m "DROID-4397 Render My Favorites section"
```

### Task 17: Render Pinned as flat rows (no header) under Home widget

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetsScreen.kt`

- [ ] **Step 1: Identify current PINNED section rendering**

Run: `grep -n "SectionType.PINNED" app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt`
Note the current behavior: it renders a section header + widget cards.

- [ ] **Step 2: Change rendering for object-link pins only**

For pins whose source is a plain object (not a list/tree widget with children), render each as a `CompactObjectRow` directly — no section header, no card wrapper. For list-type pinned widgets (e.g., pinned Sets, pinned Types), keep existing card rendering.

The spec: *"Pinned objects. Flat list of rows, no section header"* — applies to object pins. If the product wants *all* pinned widgets flattened, widen the branch; confirm with designer if ambiguous.

- [ ] **Step 3: Update ordering in `WidgetsScreen.kt`**

Ensure the section emission order inside the Lazy column is:
1. Home widget
2. Pinned (object-link rows only, flattened; card-type pins below if present)
3. Unread
4. My Favorites
5. Remaining (Types, RecentlyEdited, Bin)

- [ ] **Step 4: Build**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Visual QA**

Install debug build on device/emulator with at least one pinned object and one personal favorite. Verify:
- Pinned object renders as flat row under Home, no header.
- My Favorites renders as Unread-style section with header.
- Order matches spec.

Run: `./gradlew :app:installDebug && adb shell am start -n com.anytypeio.anytype/.ui.main.MainActivity`

- [ ] **Step 6: Commit**

```bash
git add app/
git commit -m "DROID-4397 Flat-row rendering for pinned object links"
```

---

## Phase 6 — Object Context Menu ("...") Actions

### Task 18: Expose favorite/pin state + role in `ObjectMenuOptionsProviderImpl`

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/objects/menu/ObjectMenuOptionsProviderImpl.kt`
- Test: existing `ObjectMenuOptionsProviderImplTest.kt`

- [ ] **Step 1: Add fields to `Options`**

```kotlin
data class Options(
    ...
    val isPersonallyFavorited: Boolean,
    val canPinToChannel: Boolean,   // Owner or Admin
    val isPinnedToChannel: Boolean
)
```

- [ ] **Step 2: Inject deps**

Inject `ObservePersonalFavorites`, `UserPermissionProvider`, and a way to observe the shared-pin state for the current object (likely already present via existing pin subscription — grep `isPinned`, `pinObject` in presentation/).

- [ ] **Step 3: Emit combined flow**

Combine the three flows (favorites, pins, permissions) with the existing layout flow and map into `Options`.

- [ ] **Step 4: Extend the test**

Add a test case for each of the 4 new flags toggling independently:

```kotlin
@Test
fun `emits isPersonallyFavorited when id is in favorites list`() = runTest {
    // seed observePersonalFavorites to emit listOf("obj-1")
    // target object id = "obj-1"
    // expect Options.isPersonallyFavorited == true
}
```

- [ ] **Step 5: Run test — should fail**

Run: `./gradlew :presentation:testDebugUnitTest --tests "*ObjectMenuOptionsProviderImplTest*"`
Expected: FAIL (new flag unset).

- [ ] **Step 6: Implement until test passes**

- [ ] **Step 7: Commit**

```bash
git add presentation/
git commit -m "DROID-4397 Expose favorite/pin state + admin role in ObjectMenuOptions"
```

### Task 19: Render new menu items in object `...` menu

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/objects/menu/ObjectMenuFragment.kt` (or the composable that renders `ObjectMenuOptions`)
- Modify: `core-ui/src/main/res/drawable/` (add icons if missing)

- [ ] **Step 1: Check for existing star + pin icons**

Run: `find core-ui/src/main/res/drawable -name "*star*" -o -name "*pin*"`
Mark which to create. Use Figma export if missing.

- [ ] **Step 2: Render conditionally**

In the menu composable:

```kotlin
if (options.isPersonallyFavorited) {
    MenuItem(icon = R.drawable.ic_star_filled_18, title = R.string.unfavorite,
        onClick = onUnfavorite)
} else {
    MenuItem(icon = R.drawable.ic_star_outline_18, title = R.string.favorite,
        onClick = onFavorite)
}
if (options.canPinToChannel) {
    if (options.isPinnedToChannel) {
        MenuItem(icon = R.drawable.ic_pin_18, title = R.string.unpin_from_channel,
            onClick = onUnpinFromChannel)
    } else {
        MenuItem(icon = R.drawable.ic_pin_18, title = R.string.pin_to_channel,
            onClick = onPinToChannel)
    }
}
```

Spec: star icon for favorite/unfavorite; pin icon for pin/unpin. Do not swap.

- [ ] **Step 3: Add strings**

`favorite`, `unfavorite`, `pin_to_channel`, `unpin_from_channel` in `strings.xml`.

- [ ] **Step 4: Wire handlers in `ObjectMenuViewModel`**

```kotlin
fun onFavorite(objectId: Id) = viewModelScope.launch {
    addPersonalFavorite.async(AddPersonalFavorite.Params(currentSpaceId, objectId))
}
fun onUnfavorite(...) = ...
fun onPinToChannel(...)    // existing pin-as-widget pathway
fun onUnpinFromChannel(...)
```

Reuse the existing Pin-as-widget flow from `PinObjectAsWidgetDelegate` for Pin/Unpin to Channel (same RPCs, same widget store). Owner/Admin gating enforced by middleware *and* by hiding the menu item (`canPinToChannel` flag).

- [ ] **Step 5: Test the ViewModel**

Extend `ObjectMenuViewModelTest` (or create if missing) to verify each of the 4 actions invokes the correct use case with the correct IDs.

- [ ] **Step 6: Build + run**

Run: `./gradlew :app:assembleDebug && ./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 7: Commit**

```bash
git add app/ core-ui/ presentation/
git commit -m "DROID-4397 Favorite/Unfavorite + Pin/Unpin actions in object menu"
```

---

## Phase 7 — Widget Long-Press Menu

### Task 20: Add actions to `WidgetDropDownMenu`

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/widgets/menu/WidgetDropDownMenu.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetDropDownMenuAction.kt` (or equivalent)

- [ ] **Step 1: Extend the action sealed class**

```kotlin
sealed class WidgetDropDownMenuAction { ...
    data class Favorite(val objectId: Id) : WidgetDropDownMenuAction()
    data class Unfavorite(val objectId: Id) : WidgetDropDownMenuAction()
    data class PinToChannel(val objectId: Id) : WidgetDropDownMenuAction()
    data class UnpinFromChannel(val objectId: Id) : WidgetDropDownMenuAction()
}
```

- [ ] **Step 2: Update `getWidgetMenuItems()` to emit the new items**

Extend with favorite/pin visibility, gated by the object's current favorite/pin state + user permission. The permission flag must be plumbed through (same source as Task 18 — `UserPermissionProvider.get(space)`).

- [ ] **Step 3: Wire the new actions to the view model handlers added in Task 19**

The widget long-press menu ViewModel (`HomeScreenViewModel` or the widget-specific one) should dispatch these actions to the same use cases called by the object `...` menu.

- [ ] **Step 4: Test**

Add a unit test that covers each role (Owner, Admin, Member, Viewer) and confirms the correct menu item set.

```kotlin
@Test
fun `member sees favorite but not pin-to-channel`() { ... }
@Test
fun `admin sees favorite and pin-to-channel`() { ... }
```

- [ ] **Step 5: Build + run tests**

Run: `./gradlew :app:assembleDebug testDebugUnitTest`
Expected: all pass.

- [ ] **Step 6: Commit**

```bash
git add app/ presentation/
git commit -m "DROID-4397 Favorite/Pin actions in widget long-press menu"
```

---

## Phase 8 — Drag-and-Drop Reorder for My Favorites

### Task 21: Wire `ReorderableItem` on My Favorites rows

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetSection.kt` (the `MY_FAVORITES` branch from Task 16)
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/home/HomeScreenViewModel.kt`

- [ ] **Step 1: Wrap each row in `ReorderableItem`**

Follow the pattern from `WidgetSection.kt:100+` (existing reorderable widget items). Use the same `reorderableState` currently threaded through the LazyListScope. Use `sh.calvin.reorderable.ReorderableLazyListState` semantics.

- [ ] **Step 2: Persist reorder**

When `onDragEnd` fires, compute the new ordered list of IDs and call:

```kotlin
reorderPersonalFavorites.async(
    ReorderPersonalFavorites.Params(space = currentSpaceId, order = newOrder)
)
```

- [ ] **Step 3: Optimistic UI**

During the drag, update local UI immediately. On RPC failure, revert (the subscription will emit the truth on next tick anyway — good enough).

- [ ] **Step 4: Test**

Add ViewModel test: simulate drag end → verify `ReorderPersonalFavorites` invoked with the new order list.

- [ ] **Step 5: Build + manual QA**

Run: `./gradlew :app:installDebug`
Manually drag favorites; verify persistence across app restart.

- [ ] **Step 6: Commit**

```bash
git add app/ presentation/
git commit -m "DROID-4397 Drag-and-drop reorder for My Favorites"
```

---

## Phase 9 — Edge Cases

### Task 22: Hide pin actions when role downgrades from Admin

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/widgets/WidgetDropDownMenuViewModel.kt` (wherever menu visibility computes)
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/objects/menu/ObjectMenuViewModel.kt`

- [ ] **Step 1: Subscribe to `UserPermissionProvider.observe(space)`**

Both ViewModels must observe (not `get()`) the permission so a role downgrade live-removes the Pin/Unpin items.

- [ ] **Step 2: Test**

Emit a role change in a `MutableStateFlow` and assert that the menu no longer contains Pin items.

- [ ] **Step 3: Commit**

```bash
git add presentation/
git commit -m "DROID-4397 Reactively hide pin actions on role downgrade"
```

### Task 23: Deleted / binned favorites disappear

No code to write if Task 12's subscription query is keyed on `filters = [idIn(order)]` against the object store — the store naturally omits deleted/binned objects. **Verify** by:

- [ ] **Step 1: Manual QA**

Favorite an object → send it to Bin → confirm My Favorites row disappears.

- [ ] **Step 2: Optional unit test**

If time permits, write a container test: seed `ObservePersonalFavorites` with `["a","b"]` but have `storage.subscribe` only return object `"a"` → verify only `"a"` renders.

- [ ] **Step 3: Commit (only if a test/fix was added)**

```bash
git add presentation/
git commit -m "DROID-4397 Verify deleted favorites drop out"
```

---

## Phase 10 — Final Verification

### Task 24: Run full test + lint suite, PR-check

- [ ] **Step 1: Run all unit tests**

Run: `make test_debug_all`
Expected: all green.

- [ ] **Step 2: Lint**

Run: `./gradlew lintDebug`
Expected: no new warnings in modified modules.

- [ ] **Step 3: Full PR check**

Run: `make pr_check`
Expected: SUCCESS.

- [ ] **Step 4: Manual acceptance against spec**

Walk the spec's "Empty and edge states" section:
- [ ] No favorites → My Favorites section hidden
- [ ] No pins → Pinned block hidden
- [ ] Favorited object deleted → disappears from My Favorites
- [ ] Member can favorite/unfavorite; cannot pin/unpin
- [ ] Owner/Admin can do all four actions
- [ ] Same object favorited + pinned shows in both lists independently
- [ ] Removing from one list does not affect the other
- [ ] Drag-and-drop persists across app restart
- [ ] Second device of same user sees favorites update in real time (sanity check if possible)
- [ ] Star icon used for favorite; pin icon for pin. No swaps.

- [ ] **Step 5: Commit any final tweaks and open PR**

```bash
git push -u origin droid-4397-personal-favorites
gh pr create --title "DROID-4397 Personal Favorites" --body "..."
```

---

## Out of Scope (per spec)

- Global favorites across spaces (existing `ListOfObjects.Type.Favorites` widget — leave alone)
- Client-side limits on favorites count
- Migration of existing pins
- Shared pins in 1-1 channels

## Risk Register

| Risk | Mitigation |
|------|------------|
| GO-6962 field names differ from plan's guesses | Task 1 reads the generated stubs before coding |
| Widget subscription fan-out reorders unpredictably | Preserve the ID order from `ObservePersonalFavorites` — never trust the object-store subscription order |
| Permission flow: middleware lets a Member call pin-add | Middleware enforces; client hides the button. Defense in depth. |
| Pinned rendering change breaks existing non-object pinned widgets (Sets, Types) | Only flatten pins whose source is a plain object link; keep card rendering for list-type pins |
| Reorder race when two devices drag at once | Last-write-wins is acceptable per spec; subscription reconciles |

---

**End of plan.**
