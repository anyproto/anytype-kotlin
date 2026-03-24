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

New sealed class emitted via `SharedFlow<CreateObjectNavigation>`:

```kotlin
sealed class CreateObjectNavigation {
    data class OpenEditor(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenSet(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenChat(val id: Id, val space: SpaceId) : CreateObjectNavigation()
}
```

Layout-to-destination mapping mirrors `CreateObjectFragment`'s existing logic:
- `COLLECTION`, `SET` → `OpenSet`
- `CHAT` → `OpenChat`
- Everything else → `OpenEditor`

### 2.3 VmParams Expansion

Add optional `typeKey: Key?` parameter for pre-selected type scenarios (OS widget deeplinks).

When `typeKey` is provided, the VM skips the type list and immediately creates the object.

### 2.4 Dependencies

`CreateObjectFeatureDependencies` interface gains:

```kotlin
fun createObjectByTypeAndTemplate(): CreateObjectByTypeAndTemplate
fun spaceManager(): SpaceManager
fun awaitAccountStartManager(): AwaitAccountStartManager
```

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

### Migration pattern per call site:

```kotlin
if (BuildConfig.USE_NEW_CREATE_OBJECT) {
    // 1. Get/create CreateObjectFeatureComponent from ComponentManager
    // 2. Show CreateObjectBottomSheet (Compose in ComposeView or existing Compose host)
    // 3. Collect CreateObjectNavigation from VM → navigate to editor/set/chat
} else {
    // Existing fragment-based flow (unchanged)
}
```

### MainActivity deeplink handling (2 call sites):

- `Command.OpenCreateNewType`: Branch on flag — new path launches bottom sheet with `VmParams(typeKey = typeKey)`
- `Command.Deeplink.OpenCreateObjectFromOsWidget`: Same branching

### Media actions forwarding:

`SelectPhotos`, `TakePhoto`, `SelectFiles`, `AttachExistingObject` actions are forwarded from the bottom sheet to the calling screen via the action callback lambda. The calling screen handles them (camera intent, file picker, etc.).

### Out of scope:

- `WidgetSourceTypeFragment` / `WidgetSourceTypeListener` — different use case (selecting widget source type, not creating objects)

## Section 4: DI Changes

### CreateObjectFeatureComponent expansion:

```kotlin
@Component(dependencies = [CreateObjectFeatureDependencies::class])
@PerScreen
interface CreateObjectFeatureComponent {
    // Factory stays the same, VmParams expanded
}

interface CreateObjectFeatureDependencies : ComponentDependencies {
    fun storeOfObjectTypes(): StoreOfObjectTypes                    // existing
    fun spaceViewSubscription(): SpaceViewSubscriptionContainer     // existing
    fun createObjectByTypeAndTemplate(): CreateObjectByTypeAndTemplate // new
    fun spaceManager(): SpaceManager                                 // new
    fun awaitAccountStartManager(): AwaitAccountStartManager         // new
}
```

### ComponentManager:

Existing `createObjectFeatureComponent` entry unchanged structurally — `findComponentDependencies()` resolves new dependencies from `MainComponent` which already provides them.

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
- `CreateObjectWidgetConfigDI` (OS widget config)
