# Kanban Layout Menu Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** When the Kanban (Board) layout is selected in the "Edit View → Layout" bottom sheet, show Kanban-specific controls — **Group by** (relation picker), **Color columns** (toggle), **Show icon** (existing toggle) — and persist them to the viewer.

**Architecture:** Pure UI + ViewModel wiring. The Kanban board renderer, grouping, and middleware mapping already exist (merged via DROID-4529 #3282). We extend the existing `ViewerLayoutWidgetUi` state, the `ViewerLayoutWidget` Compose sheet, a new nested `ViewerLayoutGroupByWidget` picker (copy of `ViewerLayoutCoverWidget`), and three new branches in `ObjectSetViewModel.onViewerLayoutWidgetAction`. Persistence reuses the existing `proceedWithUpdateViewer` → `UpdateDataViewViewer` path; `groupRelationKey` / `groupBackgroundColors` / `hideIcon` are already mapped in middleware. **No changes to `core-models`, `middleware`, `domain`, or the board renderer.**

**Tech Stack:** Kotlin, Jetpack Compose, Material3 `ModalBottomSheet`, Dagger, JUnit + Mockito (Robolectric for some tests), kotlinx-coroutines-test.

**Design doc:** `docs/superpowers/specs/2026-06-30-kanban-layout-menu-design.md`

**Key reference files (read before starting):**
- State: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetUi.kt`
- Sheet: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutWidget.kt`
- Cover picker (pattern to copy): `core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutCoverWidget.kt`
- VM handler: `ObjectSetViewModel.onViewerLayoutWidgetAction` (`presentation/.../sets/ObjectSetViewModel.kt`, ~line 4199) and `proceedWithUpdateViewer` (~line 4328)
- Relation-format icons: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/extensions/ResExtension.kt` → `fun RelationFormat.simpleIcon(): Int`
- `ColumnItem` composable: `core-ui/.../widgets/dv/ViewerEditWidget.kt:355`
- Board grouping (defines which formats become columns): `presentation/.../sets/BoardViewMapper.kt`, `BoardColumnQuery.kt`

**Conventions:**
- Run a single unit test: `./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.sets.<ClassName>"`
- Run all unit tests: `make test_debug_all`
- Commit message prefix: `DROID-4529 ...`
- Co-author trailer on every commit:
  `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`

---

## Task 1: Extend `ViewerLayoutWidgetUi` state (fields, types, actions, `getGroupByItems`)

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetUi.kt`
- Test: `presentation/src/test/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetStateTest.kt` (create)

### Step 1: Write the failing test

Create `presentation/src/test/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetStateTest.kt`:

```kotlin
package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.MockDataFactory
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewerLayoutWidgetStateTest {

    private fun relation(
        key: String,
        format: RelationFormat,
        name: String,
        isHidden: Boolean = false
    ): ObjectWrapper.Relation = ObjectWrapper.Relation(
        map = mapOf(
            Relations.RELATION_KEY to key,
            Relations.RELATION_FORMAT to format.code.toDouble(),
            Relations.NAME to name,
            Relations.IS_HIDDEN to isHidden
        )
    )

    private suspend fun store(vararg relations: ObjectWrapper.Relation): StoreOfRelations =
        DefaultStoreOfRelations().apply { merge(relations.toList()) }

    private fun viewer(
        groupRelationKey: String? = null,
        groupBackgroundColors: Boolean = false
    ): Block.Content.DataView.Viewer = Block.Content.DataView.Viewer(
        id = "view-1",
        name = "View",
        type = Block.Content.DataView.Viewer.Type.BOARD,
        sorts = emptyList(),
        filters = emptyList(),
        viewerRelations = emptyList(),
        groupRelationKey = groupRelationKey,
        groupBackgroundColors = groupBackgroundColors
    )

    @Test
    fun `groupByItems includes only status, tag and checkbox relations`() = runTest {
        val status = relation("k_status", RelationFormat.STATUS, "Status")
        val tag = relation("k_tag", RelationFormat.TAG, "Tag")
        val checkbox = relation("k_done", RelationFormat.CHECKBOX, "Done")
        val text = relation("k_text", RelationFormat.SHORT_TEXT, "Text")
        val storeOfRelations = store(status, tag, checkbox, text)
        val links = listOf(
            RelationLink("k_status", RelationFormat.STATUS),
            RelationLink("k_tag", RelationFormat.TAG),
            RelationLink("k_done", RelationFormat.CHECKBOX),
            RelationLink("k_text", RelationFormat.SHORT_TEXT)
        )

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(groupRelationKey = "k_tag"),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertEquals(
            listOf("k_status", "k_tag", "k_done"),
            result.groupByItems.map { it.relationKey.key }
        )
        assertTrue(result.groupByItems.single { it.relationKey.key == "k_tag" }.isChecked)
        assertEquals(false, result.groupByItems.single { it.relationKey.key == "k_status" }.isChecked)
    }

    @Test
    fun `groupByItems excludes hidden relations`() = runTest {
        val hidden = relation("k_hidden", RelationFormat.STATUS, "Hidden", isHidden = true)
        val visible = relation("k_status", RelationFormat.STATUS, "Status")
        val storeOfRelations = store(hidden, visible)
        val links = listOf(
            RelationLink("k_hidden", RelationFormat.STATUS),
            RelationLink("k_status", RelationFormat.STATUS)
        )

        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(),
            storeOfRelations = storeOfRelations,
            relationLinks = links
        )

        assertEquals(listOf("k_status"), result.groupByItems.map { it.relationKey.key })
    }

    @Test
    fun `groupBackgroundColors reflects viewer`() = runTest {
        val storeOfRelations = store()
        val result = ViewerLayoutWidgetUi.init().updateState(
            viewer = viewer(groupBackgroundColors = true),
            storeOfRelations = storeOfRelations,
            relationLinks = emptyList()
        )
        assertTrue(result.groupBackgroundColors.toggled)
    }
}
```

**Note for executor:** Verify the exact constructor/field names against `ObjectWrapper.Relation`, `RelationLink`, and `Relations.*` constants in this repo before running (e.g. `Relations.RELATION_KEY`, `Relations.RELATION_FORMAT`, `Relations.IS_HIDDEN`). Adjust the `relation(...)` builder map keys to match. Confirm `RelationFormat.code` is the int code field (used `format.code.toDouble()`); if the property name differs, fix it. The existing `ObjectSetSettingsViewModelTest` (`presentation/src/test/.../relations/ObjectSetSettingsViewModelTest.kt`) shows the `DefaultStoreOfRelations().merge(...)` + `RelationLink(...)` pattern — mirror its relation construction.

### Step 2: Run test to verify it fails

Run: `./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetStateTest"`
Expected: COMPILE FAIL — `groupByItems`, `groupBackgroundColors` are unresolved.

### Step 3: Implement the state changes

In `ViewerLayoutWidgetUi.kt`:

**(a) Add constructor fields** (after `kanbanEnabled`, all with defaults so existing call sites/previews still compile):

```kotlin
data class ViewerLayoutWidgetUi(
    val viewer: Id?,
    val showWidget: Boolean,
    val layoutType: DVViewerType,
    val withIcon: State.Toggle.WithIcon,
    val fitImage: State.Toggle.FitImage,
    val cardSize: State.CardSize,
    val imagePreviewItems: List<State.ImagePreview>,
    val showCardSize: Boolean,
    val showCoverMenu: Boolean,
    /** Whether the experimental Kanban (Board) layout is offered in the type picker. */
    val kanbanEnabled: Boolean = false,
    /** Kanban: relations the board can be grouped by (Status/Tag/Checkbox). */
    val groupByItems: List<State.GroupBy> = emptyList(),
    /** Kanban: groupBackgroundColors — colorize columns by group option color. */
    val groupBackgroundColors: State.Toggle.ColorColumns = State.Toggle.ColorColumns(false),
    /** Kanban: visibility of the nested "Group by" relation picker. */
    val showGroupByMenu: Boolean = false
) {
```

**(b) Add the `ColorColumns` toggle** inside `sealed class Toggle`:

```kotlin
sealed class Toggle : State() {
    abstract val toggled: Boolean

    data class FitImage(override val toggled: Boolean) : Toggle()
    data class WithIcon(override val toggled: Boolean) : Toggle()
    data class ColorColumns(override val toggled: Boolean) : Toggle()
}
```

**(c) Add the `GroupBy` state type** inside `sealed class State` (after `ImagePreview`):

```kotlin
data class GroupBy(
    val relationKey: RelationKey,
    val name: String,
    val format: RelationFormat,
    val isChecked: Boolean
) : State()
```

**(d) Add new actions** inside `sealed class Action`:

```kotlin
data class ColorColumns(val toggled: Boolean) : Action()
data object GroupByMenu : Action()
data class GroupByUpdate(val item: State.GroupBy) : Action()
```

**(e) Populate state in `updateState`** — add the two new fields to the returned `copy(...)`:

```kotlin
    return this.copy(
        viewer = viewer.id,
        layoutType = viewer.type,
        withIcon = ViewerLayoutWidgetUi.State.Toggle.WithIcon(!viewer.hideIcon),
        fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(viewer.coverFit),
        cardSize = cardSize,
        showCardSize = showCardSize,
        imagePreviewItems = viewer.getImagePreviewItems(
            storeOfRelations = storeOfRelations,
            relationLinks = relationLinks
        ),
        groupBackgroundColors = ViewerLayoutWidgetUi.State.Toggle.ColorColumns(
            viewer.groupBackgroundColors
        ),
        groupByItems = viewer.getGroupByItems(
            storeOfRelations = storeOfRelations,
            relationLinks = relationLinks
        ),
    )
```

**(f) Add the `getGroupByItems` helper** (private extension, near `getImagePreviewItems`):

```kotlin
private val GROUP_BY_FORMATS = setOf(
    RelationFormat.STATUS,
    RelationFormat.TAG,
    RelationFormat.CHECKBOX
)

private suspend fun DVViewer.getGroupByItems(
    storeOfRelations: StoreOfRelations,
    relationLinks: List<RelationLink>
): List<ViewerLayoutWidgetUi.State.GroupBy> {
    val selectedGroupKey = groupRelationKey
    return relationLinks
        .filter { it.format in GROUP_BY_FORMATS }
        .mapNotNull { storeOfRelations.getByKey(it.key) }
        .filter { relation ->
            relation.isValid && relation.isHidden != true && relation.isArchived != true &&
                    !relation.key.isSystemKey()
        }
        .map { relation ->
            ViewerLayoutWidgetUi.State.GroupBy(
                relationKey = RelationKey(relation.key),
                name = relation.name.orEmpty(),
                format = relation.format,
                isChecked = relation.key == selectedGroupKey
            )
        }
}
```

**Note for executor:** confirm `ObjectWrapper.Relation` exposes `.format` (RelationFormat) and `.isReadonlyValue`. The cover helper excludes `isReadonlyValue` (for write targets); grouping only *reads* the relation, so it is intentionally omitted here. If `relation.format` isn't directly available, derive it the same way the cover/relation code does. Keep `getByKey` as the lookup (same as `getValidFileRelations`).

### Step 4: Run test to verify it passes

Run: `./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetStateTest"`
Expected: PASS (3 tests).

### Step 5: Commit

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetUi.kt \
        presentation/src/test/java/com/anytypeio/anytype/presentation/sets/ViewerLayoutWidgetStateTest.kt
git commit -m "DROID-4529 Add Kanban group-by/color-columns state to ViewerLayoutWidgetUi

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: Wire ViewModel action handlers

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt` (`onViewerLayoutWidgetAction`, ~line 4199; `Dismiss` branch ~line 4202)
- Test: `presentation/src/test/java/com/anytypeio/anytype/presentation/sets/main/ObjectSetBoardLayoutMenuTest.kt` (create)

### Step 1: Write the failing test

Create `ObjectSetBoardLayoutMenuTest.kt` extending the existing `ObjectSetViewModelTestSetup` harness (mirror `ObjectSetBoardSubscriptionTest.kt` in the same package for setup — defaultDataViewBlock, stubs, `givenViewModel()`/`createViewModel()` helper, `runTest`). Focus on the deterministic state-flag transitions:

```kotlin
@Test
fun `GroupByMenu action toggles showGroupByMenu`() = runTest {
    // given a VM with the layout widget open on a BOARD viewer
    val vm = buildVmWithBoardLayoutWidgetOpen()   // see note
    assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)

    vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
    assertTrue(vm.viewerLayoutWidgetState.value.showGroupByMenu)

    vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
    assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)
}

@Test
fun `Dismiss closes group-by picker first, then widget`() = runTest {
    val vm = buildVmWithBoardLayoutWidgetOpen()
    vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.GroupByMenu)
    assertTrue(vm.viewerLayoutWidgetState.value.showGroupByMenu)

    vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.Dismiss)
    // first dismiss: picker closes, widget stays
    assertFalse(vm.viewerLayoutWidgetState.value.showGroupByMenu)
    assertTrue(vm.viewerLayoutWidgetState.value.showWidget)

    vm.onViewerLayoutWidgetAction(ViewerLayoutWidgetUi.Action.Dismiss)
    // second dismiss: widget closes
    assertFalse(vm.viewerLayoutWidgetState.value.showWidget)
}
```

**Note for executor — harness setup:** `buildVmWithBoardLayoutWidgetOpen()` is shorthand. Implement it by following `ObjectSetBoardSubscriptionTest`'s existing setup: build a state with a BOARD viewer + `kanbanEnabled = true`, open the layout widget by directly setting `vm.viewerLayoutWidgetState.value = vm.viewerLayoutWidgetState.value.copy(showWidget = true, viewer = <viewerId>, layoutType = DVViewerType.BOARD)` (the simplest deterministic seed — the menu-visibility transitions don't depend on the MW round-trip). If the existing test exposes a higher-level "open layout widget" path (e.g. via `ViewEditAction.Layout`), prefer that.

**Optional persistence test (follow existing UpdateView assertion pattern if the harness stubs the repo):** assert that `ColorColumns(true)` and `GroupByUpdate(item)` call `updateDataViewViewer` (or `repo.updateDataViewViewer`) with a viewer whose `groupBackgroundColors == true` / `groupRelationKey == item.relationKey.key`. Mirror how existing tests verify `proceedWithUpdateViewer` results (search the `ObjectSet*Test` files for `updateDataViewViewer` verification). If that verification scaffolding isn't readily available, rely on Task 1's state-mapper coverage + manual QA and keep only the two visibility tests here.

### Step 2: Run test to verify it fails

Run: `./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.sets.main.ObjectSetBoardLayoutMenuTest"`
Expected: COMPILE FAIL — `Action.GroupByMenu` / `showGroupByMenu` unresolved (until Step 3 handlers exist) and/or assertion fail.

### Step 3: Implement the handlers

In `ObjectSetViewModel.onViewerLayoutWidgetAction`, add three branches (place near the existing `CoverMenu` / `Icon` / `ImagePreviewUpdate` branches):

```kotlin
is ViewerLayoutWidgetUi.Action.ColorColumns -> {
    viewModelScope.launch {
        proceedWithUpdateViewer(
            viewerId = viewerLayoutWidgetState.value.viewer
        ) { it.copy(groupBackgroundColors = action.toggled) }
    }
}
ViewerLayoutWidgetUi.Action.GroupByMenu -> {
    val isVisible = viewerLayoutWidgetState.value.showGroupByMenu
    viewerLayoutWidgetState.value =
        viewerLayoutWidgetState.value.copy(showGroupByMenu = !isVisible)
}
is ViewerLayoutWidgetUi.Action.GroupByUpdate -> {
    if (!action.item.isChecked) {
        viewModelScope.launch {
            proceedWithUpdateViewer(
                viewerId = viewerLayoutWidgetState.value.viewer
            ) { it.copy(groupRelationKey = action.item.relationKey.key) }
        }
    } else {
        Timber.i("Group-by relation [${action.item.relationKey.key}] already set")
    }
    viewerLayoutWidgetState.value =
        viewerLayoutWidgetState.value.copy(showGroupByMenu = false)
}
```

**Extend the existing `Dismiss` branch** so an open group-by picker closes first (mirror the `showCoverMenu` handling already there):

```kotlin
ViewerLayoutWidgetUi.Action.Dismiss -> {
    val current = viewerLayoutWidgetState.value
    viewerLayoutWidgetState.value = when {
        current.showCoverMenu -> current.copy(showCoverMenu = false)
        current.showGroupByMenu -> current.copy(showGroupByMenu = false)
        else -> current.copy(
            showWidget = false,
            showCardSize = false,
            showCoverMenu = false,
            showGroupByMenu = false
        )
    }
}
```

**Note for executor:** `when (action)` over a sealed class must stay exhaustive — adding these branches is required for it to compile. Keep `import timber.log.Timber` (already imported).

### Step 4: Run test to verify it passes

Run: `./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.sets.main.ObjectSetBoardLayoutMenuTest"`
Expected: PASS.

### Step 5: Commit

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt \
        presentation/src/test/java/com/anytypeio/anytype/presentation/sets/main/ObjectSetBoardLayoutMenuTest.kt
git commit -m "DROID-4529 Handle Kanban group-by/color-columns actions in ObjectSetViewModel

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 3: Add string resources

**Files:**
- Modify: `localization/src/main/res/values/strings.xml`

### Step 1: Add strings

Add near the existing `view_layout_widget_title` / `view_layout_cover_widget_title` entries (search for `view_layout_cover_widget_title`):

```xml
<string name="view_layout_group_by_widget_title">Group by</string>
<string name="group_by">Group by</string>
<string name="color_columns">Color columns</string>
```

(`Show icon` reuses the existing `<string name="icon">Icon</string>`.)

### Step 2: Verify it compiles

Run: `./gradlew :localization:assembleDebug` (or rely on Task 5's build).
Expected: SUCCESS.

### Step 3: Commit

```bash
git add localization/src/main/res/values/strings.xml
git commit -m "DROID-4529 Add Group by / Color columns strings

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 4: Create the `ViewerLayoutGroupByWidget` picker

**Files:**
- Create: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutGroupByWidget.kt`

### Step 1: Implement the widget (copy of `ViewerLayoutCoverWidget`)

```kotlin
package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewerLayoutGroupByWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.showGroupByMenu) {
        ModalBottomSheet(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .fillMaxWidth()
                .wrapContentHeight(),
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            onDismissRequest = { action(Dismiss) },
            sheetState = bottomSheetState,
            dragHandle = { DragHandle() },
            content = {
                GroupByContent(uiState = uiState, action = action)
            }
        )
    }
}

@Composable
private fun ColumnScope.GroupByContent(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = stringResource(R.string.view_layout_group_by_widget_title),
        style = Title1,
        color = colorResource(R.color.text_primary)
    )
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 250.dp)
    ) {
        items(
            count = uiState.groupByItems.size,
            key = { index -> uiState.groupByItems[index].relationKey.key }
        ) { idx ->
            val item = uiState.groupByItems[idx]
            GroupByItem(
                text = item.name,
                iconDrawableRes = item.format.simpleIcon(),
                checked = item.isChecked
            ) {
                action(ViewerLayoutWidgetUi.Action.GroupByUpdate(item))
            }
        }
    }
}

@Composable
private fun GroupByItem(
    text: String,
    @DrawableRes iconDrawableRes: Int,
    checked: Boolean,
    action: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
            .noRippleThrottledClickable { action() }
    ) {
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterStart),
            painter = painterResource(id = iconDrawableRes),
            contentDescription = "Relation format icon"
        )
        Text(
            modifier = Modifier
                .padding(start = 34.dp)
                .align(Alignment.CenterStart),
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        if (checked) {
            Image(
                modifier = Modifier.align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_option_checked_black),
                contentDescription = "Checked"
            )
        }
    }
    Divider()
}
```

**Notes for executor:**
- `DragHandle()` is a private/internal composable already used by `ViewerLayoutWidget`/`ViewerLayoutCoverWidget` in this package — reuse it (same package, no import needed). If it isn't visible from this new file, copy its definition or hoist it.
- Confirm `fun RelationFormat.simpleIcon(): Int` is in package `com.anytypeio.anytype.core_ui.extensions` (file `ResExtension.kt`) and import it accordingly.
- `ic_option_checked_black` and the color resources are the same ones `ViewerLayoutCoverWidget` already uses — verified present.

### Step 2: Verify it compiles

Run: `./gradlew :core-ui:assembleDebug`
Expected: SUCCESS (widget is not yet referenced; this just checks it compiles).

### Step 3: Commit

```bash
git add core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutGroupByWidget.kt
git commit -m "DROID-4529 Add Group by relation picker bottom sheet

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 5: Show Kanban rows in `ViewerLayoutWidget` and wire the picker

**Files:**
- Modify: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutWidget.kt`

### Step 1: Render `ViewerLayoutGroupByWidget` alongside the cover widget

In `ViewerLayoutWidget(...)`, right after the existing `ViewerLayoutCoverWidget(uiState, action)` call (~line 105), add:

```kotlin
        ViewerLayoutGroupByWidget(
            uiState = uiState,
            action = action,
        )
```

(Same package — no import needed.)

### Step 2: Replace alpha-hidden rows with per-layout conditional rows

In `ViewerLayoutContent`, replace the block from `val isGallery = ...` (line 151) through the final `LayoutSwitcherItem` (fit_image, line 195) with conditional composition. The "Show icon" `LayoutSwitcherItem` (lines 146–150) stays as-is above this block.

```kotlin
        val isGallery = currentState.layoutType == DVViewerType.GALLERY
        val isBoard = currentState.layoutType == DVViewerType.BOARD

        if (isGallery) {
            Divider()
            ColumnItem(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                title = stringResource(id = R.string.card_size),
                value = when (currentState.cardSize) {
                    CardSize.Large -> stringResource(id = R.string.large)
                    CardSize.Small -> stringResource(id = R.string.small)
                },
                onClick = { action(CardSizeMenu) },
                arrow = painterResource(id = R.drawable.ic_list_arrow_18),
                imageModifier = Modifier.onGloballyPositioned { coordinates ->
                    if (coordinates.isAttached) {
                        with(coordinates.boundsInRoot()) { updateCurrentCoordinates(this) }
                    } else {
                        updateCurrentCoordinates(Rect.Zero)
                    }
                }
            )
            Divider()
            ColumnItem(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                title = stringResource(id = R.string.cover),
                value = currentCoverItem.getTitle(),
                onClick = { action(ViewerLayoutWidgetUi.Action.CoverMenu) },
                arrow = painterResource(id = R.drawable.ic_arrow_disclosure_18)
            )
            Divider()
            LayoutSwitcherItem(
                text = stringResource(id = R.string.fit_image),
                checked = currentState.fitImage.toggled,
                onCheckedChanged = { action(FitImage(it)) }
            )
        }

        if (isBoard) {
            Divider()
            ColumnItem(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                title = stringResource(id = R.string.group_by),
                value = currentState.groupByItems
                    .firstOrNull { it.isChecked }?.name
                    ?: stringResource(id = R.string.none),
                onClick = { action(ViewerLayoutWidgetUi.Action.GroupByMenu) },
                arrow = painterResource(id = R.drawable.ic_arrow_disclosure_18)
            )
            Divider()
            LayoutSwitcherItem(
                text = stringResource(id = R.string.color_columns),
                checked = currentState.groupBackgroundColors.toggled,
                onCheckedChanged = { action(ViewerLayoutWidgetUi.Action.ColorColumns(it)) }
            )
        }
```

**Notes for executor:**
- The `Divider` import is `com.anytypeio.anytype.core_ui.foundation.Divider`. Its previous use here passed `visible = isGallery`; the plain `Divider()` form is used by `ViewerLayoutCoverWidget`. Confirm `Divider()` has a no-arg overload (the cover widget calls `Divider()`); if it requires `visible`, use `Divider(visible = true)`.
- Because the gallery rows are no longer always-composed, the `updateCurrentCoordinates`/`ViewerLayoutListMenu` (card-size popup) anchor only attaches when Gallery is active — which is correct, since the card-size menu only applies to Gallery.
- The "Show icon" toggle remains shown for all layouts (unchanged behavior). This matches the design.

### Step 3: Update/extend the previews (optional but recommended)

Add a Board preview so the Kanban state renders in Android Studio:

```kotlin
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true, device = Devices.NEXUS_7)
@Composable
fun PreviewKanbanLayoutScreen() {
    ViewerLayoutWidget(
        uiState = ViewerLayoutWidgetUi.init().copy(
            showWidget = true,
            layoutType = DVViewerType.BOARD,
            kanbanEnabled = true,
            groupBackgroundColors = ViewerLayoutWidgetUi.State.Toggle.ColorColumns(true),
            groupByItems = emptyList()
        ),
        action = {}
    )
}
```

(Existing previews construct `ViewerLayoutWidgetUi(...)` positionally; since the new fields have defaults, those previews still compile. If any preview breaks, switch it to `ViewerLayoutWidgetUi.init().copy(...)`.)

### Step 4: Build to verify

Run: `./gradlew :core-ui:assembleDebug`
Expected: SUCCESS.

### Step 5: Commit

```bash
git add core-ui/src/main/java/com/anytypeio/anytype/core_ui/widgets/dv/ViewerLayoutWidget.kt
git commit -m "DROID-4529 Show Kanban Group by / Color columns rows in Layout menu

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 6: Full verification

### Step 1: Run the full unit test suite

Run: `make test_debug_all`
Expected: BUILD SUCCESSFUL, all tests pass (including the two new test classes).

### Step 2: Build the app

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

### Step 3: Lint

Run: `./gradlew lintDebug`
Expected: no new errors introduced by these files.

### Step 4: Manual QA checklist (device/emulator, requires the Kanban experimental flag ON)

1. Open a Set/Collection whose view is Kanban (BOARD).
2. Edit View → Layout: confirm the menu shows **Group by**, **Color columns**, **Show icon** (and NOT Card size / Cover / Fit media).
3. Tap **Group by** → picker lists Status/Tag/Checkbox relations with the current one checked; selecting another re-groups the board and the menu value updates.
4. Toggle **Color columns** → board column header colors appear/disappear; persists after reopening the menu.
5. Toggle **Show icon** → card icons hide/show.
6. Switch layout to Grid/Gallery/List and back → the correct per-layout rows show each time (no dead empty space, no leftover Kanban rows on Gallery).
7. Back/dismiss: an open Group-by picker closes on first back; a second back closes the whole sheet.

### Step 5: Final commit (if any QA fixes were needed)

```bash
git add -A
git commit -m "DROID-4529 Polish Kanban layout menu after QA

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Notes / Out of scope (do NOT implement here)

- **Cover / Fit media** for Kanban cards (mobile cards don't render covers).
- **Page limit** (`pageLimit` not mapped into the Android `DVViewer` model/middleware; board uses default limit).
- **"Add Property"** in the Group-by picker (create new Select/Multi-select/Checkbox relation). Pick-existing only.
- **Analytics** for group-by / color-columns changes (only `CHANGE_VIEW_TYPE` is logged today).
