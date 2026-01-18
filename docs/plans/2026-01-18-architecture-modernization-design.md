# Architecture Modernization Design

**Date:** 2026-01-18
**Status:** Validated against codebase
**Goal:** Migrate from legacy Dagger + mixed UI + eager loading to Hilt + Compose navigation + lazy/paginated spaces

---

## Executive Summary

This design modernizes the Anytype Android architecture to resolve:
- **OOM on large accounts** - spaces loaded all at once (`limit = 0`)
- **DI complexity** - custom ComponentManager with 110+ components
- **Navigation fragmentation** - XML Navigation Component + manual Fragment transactions
- **Monolithic presentation** - 40+ packages in `:presentation` module

The migration is incremental, keeping legacy editor/sets screens functional throughout.

---

## Current State (Validated)

### Spaces Loading
- `SpaceViewSubscriptionContainer` subscribes with `limit = 0` (fetch all)
- All spaces held in `MutableStateFlow<List<ObjectWrapper.SpaceView>>`
- No pagination library usage anywhere in codebase
- Boot sequence: MainActivity → MainViewModel.onRestore() → GlobalSubscriptionManager.onStart() → all subscriptions fire

### Dependency Injection
- Dagger 2.58 with custom `ComponentManager` singleton
- 110+ component definitions, 45+ `ComponentDependencies` interfaces
- Manual `.get()`, `.new()`, `.release()` lifecycle management
- Custom `@PerScreen` scope
- No existing Hilt setup

### Navigation
- MainActivity (903 lines) hosts `NavHostFragment` with `graph.xml` (708+ lines)
- Command-driven: ViewModels emit `Command.Navigate(...)` → Activity dispatches
- Nested graphs: `objectNavigation`, `dataViewNavigation`
- Deep links: `DefaultDeepLinkResolver` handles 8+ URL patterns
- Limited Compose NavHost usage (MembershipFragment only)

### Module Structure (30 modules)
- Clean architecture layers exist: `:domain`, `:data`, `:presentation`
- Core modules: `:core-models`, `:core-utils`, `:core-ui`
- 7 feature modules: `:feature-chats`, `:feature-properties`, etc.
- Auth and Spaces logic embedded in monolithic `:presentation`

---

## Target Architecture

### Layers Per Feature
```
UI (Compose screens + ViewModel)
    ↓
Domain (Use cases)
    ↓
Data (Repository impl + PagingSource + cache)
```

### Module Structure (Target)
```
:app                    - Entry, DI composition, MainActivity, NavHost
:core-utils             - Dispatchers, Result/Either, logging (exists)
:core-models            - Domain models (exists)
:core-ui                - Design system (exists)
:domain                 - Use cases, repository interfaces (exists)
:data                   - Repository implementations (exists)
:feature:auth           - Login/logout, session restoration (NEW - extract)
:feature:spaces         - Spaces list, space switching, pagination (NEW - extract)
:feature-chats          - Chat functionality (exists)
:feature-editor         - Legacy wrapper (exists in :app)
:feature-sets           - Legacy wrapper (exists in :app)
```

### Data Flow
```
UI (Compose/Views) → ViewModels → Use Cases → Repositories → Middleware (Go)
```

### App State Machine
```kotlin
sealed class AppState {
    object Loading : AppState()
    object LoggedOut : AppState()
    data class LoggedIn(val session: Session, val config: AccountConfig) : AppState()
}
```

Boot flow:
1. `SessionRepository.restore()` - check token/user presence
2. If none → `LoggedOut`
3. If session → fetch minimal account/config → `LoggedIn`
4. **Do NOT load spaces list in boot**

---

## Implementation Milestones

### Milestone A: Foundations

**Objectives:**
- Enable Hilt alongside Dagger (no migration yet)
- Extract `:feature:auth` and `:feature:spaces` from `:presentation`
- Implement session boot without spaces preloading
- Create ComponentManager bridge layer

**Tasks:**
1. Add Hilt plugins + `@HiltAndroidApp` to `AndroidApplication`
2. Add `@AndroidEntryPoint` to MainActivity
3. Create `ComponentManagerBridge` - wrapper that allows gradual replacement
4. Extract auth-related code from `:presentation` to `:feature:auth`
5. Extract spaces-related code from `:presentation` to `:feature:spaces`
6. Implement `SessionRepository` with `restore()` method
7. Implement `AppState` sealed class and `AppViewModel`
8. Remove spaces subscription from boot sequence

**Done when:**
- [ ] Hilt compiles alongside Dagger without conflicts
- [ ] App boots without calling `SpaceViewSubscriptionContainer.start()`
- [ ] `:feature:auth` module exists with login/logout logic
- [ ] `:feature:spaces` module exists with spaces list logic
- [ ] Cold start does not request all spaces

### Milestone B: Compose Navigation Root

**Objectives:**
- MainActivity becomes Compose root with NavHost
- Legacy screens wrapped as navigation destinations
- Navigator abstraction updated for Compose

**Tasks:**
1. Document all 30+ destinations in `graph.xml` with route mapping:
   ```
   XML destination          → Compose route
   ─────────────────────────────────────────
   splashScreen             → (removed, handled by AppState)
   vaultScreen              → "vault"
   homeScreen               → "home"
   pageScreen               → "editor/{objectId}?space={spaceId}"
   objectSetScreen          → "set/{objectId}?space={spaceId}"
   chatScreen               → "chat/{chatId}?space={spaceId}"
   ```
2. Replace `activity_main.xml` NavHostFragment with Compose NavHost
3. Implement `RootNavGraph`:
   ```kotlin
   NavHost(startDestination = when(appState) {
       is LoggedOut -> "auth"
       is LoggedIn -> "main"
   }) {
       authGraph()      // login screens
       mainGraph()      // vault, spaces, content
       legacyGraph()    // wrapped fragments
   }
   ```
4. Create `FragmentDestination` wrapper for legacy screens
5. Update `Navigator` to use Compose `NavController`
6. Keep deep link resolution at Activity level, route to appropriate graph

**Done when:**
- [ ] MainActivity uses Compose NavHost
- [ ] Auth flow navigates via Compose routes
- [ ] Legacy editor/sets screens reachable via wrapped destinations
- [ ] Deep links still work correctly
- [ ] Back navigation behaves correctly

### Milestone C: Spaces Pagination (Critical for OOM)

**Objectives:**
- Implement paginated spaces loading
- Remove `limit = 0` pattern
- Build Compose SpacesList screen

**Tasks:**
1. Define pagination contract:
   ```kotlin
   interface SpacesRepository {
       fun pagedSpaces(): Flow<PagingData<Space>>
       suspend fun getSpace(spaceId: SpaceId): Space
       suspend fun getFirstSpaceId(): SpaceId?
   }
   ```
2. Implement `SpacesPagingSource`:
   ```kotlin
   class SpacesPagingSource(
       private val middleware: Middleware
   ) : PagingSource<String, Space>() {
       override suspend fun load(params: LoadParams<String>): LoadResult<String, Space> {
           val cursor = params.key
           val limit = params.loadSize
           val result = middleware.getSpaces(limit = limit, cursor = cursor)
           return LoadResult.Page(
               data = result.spaces,
               prevKey = null,
               nextKey = result.nextCursor
           )
       }
   }
   ```
3. Update `SpaceViewSubscriptionContainer` to use pagination or replace entirely
4. Implement `getFirstSpaceId()` - lightweight query for default space
5. Build Compose `SpacesListScreen`:
   ```kotlin
   @Composable
   fun SpacesListScreen(viewModel: SpacesViewModel) {
       val spaces = viewModel.spaces.collectAsLazyPagingItems()
       LazyColumn {
           items(spaces) { space ->
               SpaceCard(space, onClick = { viewModel.openSpace(space.id) })
           }
       }
   }
   ```
6. Implement "open default space" logic using `getFirstSpaceId()`

**Done when:**
- [ ] Spaces list loads in pages (10-20 per page)
- [ ] Scrolling triggers next page load
- [ ] Memory stable for accounts with hundreds of spaces
- [ ] Cold start does not request all spaces
- [ ] Default space opens without loading full list

### Milestone D: Deep Link Correctness

**Objectives:**
- Deep links open correct destination without preloading
- Space switching handled minimally

**Tasks:**
1. Update `DefaultDeepLinkResolver` to return route strings
2. Implement object → space resolution endpoint/cache
3. Handle deep link scenarios:
   - Same space: navigate directly
   - Different space: switch space minimally, then navigate
   - Unknown space: resolve from object ID
4. Test all 8+ deep link patterns

**Done when:**
- [ ] Deep link opens correct destination without full preload
- [ ] Space switching works with minimal data fetch
- [ ] All existing deep link patterns still work

### Milestone E: Clean-up and Removal

**Objectives:**
- Remove Dagger completely
- Remove ComponentManager and bridge
- Enforce module boundaries

**Tasks:**
1. Migrate remaining Dagger injection sites to Hilt
2. Replace `componentManager().xyz.get().inject(this)` with `@Inject`
3. Delete `ComponentManager.kt` and bridge layer
4. Delete all Dagger `@Component`, `@Module`, `@Subcomponent` definitions
5. Verify domain module has no Android imports
6. Remove unused code from `:presentation`

**Done when:**
- [ ] No Dagger dependencies in build files
- [ ] No `ComponentManager` references
- [ ] `@Inject` and `@HiltViewModel` used everywhere
- [ ] Domain module is pure Kotlin
- [ ] Build passes with Hilt only

---

## Clean Code Conventions

- No singletons holding large lists in memory
- ViewModel state is immutable `data class` + `StateFlow`
- One-time events via `SharedFlow` / `Channel`
- Use cases named by intent: `RestoreSession`, `LoadPagedSpaces`, `OpenDefaultSpace`
- Repository interfaces in domain, implementations in data
- UI doesn't know DTOs, only domain models

---

## Acceptance Criteria

- [ ] Cold start does not request "all spaces"
- [ ] Memory remains stable for accounts with hundreds of spaces
- [ ] Spaces list loads in pages, scroll triggers next page
- [ ] Deep link opens correct destination without full preload
- [ ] Dagger removed, Hilt used everywhere
- [ ] Navigation is Compose-based; legacy screens still reachable
- [ ] All existing tests pass
- [ ] No regression in app functionality

---

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Dual DI systems cause double instantiation | Medium | High | Careful scope management, integration tests |
| Navigation regression breaks deep links | Medium | High | Document all routes before migration, E2E tests |
| Backend doesn't support cursor pagination | Low | High | Implement offset-based first, abstract for cursor |
| ComponentManager bridge adds complexity | Medium | Medium | Time-box to 2 weeks, delete in Milestone E |
| Legacy screens break during navigation migration | Medium | Medium | Keep Fragment wrappers until Compose conversion |

---

## Open Questions

1. Does the Go middleware support cursor-based pagination for spaces?
2. Should we use Room + RemoteMediator for spaces cache, or in-memory only?
3. What's the target page size for spaces (10? 20? 50?)?
4. Should auth module be a separate Gradle module or package within `:app`?

---

## References

- Current DI: `/app/src/main/java/com/anytypeio/anytype/di/`
- ComponentManager: `/app/src/main/java/com/anytypeio/anytype/di/common/ComponentManager.kt`
- Navigation graph: `/app/src/main/res/navigation/graph.xml`
- Spaces subscription: `SpaceViewSubscriptionContainer.kt`
- MainActivity: `/app/src/main/java/com/anytypeio/anytype/ui/main/MainActivity.kt`
