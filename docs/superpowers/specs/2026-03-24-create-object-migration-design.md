# Migrate Legacy Create-Object Flow to feature-create-object Module

**Date:** 2026-03-24
**Status:** Approved

## Goal

Replace the legacy `CreateObjectFragment`/`CreateObjectViewModel`/`SelectObjectTypeBaseFragment` flow with the new `feature-create-object` module across all call sites, behind a `BuildConfig` feature flag.

## Context

The `feature-create-object` module was introduced in commit `20822f0c4` (DROID-4201) with a redesigned UI (Compose-based bottom sheet and popup). It is fully built but not wired into any screen. The legacy flow is spread across 8+ screens via fragment-based listener interfaces.

**Legacy code to replace:**
- `CreateObjectFragment` + `CreateObjectViewModel` (creation + post-creation navigation)
- `SelectObjectTypeBaseFragment` subclasses (`ObjectTypeSelectionFragment`, `WidgetSourceTypeFragment` — note: `WidgetSourceTypeFragment` is out of scope, different use case)
- `SelectObjectTypeViewModel` (type listing with pin/default features — dropped in new flow)
- Listener interfaces: `ObjectTypeSelectionListener`, `CollectionObjectTypeSelectionListener`
- DI: `CreateObjectSubComponent`, `SelectObjectTypeComponent`
- Nav graph: `action_global_createObjectFragment`

**New code:**
- `NewCreateObjectViewModel` (type listing + filtering + creation + navigation)
- `CreateObjectBottomSheet` / `CreateObjectPopup` (Compose UI)
- `CreateObjectFeatureComponent` (DI)

## Design Decisions

- **Templates:** Skip template picker — use default template via `CreateObjectByTypeAndTemplate` internally
- **Pin/default type features:** Dropped — widget sort order is sufficient
- **Feature flag:** `BuildConfig.USE_NEW_CREATE_OBJECT` (compile-time, rebuild to toggle)
- **Cleanup:** Legacy code removed after one release cycle with flag set to `true`

## Section 1: Feature Flag

Add `USE_NEW_CREATE_OBJECT` boolean `buildConfigField` in `app/build.gradle`:

```groovy
buildTypes {
    debug {
        buildConfigField "boolean", "USE_NEW_CREATE_OBJECT", "false"
    }
    release {
        buildConfigField "boolean", "USE_NEW_CREATE_OBJECT", "false"
    }
}
```

All branching uses `BuildConfig.USE_NEW_CREATE_OBJECT`.

## Section 2: NewCreateObjectViewModel Expansion

The VM currently handles type listing/filtering only. It must gain:

### 2.1 Object Creation

Add `CreateObjectByTypeAndTemplate` use case as a dependency. When `CreateObjectAction.CreateObjectOfType` is dispatched, the VM:
1. Calls `CreateObjectByTypeAndTemplate` with the selected type key
2. Emits a navigation result based on the created object's layout

### 2.2 Navigation Result

New sealed class emitted via `Channel(Channel.BUFFERED)` exposed as `Flow` via `receiveAsFlow()` (not `SharedFlow`, to avoid losing one-shot events during config changes):

```kotlin
sealed class CreateObjectNavigation {
    data class OpenEditor(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenSet(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenChat(val id: Id, val space: SpaceId) : CreateObjectNavigation()
}
```

Layout-to-destination mapping mirrors `CreateObjectFragment`'s existing logic:
- `COLLECTION`, `SET` → `OpenSet`
- `CHAT`, `CHAT_DERIVED` → `OpenChat`
- Everything else → `OpenEditor`

**Dismiss behavior:** The bottom sheet auto-dismisses before navigation to avoid remaining visible on back press. On creation failure, an error state is shown within the bottom sheet with a retry option (matching the existing error/retry pattern in `NewCreateObjectViewModel`).

### 2.3 VmParams Expansion

Add optional `typeKey: TypeKey?` parameter for pre-selected type scenarios (OS widget deeplinks). Note: use `TypeKey` wrapper class (not the `Key` string typealias) to match `CreateObjectByTypeAndTemplate.Param`.

When `typeKey` is provided, the VM skips the type list and immediately creates the object.

### 2.4 Dependencies

The VM needs `CreateObjectByTypeAndTemplate`, `SpaceManager`, and `AppCoroutineDispatchers`. These are provided via a **Dagger module inside the component** (not via the dependencies interface), matching the existing pattern in `CreateObjectDI.kt` (lines 37-46) where use cases are instantiated per-screen from lower-level deps.

New `CreateObjectFeatureModule` added to the component:

```kotlin
@Module
object CreateObjectFeatureModule {
    @Provides
    @PerScreen
    fun provideCreateObjectByTypeAndTemplate(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CreateObjectByTypeAndTemplate = CreateObjectByTypeAndTemplate(repo, dispatchers)
}
```

`CreateObjectFeatureDependencies` interface gains:

```kotlin
fun blockRepository(): BlockRepository           // new — provided by MainComponent
fun dispatchers(): AppCoroutineDispatchers        // new — provided by MainComponent
fun spaceManager(): SpaceManager                  // new — provided by MainComponent
```

`AwaitAccountStartManager` is **not** needed — the new VM is only used from within active screens where the account is already started. The OS widget deeplink path goes through `MainActivity` which already awaits account start before dispatching commands.

## Section 3: Call Site Migration

Each screen branches on the flag. The 8 call sites:

| Screen | File | Current | New |
|--------|------|---------|-----|
| WidgetsScreenFragment | `app/.../ui/home/WidgetsScreenFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| EditorFragment | `app/.../ui/editor/EditorFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| CollectionFragment | `app/.../ui/widgets/collection/CollectionFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| AllContentFragment | `app/.../ui/allcontent/AllContentFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| DateObjectFragment | `app/.../ui/date/DateObjectFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| ObjectSetFragment | `app/.../ui/sets/ObjectSetFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| SpaceSettingsFragment | `app/.../ui/settings/space/SpaceSettingsFragment.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |
| CreateObjectWidgetConfigActivity | `app/.../ui/oswidgets/CreateObjectWidgetConfigActivity.kt` | `ObjectTypeSelectionFragment` | `CreateObjectBottomSheet` |

### Compose hosting approach — `CreateObjectDialogFragment` wrapper:

Create a new `CreateObjectDialogFragment` (extends `BaseBottomSheetComposeFragment`) that:
1. Hosts the `CreateObjectBottomSheet` composable
2. Owns the `NewCreateObjectViewModel` (is the `ViewModelStoreOwner`)
3. Injects via `ComponentManager.createObjectFeatureComponent`
4. Releases the component in `onDestroy()`
5. Communicates results back to the caller via `FragmentResult` API (`setFragmentResult`)

This matches the existing pattern used by `ObjectTypeSelectionFragment` (which also extends a base compose fragment and is shown via `dialog.show(childFragmentManager, ...)`). All 8 call sites show this wrapper fragment the same way, regardless of whether the host is View-based or Compose-based.

### Migration pattern per call site:

```kotlin
if (BuildConfig.USE_NEW_CREATE_OBJECT) {
    val dialog = CreateObjectDialogFragment.new(space = spaceId)
    dialog.show(childFragmentManager, TAG)
    // Listen for results via FragmentResultListener:
    //   - CreateObjectNavigation → navigate to editor/set/chat
    //   - CreateObjectAction (media) → handle camera/file picker
} else {
    // Existing fragment-based flow (unchanged)
}
```

### MainActivity deeplink handling (2 additional call sites):

These are distinct from the 8 UI call sites — they don't show a type picker. They directly create an object with a pre-known type.

- `Command.OpenCreateNewType`: Branch on flag — new path instantiates `NewCreateObjectViewModel` with `VmParams(typeKey = typeKey)` for immediate creation, then navigates based on the result
- `Command.Deeplink.OpenCreateObjectFromOsWidget`: Same branching

### Media actions forwarding:

`SelectPhotos`, `TakePhoto`, `SelectFiles`, `AttachExistingObject` actions are forwarded from the dialog to the calling screen via `FragmentResult`. The calling screen handles them (camera intent, file picker, etc.).

### Out of scope:

- `WidgetSourceTypeFragment` / `WidgetSourceTypeListener` — different use case (selecting widget source type, not creating objects)
- `CollectionAddObjectTypeFragment` / `CollectionObjectTypeSelectionListener` — used by `ObjectSetFragment` for a different action (selecting a type for collection view filtering, not creating objects). Remains on the legacy path.
- `AppDefaultObjectTypeFragment` — used by `SpaceSettingsFragment` for setting the default object type, not creating objects. Remains on the legacy path.

## Section 4: DI Changes

### CreateObjectFeatureComponent expansion:

```kotlin
@Component(
    dependencies = [CreateObjectFeatureDependencies::class],
    modules = [CreateObjectFeatureModule::class]
)
@PerScreen
interface CreateObjectFeatureComponent {
    // Factory stays the same, VmParams expanded
}

interface CreateObjectFeatureDependencies : ComponentDependencies {
    fun storeOfObjectTypes(): StoreOfObjectTypes                    // existing
    fun spaceViewSubscription(): SpaceViewSubscriptionContainer     // existing
    fun blockRepository(): BlockRepository                          // new
    fun dispatchers(): AppCoroutineDispatchers                      // new
    fun spaceManager(): SpaceManager                                // new
}
```

All new dependencies (`BlockRepository`, `AppCoroutineDispatchers`, `SpaceManager`) are already provided by `MainComponent` and resolved via `findComponentDependencies()`.

### ComponentManager:

Existing `createObjectFeatureComponent` entry unchanged structurally — `findComponentDependencies()` resolves the new dependencies from `MainComponent`.

### VM lifecycle and ownership:

The `NewCreateObjectViewModel` is scoped to a new `CreateObjectDialogFragment` wrapper (see Section 3). The wrapper Fragment is the `ViewModelStoreOwner`. `ComponentManager.createObjectFeatureComponent.release()` is called in the wrapper's `onDestroy()`.

### No new components:

Legacy `CreateObjectSubComponent` and `SelectObjectTypeComponent` remain for the old path behind the flag.

## Section 5: Testing

### Unit tests:

Expand `NewCreateObjectViewModelTest` to cover:
- Object creation success → correct `CreateObjectNavigation` emitted per layout type
- Object creation failure → error state
- Pre-selected `typeKey` → immediate creation without type list
- No new test classes needed

### Flag lifecycle:

1. Ship with `USE_NEW_CREATE_OBJECT = false`
2. QA validates with `true` in debug builds
3. Flip to `true` for release after QA sign-off
4. After one stable release cycle, remove:
   - `BuildConfig` field
   - All `if/else` branches (keep new path only)
   - `CreateObjectFragment`, `CreateObjectViewModel`, `CreateObjectSubComponent`
   - `SelectObjectTypeBaseFragment`, `ObjectTypeSelectionFragment`, `SelectObjectTypeViewModel`, `SelectObjectTypeComponent`
   - `ObjectTypeSelectionListener`, `CollectionObjectTypeSelectionListener` interfaces
   - Nav graph entry `createObjectFragment` and `action_global_createObjectFragment`

### Permanently retained:

- `WidgetSourceTypeFragment` / `WidgetSourceTypeListener`
- `CollectionAddObjectTypeFragment` / `CollectionObjectTypeSelectionListener`
- `AppDefaultObjectTypeFragment`
- `CreateObjectWidgetConfigDI` (OS widget config)

Note: `ObjectTypeSelectionListener` interface is shared by some retained fragments. During cleanup, verify which listeners are still referenced before deleting.
