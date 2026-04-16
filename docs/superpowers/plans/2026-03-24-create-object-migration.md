# Create-Object Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy `CreateObjectFragment`/`SelectObjectTypeBaseFragment` flow with the new `feature-create-object` module across all call sites, behind a `BuildConfig.USE_NEW_CREATE_OBJECT` feature flag.

**Architecture:** Expand `NewCreateObjectViewModel` with object creation and navigation capabilities. Create a `CreateObjectDialogFragment` wrapper to host the Compose bottom sheet in Fragment-based screens. Branch on `BuildConfig` at each of the 8 UI call sites + 2 deeplink call sites.

**Tech Stack:** Kotlin, Jetpack Compose, Dagger 2, Navigation Component, Coroutines/Flow, FragmentResult API

**Spec:** `docs/superpowers/specs/2026-03-24-create-object-migration-design.md`

---

## File Map

### New Files
| File | Responsibility |
|------|---------------|
| `feature-create-object/src/main/java/.../presentation/CreateObjectNavigation.kt` | Navigation result sealed class |
| `app/src/main/java/.../ui/create/CreateObjectDialogFragment.kt` | Bottom sheet dialog fragment wrapper |

### Modified Files
| File | Changes |
|------|---------|
| `app/build.gradle` | Add `USE_NEW_CREATE_OBJECT` BuildConfig field |
| `feature-create-object/src/main/java/.../presentation/NewCreateObjectViewModel.kt` | Add creation logic, navigation channel, VmParams.typeKey |
| `feature-create-object/src/main/java/.../presentation/CreateObjectViewModelFactory.kt` | Add new constructor params |
| `app/src/main/java/.../di/feature/CreateObjectFeatureDI.kt` | Add module, expand dependencies, add inject method |
| `app/src/main/java/.../ui/home/WidgetsScreenFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/editor/EditorFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/widgets/collection/CollectionFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/allcontent/AllContentFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/date/DateObjectFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/sets/ObjectSetFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/settings/space/SpaceSettingsFragment.kt` | Branch on flag |
| `app/src/main/java/.../ui/oswidgets/CreateObjectWidgetConfigActivity.kt` | Branch on flag |
| `app/src/main/java/.../ui/main/MainActivity.kt` | Branch on flag for deeplinks |
| `feature-create-object/src/test/.../presentation/NewCreateObjectViewModelTest.kt` | Add creation/navigation tests |

---

### Task 1: Add BuildConfig Feature Flag

**Files:**
- Modify: `app/build.gradle:129-170` (buildTypes section)

- [ ] **Step 1: Add buildConfigField to debug buildType**

In `app/build.gradle`, inside the `debug` block (after line 151 `buildConfigField "boolean", "SHOW_CHATS", "true"`), add:

```groovy
buildConfigField "boolean", "USE_NEW_CREATE_OBJECT", "false"
```

- [ ] **Step 2: Add buildConfigField to release buildType**

In `app/build.gradle`, inside the `release` block (after line 134 `buildConfigField "boolean", "SHOW_CHATS", "true"`), add:

```groovy
buildConfigField "boolean", "USE_NEW_CREATE_OBJECT", "false"
```

- [ ] **Step 3: Verify build compiles**

Run: `./gradlew :app:assembleDebug --dry-run`
Expected: BUILD SUCCESSFUL (no compilation errors from the new field)

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle
git commit -m "feat: add USE_NEW_CREATE_OBJECT BuildConfig feature flag"
```

---

### Task 2: Add CreateObjectNavigation Sealed Class

**Files:**
- Create: `feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/CreateObjectNavigation.kt`

- [ ] **Step 1: Create the navigation result file**

```kotlin
package com.anytypeio.anytype.feature_create_object.presentation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId

sealed class CreateObjectNavigation {
    data class OpenEditor(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenSet(val id: Id, val space: SpaceId) : CreateObjectNavigation()
    data class OpenChat(val id: Id, val space: SpaceId) : CreateObjectNavigation()
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :feature-create-object:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/CreateObjectNavigation.kt
git commit -m "feat: add CreateObjectNavigation sealed class for post-creation routing"
```

---

### Task 3: Expand NewCreateObjectViewModel with Creation Logic

**Files:**
- Modify: `feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/NewCreateObjectViewModel.kt`
- Modify: `feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/CreateObjectViewModelFactory.kt`

- [ ] **Step 1: Write failing tests for object creation**

Add to `feature-create-object/src/test/java/com/anytypeio/anytype/feature_create_object/presentation/NewCreateObjectViewModelTest.kt`:

```kotlin
@Test
fun `should emit OpenEditor navigation when object with basic layout is created`() = runTest {
    // Given
    val createdObjectId = "created-obj-123"
    val spaceId = SpaceId("test-space")
    val typeKey = TypeKey("ot-page")
    val createResult = CreateObjectByTypeAndTemplate.Result.Success(
        objectId = createdObjectId,
        event = Payload(context = "", events = emptyList()),
        typeKey = typeKey,
        obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to createdObjectId,
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )
        )
    )
    val createObject = mock<CreateObjectByTypeAndTemplate>()
    whenever(createObject.async(any())).thenReturn(Resultat.success(createResult))

    val vm = NewCreateObjectViewModel(
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViewContainer = spaceViewContainer,
        vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId),
        createObjectByTypeAndTemplate = createObject
    )

    // When & Then
    vm.navigation.test {
        vm.onAction(CreateObjectAction.CreateObjectOfType(typeKey = typeKey.key, typeName = "Page"))
        val nav = awaitItem()
        assertIs<CreateObjectNavigation.OpenEditor>(nav)
        assertEquals(createdObjectId, nav.id)
        assertEquals(spaceId, nav.space)
    }
}

@Test
fun `should emit OpenSet navigation when collection layout object is created`() = runTest {
    val createdObjectId = "created-collection-123"
    val spaceId = SpaceId("test-space")
    val typeKey = TypeKey("ot-collection")
    val createResult = CreateObjectByTypeAndTemplate.Result.Success(
        objectId = createdObjectId,
        event = Payload(context = "", events = emptyList()),
        typeKey = typeKey,
        obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to createdObjectId,
                Relations.LAYOUT to ObjectType.Layout.COLLECTION.code.toDouble()
            )
        )
    )
    val createObject = mock<CreateObjectByTypeAndTemplate>()
    whenever(createObject.async(any())).thenReturn(Resultat.success(createResult))

    val vm = NewCreateObjectViewModel(
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViewContainer = spaceViewContainer,
        vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId),
        createObjectByTypeAndTemplate = createObject
    )

    vm.navigation.test {
        vm.onAction(CreateObjectAction.CreateObjectOfType(typeKey = typeKey.key, typeName = "Collection"))
        val nav = awaitItem()
        assertIs<CreateObjectNavigation.OpenSet>(nav)
    }
}

@Test
fun `should emit OpenChat navigation when chat layout object is created`() = runTest {
    val createdObjectId = "created-chat-123"
    val spaceId = SpaceId("test-space")
    val typeKey = TypeKey("ot-chat")
    val createResult = CreateObjectByTypeAndTemplate.Result.Success(
        objectId = createdObjectId,
        event = Payload(context = "", events = emptyList()),
        typeKey = typeKey,
        obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to createdObjectId,
                Relations.LAYOUT to ObjectType.Layout.CHAT.code.toDouble()
            )
        )
    )
    val createObject = mock<CreateObjectByTypeAndTemplate>()
    whenever(createObject.async(any())).thenReturn(Resultat.success(createResult))

    val vm = NewCreateObjectViewModel(
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViewContainer = spaceViewContainer,
        vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId),
        createObjectByTypeAndTemplate = createObject
    )

    vm.navigation.test {
        vm.onAction(CreateObjectAction.CreateObjectOfType(typeKey = typeKey.key, typeName = "Chat"))
        val nav = awaitItem()
        assertIs<CreateObjectNavigation.OpenChat>(nav)
    }
}

@Test
fun `should show error state when object creation fails`() = runTest {
    val spaceId = SpaceId("test-space")
    val typeKey = TypeKey("ot-page")
    val createObject = mock<CreateObjectByTypeAndTemplate>()
    whenever(createObject.async(any())).thenReturn(Resultat.failure(RuntimeException("Creation failed")))

    val vm = NewCreateObjectViewModel(
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViewContainer = spaceViewContainer,
        vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId),
        createObjectByTypeAndTemplate = createObject
    )

    vm.onAction(CreateObjectAction.CreateObjectOfType(typeKey = typeKey.key, typeName = "Page"))

    vm.state.test {
        val state = awaitItem()
        assertNotNull(state.error)
    }
}

@Test
fun `should immediately create object when typeKey is pre-selected`() = runTest {
    val createdObjectId = "created-preselected-123"
    val spaceId = SpaceId("test-space")
    val typeKey = TypeKey("ot-note")
    val createResult = CreateObjectByTypeAndTemplate.Result.Success(
        objectId = createdObjectId,
        event = Payload(context = "", events = emptyList()),
        typeKey = typeKey,
        obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to createdObjectId,
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
            )
        )
    )
    val createObject = mock<CreateObjectByTypeAndTemplate>()
    whenever(createObject.async(any())).thenReturn(Resultat.success(createResult))

    val vm = NewCreateObjectViewModel(
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViewContainer = spaceViewContainer,
        vmParams = NewCreateObjectViewModel.VmParams(spaceId = spaceId, typeKey = typeKey),
        createObjectByTypeAndTemplate = createObject
    )

    // VM should auto-create on init due to pre-selected typeKey
    vm.navigation.test {
        val nav = awaitItem()
        assertIs<CreateObjectNavigation.OpenEditor>(nav)
        assertEquals(createdObjectId, nav.id)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :feature-create-object:testDebugUnitTest`
Expected: FAIL — `NewCreateObjectViewModel` constructor doesn't accept new parameters yet

- [ ] **Step 3: Update VmParams with optional typeKey**

In `NewCreateObjectViewModel.kt`, update the `VmParams` data class (line 173-175):

```kotlin
data class VmParams(
    val spaceId: SpaceId,
    val typeKey: TypeKey? = null
)
```

Add the import: `import com.anytypeio.anytype.core_models.primitives.TypeKey`

- [ ] **Step 4: Add new constructor parameters to ViewModel**

Update the constructor (lines 34-38) to:

```kotlin
class NewCreateObjectViewModel @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val vmParams: VmParams,
    private val createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate
) : ViewModel()
```

Note: `SpaceManager` is NOT needed — we use `vmParams.spaceId` which is always available.

Add imports:
```kotlin
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
```

- [ ] **Step 5: Add navigation channel and creation logic**

After the `_state` declaration (around line 41), add:

```kotlin
private val _navigation = Channel<CreateObjectNavigation>(Channel.BUFFERED)
val navigation = _navigation.receiveAsFlow()
```

Replace the `onAction` method (lines 159-167) with:

```kotlin
fun onAction(action: CreateObjectAction) {
    when (action) {
        is CreateObjectAction.UpdateSearch -> onSearchQueryChanged(action.query)
        is CreateObjectAction.Retry -> retry()
        is CreateObjectAction.CreateObjectOfType -> onCreateObject(
            typeKey = TypeKey(action.typeKey),
            typeName = action.typeName
        )
        else -> { /* Media actions handled by parent component */ }
    }
}

private fun onCreateObject(typeKey: TypeKey, typeName: String) {
    viewModelScope.launch {
        createObjectByTypeAndTemplate.async(
            CreateObjectByTypeAndTemplate.Param(
                typeKey = typeKey,
                space = vmParams.spaceId,
                keys = emptyList()
            )
        ).fold(
            onSuccess = { result ->
                when (result) {
                    is CreateObjectByTypeAndTemplate.Result.Success -> {
                        val layout = result.obj.layout
                        val nav = when (layout) {
                            ObjectType.Layout.COLLECTION,
                            ObjectType.Layout.SET -> CreateObjectNavigation.OpenSet(
                                id = result.objectId,
                                space = vmParams.spaceId
                            )
                            ObjectType.Layout.CHAT,
                            ObjectType.Layout.CHAT_DERIVED -> CreateObjectNavigation.OpenChat(
                                id = result.objectId,
                                space = vmParams.spaceId
                            )
                            else -> CreateObjectNavigation.OpenEditor(
                                id = result.objectId,
                                space = vmParams.spaceId
                            )
                        }
                        _navigation.send(nav)
                    }
                    is CreateObjectByTypeAndTemplate.Result.ObjectTypeNotFound -> {
                        _state.update { it.copy(error = "Object type not found") }
                    }
                }
            },
            onFailure = { e ->
                _state.update { it.copy(error = e.message ?: "Failed to create object") }
            }
        )
    }
}
```

Note: `async()` returns `Resultat<Result>`, so we use `.fold()` to unwrap. This matches the pattern used by the legacy `CreateObjectViewModel`. We use `vmParams.spaceId` consistently (not `spaceManager.get()`) since the space is known at construction time.

- [ ] **Step 6: Add init block for pre-selected typeKey**

At the end of the existing `init` block, add:

```kotlin
vmParams.typeKey?.let { key ->
    onCreateObject(typeKey = key, typeName = "")
}
```

- [ ] **Step 7: Update CreateObjectViewModelFactory**

Update `CreateObjectViewModelFactory.kt` to include the new parameter:

```kotlin
class CreateObjectViewModelFactory @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val vmParams: NewCreateObjectViewModel.VmParams,
    private val createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewCreateObjectViewModel(
            storeOfObjectTypes = storeOfObjectTypes,
            spaceViewContainer = spaceViewContainer,
            vmParams = vmParams,
            createObjectByTypeAndTemplate = createObjectByTypeAndTemplate
        ) as T
    }
}
```

Add import:
```kotlin
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
```

- [ ] **Step 8: Fix existing tests to pass new parameters**

In `NewCreateObjectViewModelTest.kt`, update the test helper that creates the ViewModel. Find where `NewCreateObjectViewModel` is instantiated in existing tests and add the new parameters with mocks:

```kotlin
// Add as class-level test double:
private val createObjectByTypeAndTemplate = mock<CreateObjectByTypeAndTemplate>()
```

Update all existing VM instantiations to include:
```kotlin
createObjectByTypeAndTemplate = createObjectByTypeAndTemplate
```

- [ ] **Step 9: Run all tests**

Run: `./gradlew :feature-create-object:testDebugUnitTest`
Expected: ALL PASS (existing + new tests)

- [ ] **Step 10: Commit**

```bash
git add feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/NewCreateObjectViewModel.kt \
      feature-create-object/src/main/java/com/anytypeio/anytype/feature_create_object/presentation/CreateObjectViewModelFactory.kt \
      feature-create-object/src/test/java/com/anytypeio/anytype/feature_create_object/presentation/NewCreateObjectViewModelTest.kt
git commit -m "feat: expand NewCreateObjectViewModel with object creation and navigation"
```

---

### Task 4: Update DI — CreateObjectFeatureDI

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/di/feature/CreateObjectFeatureDI.kt`

- [ ] **Step 1: Expand CreateObjectFeatureDependencies interface**

Replace the existing `CreateObjectFeatureDependencies` interface (lines 49-52) with:

```kotlin
interface CreateObjectFeatureDependencies : ComponentDependencies {
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun blockRepository(): BlockRepository
    fun logger(): Logger
    fun dispatchers(): AppCoroutineDispatchers
}
```

Note: `SpaceManager` is NOT needed — the VM uses `vmParams.spaceId` directly.

Add imports:
```kotlin
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
```

- [ ] **Step 2: Add CreateObjectFeatureModule @Provides method**

Replace the existing `CreateObjectFeatureModule` (lines 38-47) with:

```kotlin
@Module
object CreateObjectFeatureModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectByTypeAndTemplate(
        repo: BlockRepository,
        logger: Logger,
        dispatchers: AppCoroutineDispatchers
    ): CreateObjectByTypeAndTemplate = CreateObjectByTypeAndTemplate(
        repo = repo,
        logger = logger,
        dispatchers = dispatchers
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: CreateObjectViewModelFactory
        ): ViewModelProvider.Factory
    }
}
```

Note: `BlockRepository`, `Logger`, and `AppCoroutineDispatchers` are resolved by Dagger from the component dependencies interface automatically. No explicit re-provides needed.

- [ ] **Step 3: Add inject method for CreateObjectDialogFragment**

In the `CreateObjectFeatureComponent` interface (line 23), uncomment/replace the commented inject method:

```kotlin
fun inject(fragment: CreateObjectDialogFragment)
```

Add import:
```kotlin
import com.anytypeio.anytype.ui.create.CreateObjectDialogFragment
```

Note: Task 5 must be completed before this step compiles. Commit Tasks 4 and 5 together, or do Task 5 first if working sequentially.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/di/feature/CreateObjectFeatureDI.kt
git commit -m "feat: expand CreateObjectFeatureComponent with creation use case and dependencies"
```

---

### Task 5: Create CreateObjectDialogFragment

**Files:**
- Create: `app/src/main/java/com/anytypeio/anytype/ui/create/CreateObjectDialogFragment.kt`

- [ ] **Step 1: Create the dialog fragment**

```kotlin
package com.anytypeio.anytype.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectAction
import com.anytypeio.anytype.feature_create_object.presentation.CreateObjectNavigation
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_create_object.ui.CreateObjectContent
import javax.inject.Inject

class CreateObjectDialogFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val vm by viewModels<NewCreateObjectViewModel> { factory }

    private val space: String get() = requireArguments().getString(ARG_SPACE, "")
    private val typeKey: String? get() = requireArguments().getString(ARG_TYPE_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            // Use CreateObjectContent directly — NOT CreateObjectBottomSheet.
            // The fragment itself IS the bottom sheet (BaseBottomSheetComposeFragment),
            // so wrapping in ModalBottomSheet would create a double bottom sheet.
            CreateObjectContent(
                state = state,
                onAction = { action ->
                    when (action) {
                        is CreateObjectAction.SelectPhotos,
                        is CreateObjectAction.TakePhoto,
                        is CreateObjectAction.SelectFiles,
                        is CreateObjectAction.AttachExistingObject -> {
                            setFragmentResult(
                                RESULT_KEY_ACTION,
                                bundleOf(RESULT_ACTION_TYPE to action::class.java.simpleName)
                            )
                            dismiss()
                        }
                        else -> vm.onAction(action)
                    }
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        jobs += lifecycleScope.subscribe(vm.navigation) { nav ->
            setFragmentResult(
                RESULT_KEY_NAVIGATION,
                bundleOf(
                    RESULT_NAV_TYPE to nav::class.java.simpleName,
                    RESULT_NAV_ID to when (nav) {
                        is CreateObjectNavigation.OpenEditor -> nav.id
                        is CreateObjectNavigation.OpenSet -> nav.id
                        is CreateObjectNavigation.OpenChat -> nav.id
                    },
                    RESULT_NAV_SPACE to when (nav) {
                        is CreateObjectNavigation.OpenEditor -> nav.space.id
                        is CreateObjectNavigation.OpenSet -> nav.space.id
                        is CreateObjectNavigation.OpenChat -> nav.space.id
                    }
                )
            )
            dismiss()
        }
    }

    override fun injectDependencies() {
        val typeKeyParam = typeKey?.let { TypeKey(it) }
        componentManager().createObjectFeatureComponent.get(
            NewCreateObjectViewModel.VmParams(
                spaceId = SpaceId(space),
                typeKey = typeKeyParam
            )
        ).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().createObjectFeatureComponent.release()
    }

    companion object {
        const val TAG = "CreateObjectDialogFragment"
        private const val ARG_SPACE = "arg.create-object.space"
        private const val ARG_TYPE_KEY = "arg.create-object.type-key"

        const val RESULT_KEY_NAVIGATION = "result.create-object.navigation"
        const val RESULT_KEY_ACTION = "result.create-object.action"
        const val RESULT_NAV_TYPE = "result.nav.type"
        const val RESULT_NAV_ID = "result.nav.id"
        const val RESULT_NAV_SPACE = "result.nav.space"
        const val RESULT_ACTION_TYPE = "result.action.type"

        fun new(space: Id, typeKey: String? = null) = CreateObjectDialogFragment().apply {
            arguments = bundleOf(
                ARG_SPACE to space,
                ARG_TYPE_KEY to typeKey
            )
        }
    }
}
```

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/create/CreateObjectDialogFragment.kt
git commit -m "feat: add CreateObjectDialogFragment wrapper for Compose bottom sheet"
```

---

### Task 6: Migrate WidgetsScreenFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetsScreenFragment.kt:425-428,642-644`

- [ ] **Step 1: Add imports**

Add at the top of the file:

```kotlin
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.create.CreateObjectDialogFragment
```

- [ ] **Step 2: Branch the dialog show site**

Replace lines 425-428:

```kotlin
is Command.OpenObjectCreateDialog -> {
    val dialog = ObjectTypeSelectionFragment.new(
        space = command.space.id
    )
    dialog.show(childFragmentManager, "object-create-dialog")
}
```

With:

```kotlin
is Command.OpenObjectCreateDialog -> {
    if (BuildConfig.USE_NEW_CREATE_OBJECT) {
        val dialog = CreateObjectDialogFragment.new(space = command.space.id)
        dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
    } else {
        val dialog = ObjectTypeSelectionFragment.new(
            space = command.space.id
        )
        dialog.show(childFragmentManager, "object-create-dialog")
    }
}
```

- [ ] **Step 3: Add FragmentResultListener for navigation**

In the `onViewCreated` or `onStart` method, register a result listener. Find the appropriate lifecycle method and add:

```kotlin
childFragmentManager.setFragmentResultListener(
    CreateObjectDialogFragment.RESULT_KEY_NAVIGATION,
    viewLifecycleOwner
) { _, bundle ->
    val navType = bundle.getString(CreateObjectDialogFragment.RESULT_NAV_TYPE)
    val id = bundle.getString(CreateObjectDialogFragment.RESULT_NAV_ID) ?: return@setFragmentResultListener
    val space = bundle.getString(CreateObjectDialogFragment.RESULT_NAV_SPACE) ?: return@setFragmentResultListener
    when (navType) {
        "OpenEditor" -> navigateToEditor(id, space)
        "OpenSet" -> navigateToDataView(id, space)
        "OpenChat" -> navigateToChat(id, space)
    }
}
```

Note: The exact navigation methods (`navigateToEditor`, `navigateToDataView`, `navigateToChat`) must match the existing navigation patterns in this fragment. Check the fragment for existing navigation helpers and adapt accordingly.

- [ ] **Step 4: Verify build compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/home/WidgetsScreenFragment.kt
git commit -m "feat: migrate WidgetsScreenFragment to new create-object flow behind flag"
```

---

### Task 7: Migrate EditorFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/editor/EditorFragment.kt:655-663,2357-2359`

- [ ] **Step 1: Add imports**

```kotlin
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.create.CreateObjectDialogFragment
```

- [ ] **Step 2: Branch the dialog show site**

Replace lines 660-661 (inside the `longClicks` handler):

```kotlin
val dialog = ObjectTypeSelectionFragment.new(space = space)
dialog.show(childFragmentManager, "editor-create-object-of-type-dialog")
```

With:

```kotlin
if (BuildConfig.USE_NEW_CREATE_OBJECT) {
    val dialog = CreateObjectDialogFragment.new(space = space)
    dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
} else {
    val dialog = ObjectTypeSelectionFragment.new(space = space)
    dialog.show(childFragmentManager, "editor-create-object-of-type-dialog")
}
```

- [ ] **Step 3: Add FragmentResultListener for navigation**

Register the result listener in the appropriate lifecycle method, following the same pattern as Task 6 Step 3 but using this fragment's navigation methods.

- [ ] **Step 4: Verify build compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/editor/EditorFragment.kt
git commit -m "feat: migrate EditorFragment to new create-object flow behind flag"
```

---

### Task 8: Migrate CollectionFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/widgets/collection/CollectionFragment.kt:71-74,207-209`

- [ ] **Step 1: Add imports**

```kotlin
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.create.CreateObjectDialogFragment
```

- [ ] **Step 2: Branch the dialog show site (lines 71-74)**

Replace:

```kotlin
onCreateObjectLongClicked = {
    val dialog = ObjectTypeSelectionFragment.new(space = space)
    dialog.show(childFragmentManager, "fullscreen-widget-create-object-type-dialog")
},
```

With:

```kotlin
onCreateObjectLongClicked = {
    if (BuildConfig.USE_NEW_CREATE_OBJECT) {
        val dialog = CreateObjectDialogFragment.new(space = space)
        dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
    } else {
        val dialog = ObjectTypeSelectionFragment.new(space = space)
        dialog.show(childFragmentManager, "fullscreen-widget-create-object-type-dialog")
    }
},
```

- [ ] **Step 3: Add FragmentResultListener**

Same pattern as Task 6 Step 3, adapted to this fragment's navigation.

- [ ] **Step 4: Verify build compiles and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/widgets/collection/CollectionFragment.kt
git commit -m "feat: migrate CollectionFragment to new create-object flow behind flag"
```

---

### Task 9: Migrate AllContentFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/allcontent/AllContentFragment.kt:252-254,279-281`

- [ ] **Step 1: Add imports and branch dialog show site (lines 252-254)**

Replace:

```kotlin
onCreateObjectLongClicked = {
    val dialog = ObjectTypeSelectionFragment.new(space = space)
    dialog.show(childFragmentManager, null)
},
```

With:

```kotlin
onCreateObjectLongClicked = {
    if (BuildConfig.USE_NEW_CREATE_OBJECT) {
        val dialog = CreateObjectDialogFragment.new(space = space)
        dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
    } else {
        val dialog = ObjectTypeSelectionFragment.new(space = space)
        dialog.show(childFragmentManager, null)
    }
},
```

- [ ] **Step 2: Add FragmentResultListener**

Same pattern as Task 6 Step 3.

- [ ] **Step 3: Verify build compiles and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/allcontent/AllContentFragment.kt
git commit -m "feat: migrate AllContentFragment to new create-object flow behind flag"
```

---

### Task 10: Migrate DateObjectFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/date/DateObjectFragment.kt:177-179,210-212`

- [ ] **Step 1: Add imports and branch dialog show site (lines 177-179)**

Replace:

```kotlin
DateObjectCommand.TypeSelectionScreen -> {
    val dialog = ObjectTypeSelectionFragment.new(space = space)
    dialog.show(childFragmentManager, null)
}
```

With:

```kotlin
DateObjectCommand.TypeSelectionScreen -> {
    if (BuildConfig.USE_NEW_CREATE_OBJECT) {
        val dialog = CreateObjectDialogFragment.new(space = space)
        dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
    } else {
        val dialog = ObjectTypeSelectionFragment.new(space = space)
        dialog.show(childFragmentManager, null)
    }
}
```

- [ ] **Step 2: Add FragmentResultListener and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/date/DateObjectFragment.kt
git commit -m "feat: migrate DateObjectFragment to new create-object flow behind flag"
```

---

### Task 11: Migrate ObjectSetFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt:349-357,1594-1596`

Note: Only migrate the `ObjectTypeSelectionListener` path. The `CollectionObjectTypeSelectionListener` / `CollectionAddObjectTypeFragment` path is out of scope.

- [ ] **Step 1: Add imports and branch dialog show site (lines 354-355)**

Replace:

```kotlin
val dialog = ObjectTypeSelectionFragment.new(space = space)
dialog.show(childFragmentManager, "set-create-object-of-type-dialog")
```

With:

```kotlin
if (BuildConfig.USE_NEW_CREATE_OBJECT) {
    val dialog = CreateObjectDialogFragment.new(space = space)
    dialog.show(childFragmentManager, CreateObjectDialogFragment.TAG)
} else {
    val dialog = ObjectTypeSelectionFragment.new(space = space)
    dialog.show(childFragmentManager, "set-create-object-of-type-dialog")
}
```

- [ ] **Step 2: Add FragmentResultListener and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt
git commit -m "feat: migrate ObjectSetFragment to new create-object flow behind flag"
```

---

### Task 12: Migrate SpaceSettingsFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/settings/space/SpaceSettingsFragment.kt:383-385`

Note: This fragment implements `ObjectTypeSelectionListener` but doesn't directly show `ObjectTypeSelectionFragment` — it's shown by a child. The callback at line 383 needs branching.

- [ ] **Step 1: Investigate how the dialog is triggered**

Read the fragment to find where `ObjectTypeSelectionFragment` is shown. It may be triggered by a child fragment or composable. Adapt the branching accordingly — the migration may only need a flag check at the callback level or may need to intercept the dialog show.

- [ ] **Step 2: Apply appropriate branching and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/settings/space/SpaceSettingsFragment.kt
git commit -m "feat: migrate SpaceSettingsFragment to new create-object flow behind flag"
```

---

### Task 13: Migrate CreateObjectWidgetConfigActivity

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/oswidgets/CreateObjectWidgetConfigActivity.kt:104-106,109-111`

**Important:** This activity has fundamentally different behavior from other call sites. It uses the type picker to CONFIGURE a widget (store the type selection), not to CREATE an object. The `CreateObjectDialogFragment` creates objects immediately on type selection, which is wrong here.

**Decision:** Exclude this call site from migration. The OS widget config activity stays on the legacy `ObjectTypeSelectionFragment` path. It is already documented as out-of-scope for the `CreateObjectWidgetConfigDI` in the spec, and the activity's use of `ObjectTypeSelectionFragment` is really "type selection for widget config," not "create an object."

- [ ] **Step 1: Add a code comment documenting the exclusion**

At the top of `showTypeSelectionDialog` in `CreateObjectWidgetConfigActivity.kt`, add:

```kotlin
// Note: This call site intentionally stays on ObjectTypeSelectionFragment.
// The widget config activity selects a type for future creation (stored in preferences),
// it does not create an object immediately. See create-object-migration spec.
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/oswidgets/CreateObjectWidgetConfigActivity.kt
git commit -m "docs: document CreateObjectWidgetConfigActivity exclusion from create-object migration"
```

---

### Task 14: Migrate MainActivity Deeplink Handling

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/main/MainActivity.kt:169-176,375-391`

- [ ] **Step 1: Add imports**

```kotlin
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.create.CreateObjectDialogFragment
```

- [ ] **Step 2: Branch Command.OpenCreateNewType (lines 169-176)**

Replace:

```kotlin
is Command.OpenCreateNewType -> {
    findNavController(R.id.fragment)
        .navigate(
            R.id.action_global_createObjectFragment,
            bundleOf(
                CreateObjectFragment.TYPE_KEY to command.type
            )
        )
}
```

With:

```kotlin
is Command.OpenCreateNewType -> {
    if (BuildConfig.USE_NEW_CREATE_OBJECT) {
        val dialog = CreateObjectDialogFragment.new(
            space = vm.getCurrentSpaceId(),
            typeKey = command.type
        )
        dialog.show(supportFragmentManager, CreateObjectDialogFragment.TAG)
    } else {
        findNavController(R.id.fragment)
            .navigate(
                R.id.action_global_createObjectFragment,
                bundleOf(
                    CreateObjectFragment.TYPE_KEY to command.type
                )
            )
    }
}
```

Note: The exact method name `vm.getCurrentSpaceId()` may differ — check `MainViewModel` for how to get the current space ID.

- [ ] **Step 3: Branch Command.Deeplink.OpenCreateObjectFromOsWidget (lines 375-391)**

Apply the same branching pattern. The new path uses `CreateObjectDialogFragment.new(space = ..., typeKey = command.typeKey)` which will auto-create via the pre-selected typeKey in VmParams.

- [ ] **Step 4: Add FragmentResultListener for navigation results**

Register a result listener in the Activity that navigates to editor/set/chat using the nav controller, matching the existing navigation patterns.

- [ ] **Step 5: Verify build compiles and commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/main/MainActivity.kt
git commit -m "feat: migrate MainActivity deeplink handling to new create-object flow behind flag"
```

---

### Task 15: Final Integration Test

- [ ] **Step 1: Run all unit tests**

Run: `./gradlew :feature-create-object:testDebugUnitTest`
Expected: ALL PASS

- [ ] **Step 2: Run full app compilation**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run lint check**

Run: `./gradlew :app:lintDebug :feature-create-object:lintDebug`
Expected: No new errors

- [ ] **Step 4: Verify flag=false path (default)**

Build and run the app. All create-object flows should behave identically to before (legacy path). Verify at least: home screen create, editor create, collection create.

- [ ] **Step 5: Verify flag=true path**

Change `USE_NEW_CREATE_OBJECT` to `true` in `app/build.gradle`, rebuild, and test:
- Home screen → new bottom sheet appears
- Pick a type → object created → navigated to editor
- Pick a collection type → navigated to set view
- Pick a chat type → navigated to chat

- [ ] **Step 6: Reset flag to false and final commit**

Reset `USE_NEW_CREATE_OBJECT` back to `false` in both build types, then:

```bash
git add app/build.gradle
git commit -m "chore: reset USE_NEW_CREATE_OBJECT flag to false after validation"
```
