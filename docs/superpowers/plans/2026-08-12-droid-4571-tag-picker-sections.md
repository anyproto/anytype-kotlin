# DROID-4571 Tag Picker Sections Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The tag/status value picker shows a "Selected" section on top and an "All values" section below, each with drag-to-reorder.

**Architecture:** The ViewModel builds one flat list with two header items interleaved. A drag inside "Selected" writes the permuted id list through the existing optimistic `UpdateDetail` path. A drag inside "All values" merges the displayed order back into the full option order and calls `SetRelationOptionOrder`. The Compose layer renders the headers as non-draggable rows and rejects a move across a section boundary.

**Tech Stack:** Kotlin, Jetpack Compose, `sh.calvin.reorderable`, Mockito-Kotlin, Turbine, JUnit4.

**Spec:** `docs/superpowers/specs/2026-08-12-droid-4571-tag-picker-sections-design.md`

## Global Constraints

- Java 17 is required for every Gradle invocation.
- New user-visible strings go to `localization/src/main/res/values/strings.xml` only.
- Commit messages start with `DROID-4571` and use ASD-STE100 Simplified Technical English (active voice, no contractions, one action per sentence).
- Every commit ends with the trailer `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>`.
- The read-only picker layout must not change: a flat list of the selected values, no headers.
- The display sort of selected values must never write through `SetRelationOptionOrder` (that RPC changes the global option order for all objects).
- Match existing code style: plain `object` subclasses in sealed classes (not `data object`), Timber logging, no new comments that merely restate code.

## File Map

| File | Role |
| --- | --- |
| `presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt` | ViewModel + all state models (bottom of file). Both tasks modify it. |
| `core-ui/src/main/java/com/anytypeio/anytype/core_ui/relations/TagOrStatusCompose.kt` | The picker screen. Task 1 modifies it. |
| `localization/src/main/res/values/strings.xml` | Task 1 adds two strings. |
| `presentation/src/test/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModelTest.kt` | All new tests. |

Test command used throughout:

```bash
./gradlew :presentation:testDebugUnitTest --tests "com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModelTest"
```

Compile check for the UI module:

```bash
./gradlew :core-ui:compileDebugKotlin
```

---

### Task 1: Section composition — model, ViewModel, Compose rendering

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt`
- Modify: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/relations/TagOrStatusCompose.kt`
- Modify: `localization/src/main/res/values/strings.xml`
- Test: `presentation/src/test/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModelTest.kt`

**Interfaces:**
- Consumes: existing `RelationsListItem`, `TagStatusViewState.Content`, `initViewState`, `filterOptions`.
- Produces (Task 2 relies on these exact shapes):
  - `RelationsListItem.Section` sealed class with `object Selected : Section()` and `object AllValues : Section()`.
  - `TagStatusViewState.Content.items: List<RelationsListItem>` (widened from `List<RelationsListItem.Item>`).
  - Editable list layout: `[Section.Selected, selected items in value order, Section.AllValues, unselected items in orderId order]`, a header omitted when its group is empty.
  - Test helpers `allItems()` (full list) and `items()` (items only, headers filtered).

- [ ] **Step 1: Write the failing tests**

In `TagOrStatusValueViewModelTest.kt`, replace the `items()` helper (line 162-163) with:

```kotlin
    private fun TagOrStatusValueViewModel.allItems(): List<RelationsListItem> =
        (viewState.value as TagStatusViewState.Content).items

    private fun TagOrStatusValueViewModel.items(): List<RelationsListItem.Item> =
        allItems().filterIsInstance<RelationsListItem.Item>()

    private fun List<RelationsListItem>.byId(id: Id) =
        filterIsInstance<RelationsListItem.Item>().first { it.optionId == id }

    /** Maps each row to its optionId, or to the Section object itself, for order assertions. */
    private fun TagOrStatusValueViewModel.rows(): List<Any> =
        allItems().map { if (it is RelationsListItem.Item) it.optionId else it }
```

Note: the old `byId` extension on `List<RelationsListItem.Item>` still compiles; replace it with the version above (receiver `List<RelationsListItem>`) so both helper outputs work with it. Keep every existing test unchanged otherwise — `items()` filters the headers out, so the old assertions stay valid.

Change `buildViewModel` (line 137) to accept a lock flag:

```kotlin
    private fun buildViewModel(
        initialIds: List<Id>,
        isLocked: Boolean = false
    ): TagOrStatusValueViewModel {
```

and pass `isLocked = isLocked` in `ViewModelParams` (line 147).

Add the new tests at the end of the class:

```kotlin
    @Test
    fun `editable tag list shows the selected section then the all values section`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2")))

        val vm = buildViewModel(initialIds = listOf(tagC, tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(
                RelationsListItem.Section.Selected,
                tagC,
                tagB,
                RelationsListItem.Section.AllValues,
                tagA
            ),
            vm.rows()
        )
        assertEquals(1, (vm.allItems().byId(tagC) as RelationsListItem.Item.Tag).number)
        assertEquals(2, (vm.allItems().byId(tagB) as RelationsListItem.Item.Tag).number)
    }

    @Test
    fun `no selection shows only the all values header`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.AllValues, tagA, tagB),
            vm.rows()
        )
    }

    @Test
    fun `full selection shows only the selected header`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.Selected, tagA, tagB),
            vm.rows()
        )
    }

    @Test
    fun `status list gets the same sections`() = runTest {
        storeOfRelations.merge(listOf(statusRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagB))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(
                RelationsListItem.Section.Selected,
                tagB,
                RelationsListItem.Section.AllValues,
                tagA
            ),
            vm.rows()
        )
    }

    @Test
    fun `read-only list has no section headers`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagB), isLocked = true)
        advanceUntilIdle()

        assertEquals(listOf<Any>(tagB), vm.rows())
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run the test command from the File Map section.
Expected: the five new tests FAIL to compile or FAIL on assertion (`Section` does not exist yet). If compilation of the test file fails entirely, that counts as the expected failure.

- [ ] **Step 3: Implement the model change and the list composition**

In `TagOrStatusValueViewModel.kt`:

3a. Add `Section` to `RelationsListItem` (line 635) and keep everything else:

```kotlin
sealed class RelationsListItem {

    sealed class Section : RelationsListItem() {
        object Selected : Section()
        object AllValues : Section()
    }

    sealed class Item : RelationsListItem() {
        // ... unchanged ...
```

3b. Widen `TagStatusViewState.Content.items` (line 614):

```kotlin
    data class Content(
        val title: String,
        val items: List<RelationsListItem>,
        val createItem: RelationsListItem.CreateItem.Tag? = null,
        val isRelationEditable: Boolean,
        val showItemMenu: RelationsListItem.Item? = null
    ) : TagStatusViewState()
```

3c. Rewrite `initViewState` (line 327-380). Replace the `result` accumulation with:

```kotlin
    private fun initViewState(
        relation: ObjectWrapper.Relation,
        ids: List<Id>,
        options: List<ObjectWrapper.Option>,
        query: String
    ) {
        val isTagRelation = relation.format == Relation.Format.TAG

        val mapped: List<RelationsListItem.Item> = when (relation.format) {
            Relation.Format.STATUS -> mapStatusOptions(ids = ids, options = options)
            Relation.Format.TAG -> mapTagOptions(ids = ids, options = options)
            else -> {
                Timber.w("Relation format should be Tag or Status but was: ${relation.format}")
                emptyList()
            }
        }

        val items = buildSectionedList(mapped = mapped, ids = ids)

        // CreateItem is only shown for TAG relations when there's a search query
        val createItem = if (isTagRelation && query.isNotBlank() && isEditableRelation) {
            RelationsListItem.CreateItem.Tag(query)
        } else {
            null
        }

        viewState.value = if (items.isEmpty() && createItem == null) {
            TagStatusViewState.Empty(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
            )
        } else {
            TagStatusViewState.Content(
                isRelationEditable = isEditableRelation,
                title = relation.name.orEmpty(),
                items = items,
                createItem = createItem
            )
        }.also {
            Timber.d("TagStatusViewModel initViewState, viewState: $it")
        }
    }

    /**
     * Editable relations get two sections: the selected options in value order,
     * then the unselected options in the global option order. The read-only list
     * stays flat. A header is omitted when its group is empty. The display sort
     * is deliberately separate from the persisted option order (DROID-3916).
     */
    private fun buildSectionedList(
        mapped: List<RelationsListItem.Item>,
        ids: List<Id>
    ): List<RelationsListItem> {
        if (!isEditableRelation) return mapped
        val selected = mapped.filter { it.isSelected }.sortedBy { ids.indexOf(it.optionId) }
        val unselected = mapped.filter { !it.isSelected }
        return buildList {
            if (selected.isNotEmpty()) {
                add(RelationsListItem.Section.Selected)
                addAll(selected)
            }
            if (unselected.isNotEmpty()) {
                add(RelationsListItem.Section.AllValues)
                addAll(unselected)
            }
        }
    }
```

3d. The `OnMove` handler (line 253-280) no longer compiles because `items` now holds headers without `optionId`. Apply a minimal compile fix only — Task 2 replaces the whole handler:

```kotlin
                    val reorderedIds = currentState.items
                        .filterIsInstance<RelationsListItem.Item>()
                        .toMutableList()
                        .apply { add(action.to, removeAt(action.from)) }
                        .map { it.optionId }
```

Note: this intermediate behavior is wrong for sectioned lists (the indices include headers). That is acceptable inside this task only because Task 2 rewrites the handler before the branch is finished; the tests for `OnMove` arrive in Task 2.

3e. Add the two strings to `localization/src/main/res/values/strings.xml`, next to other relation strings (search for `name="options_empty_title"` and add below that block):

```xml
    <string name="tag_status_section_selected">Selected</string>
    <string name="tag_status_section_all_values">All values</string>
```

- [ ] **Step 4: Adapt the Compose screen**

In `TagOrStatusCompose.kt`:

4a. Add imports:

```kotlin
import com.anytypeio.anytype.core_ui.foundation.Section
```

4b. Add a private key helper at file scope (below `getTitle`, line 417):

```kotlin
private fun RelationsListItem.uiKey(): String = when (this) {
    RelationsListItem.Section.Selected -> "section_selected"
    RelationsListItem.Section.AllValues -> "section_all_values"
    is RelationsListItem.Item -> optionId
    is RelationsListItem.CreateItem -> "create_item"
}
```

4c. In `RelationsViewContent` (editable branch), widen the local list (line 195):

```kotlin
        val items = remember { mutableStateListOf<RelationsListItem>() }
        items.swapList(state.items)
```

4d. Change `onDragStoppedHandler` (line 201-214) to key-based lookups:

```kotlin
        val onDragStoppedHandler = {
            val originalItemId = draggedItemId.value
            if (originalItemId != null) {
                // Find original index from state.items (unchanged during drag)
                val originalIndex = state.items.indexOfFirst { it.uiKey() == originalItemId }
                // Find new index in reordered items list
                val newIndex = items.indexOfFirst { it.uiKey() == originalItemId }

                if (originalIndex != -1 && newIndex != -1 && originalIndex != newIndex) {
                    action(TagStatusAction.OnMove(from = originalIndex, to = newIndex))
                }
            }
            draggedItemId.value = null
        }
```

4e. In `rememberReorderableLazyListState` (line 216-243), reject a move across a section boundary. Replace the index lookups with:

```kotlin
            // Capture original dragged item on first move
            if (draggedItemId.value == null) {
                draggedItemId.value = fromId
            }

            // Find current indices by key
            val f = items.indexOfFirst { it.uiKey() == fromId }
            val t = items.indexOfFirst { it.uiKey() == toId }
            val fromItem = items.getOrNull(f) as? RelationsListItem.Item
            val toItem = items.getOrNull(t) as? RelationsListItem.Item

            // A drag never crosses a section header: the target must be an item
            // of the same section (same selection state).
            if (fromItem == null || toItem == null || fromItem.isSelected != toItem.isSelected) {
                return@rememberReorderableLazyListState
            }

            if (f != t) {
                val newList = items.toMutableList().apply {
                    add(t, removeAt(f))
                }
                items.swapList(newList)

                ViewCompat.performHapticFeedback(
                    view,
                    HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                )
            }
```

4f. In the editable `LazyColumn` (line 245-296), key by `uiKey()` and render headers outside `ReorderableItem`:

```kotlin
            items(
                count = items.size,
                key = { index -> items[index].uiKey() }
            ) { index ->
                when (val item = items[index]) {
                    RelationsListItem.Section.Selected -> Section(
                        title = stringResource(id = R.string.tag_status_section_selected)
                    )
                    RelationsListItem.Section.AllValues -> Section(
                        title = stringResource(id = R.string.tag_status_section_all_values)
                    )
                    is RelationsListItem.Item -> ReorderableItem(
                        reorderableLazyListState,
                        key = item.uiKey()
                    ) { isDragging ->
                        val currentItem = LocalView.current
                        if (isDragging) {
                            currentItem.isHapticFeedbackEnabled = true
                            currentItem.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                        val dragHandleModifier = Modifier.longPressDraggableHandle(
                            onDragStarted = {
                                ViewCompat.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstantsCompat.GESTURE_START
                                )
                            },
                            onDragStopped = {
                                ViewCompat.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstantsCompat.GESTURE_END
                                )
                                onDragStoppedHandler()
                            }
                        )
                        when (item) {
                            is RelationsListItem.Item.Tag -> TagItem(
                                state = item,
                                action = action,
                                isEditable = state.isRelationEditable,
                                showDivider = true,
                                isDragging = isDragging,
                                dragHandleModifier = dragHandleModifier
                            )

                            is RelationsListItem.Item.Status -> StatusItem(
                                state = item,
                                action = action,
                                isEditable = state.isRelationEditable,
                                showDivider = true,
                                isDragging = isDragging,
                                dragHandleModifier = dragHandleModifier
                            )
                        }
                    }
                    is RelationsListItem.CreateItem -> Unit
                }
            }
```

Keep the `state.createItem` block after it unchanged.

4g. In the read-only branch (line 314-334), the `when (item)` now sees `RelationsListItem`. Add a fall-through branch so the block stays exhaustive-safe:

```kotlin
                    when (item) {
                        is RelationsListItem.Item.Tag -> TagItem(...unchanged...)
                        is RelationsListItem.Item.Status -> StatusItem(...unchanged...)
                        else -> Unit
                    }
```

4h. Update `TagOrStatusValueScreenPreview` (line 454) so the preview shows the sections: insert `RelationsListItem.Section.Selected` before the selected tag and `RelationsListItem.Section.AllValues` before the unselected tags in the `items` list. The preview's `items` variable type becomes `List<RelationsListItem>`.

- [ ] **Step 5: Run the tests and the UI compile check**

Run both commands from the File Map section.
Expected: all tests PASS (the five new ones and the ten existing ones), `:core-ui:compileDebugKotlin` succeeds.

- [ ] **Step 6: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt \
        core-ui/src/main/java/com/anytypeio/anytype/core_ui/relations/TagOrStatusCompose.kt \
        localization/src/main/res/values/strings.xml \
        presentation/src/test/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModelTest.kt
git commit -m "DROID-4571 Properties | Fix | Show Selected and All values sections in the tag picker

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: Sectioned drag semantics in OnMove

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt`
- Test: `presentation/src/test/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModelTest.kt`

**Interfaces:**
- Consumes from Task 1: `RelationsListItem.Section`, `Content.items: List<RelationsListItem>`, sectioned list layout, `proceedWithOptimisticUpdate(newIds, persistedValue, analyticsEvent, dismissOnSuccess)` (unchanged, line 443), `activateOptionEventLock()` (line 555), `selectedIds` (line 117).
- Produces: private functions `onMove(from, to)`, `moveSelectedValue(moved)`, `moveOptionOrder(moved)`, `mergeReorderedSubset(full, displayedNewOrder)`. `TagStatusAction.OnMove(from, to)` keeps flat-list indices that include the headers.

- [ ] **Step 1: Write the failing tests**

Add to the test class fields (near line 84):

```kotlin
    private val tagD = "opt-d"

    // Named so that the query "7" matches both.
    private val tag7a = "tag7a"
    private val tag7b = "tag7b"
```

Add imports:

```kotlin
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.relations.SetRelationOptionOrder
import org.mockito.kotlin.verifyNoInteractions
```

(`SetRelationOptionOrder` is already imported; skip it if the import exists.)

Add a stub helper next to `stubPersistSuccess` (line 125):

```kotlin
    private fun stubOptionOrderSuccess() {
        setRelationOptionOrder.stub {
            onBlocking { async(any()) } doReturn Resultat.success(Unit)
        }
    }

    private fun orderParams(orderedIds: List<Id>) = SetRelationOptionOrder.Params(
        spaceId = space,
        relationKey = RelationKey(relationKey),
        orderedIds = orderedIds
    )
```

Add the tests:

```kotlin
    @Test
    fun `drag inside the selected section reorders the object value`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2")))
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB, tagC))
        advanceUntilIdle()

        // Flat list: [Section.Selected, A, B, C]. Move A after C.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 3))
        advanceUntilIdle()

        assertEquals(
            listOf<Any>(RelationsListItem.Section.Selected, tagB, tagC, tagA),
            vm.rows()
        )
        assertEquals(1, (vm.allItems().byId(tagB) as RelationsListItem.Item.Tag).number)
        assertEquals(3, (vm.allItems().byId(tagA) as RelationsListItem.Item.Tag).number)
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tagB, tagC, tagA))) }
        verifyNoInteractions(setRelationOptionOrder)
    }

    @Test
    fun `drag inside the selected section keeps the selections hidden by the query`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag2, "0"), option(tag7a, "1"), option(tag7b, "2"))
        )
        stubPersistSuccess()

        val vm = buildViewModel(initialIds = listOf(tag2, tag7a, tag7b))
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        // Flat list under the query: [Section.Selected, tag7a, tag7b]. Swap them.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
        advanceUntilIdle()

        // The hidden selection tag2 keeps its first position.
        verifyBlocking(setObjectDetails) { invoke(params(listOf(tag2, tag7b, tag7a))) }
    }

    @Test
    fun `a failed value write after a selected drag reverts the order and toasts`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))
        stubPersistFailure()

        val vm = buildViewModel(initialIds = listOf(tagA, tagB))
        advanceUntilIdle()
        val snapshot = vm.viewState.value

        vm.toasts.test {
            vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
            advanceUntilIdle()
            val messages = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<String>>()
                .map { it.value }
            assertTrue(messages.contains("Error while updating value"))
        }

        assertEquals(snapshot, vm.viewState.value)
    }

    @Test
    fun `drag inside all values saves the merged global option order`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tagA, "0"), option(tagB, "1"), option(tagC, "2"), option(tagD, "3"))
        )
        stubOptionOrderSuccess()

        val vm = buildViewModel(initialIds = listOf(tagB))
        advanceUntilIdle()

        // Flat list: [Section.Selected, B, Section.AllValues, A, C, D]. Move D before A.
        vm.onAction(TagStatusAction.OnMove(from = 5, to = 3))
        advanceUntilIdle()

        // Full order was [A, B, C, D]; B is hidden from the section and keeps its slot.
        verifyBlocking(setRelationOptionOrder) {
            async(orderParams(listOf(tagD, tagB, tagA, tagC)))
        }
        verifyNoInteractions(setObjectDetails)
    }

    @Test
    fun `drag inside all values under a query keeps the hidden options in place`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(
            listOf(option(tag7a, "0"), option(tag2, "1"), option(tag7b, "2"))
        )
        stubOptionOrderSuccess()

        val vm = buildViewModel(initialIds = emptyList())
        advanceUntilIdle()

        vm.onQueryChanged("7")
        advanceUntilIdle()

        // Flat list under the query: [Section.AllValues, tag7a, tag7b]. Swap them.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 2))
        advanceUntilIdle()

        // Full order was [tag7a, tag2, tag7b]; hidden tag2 keeps its slot.
        verifyBlocking(setRelationOptionOrder) {
            async(orderParams(listOf(tag7b, tag2, tag7a)))
        }
    }

    @Test
    fun `drag across sections is ignored`() = runTest {
        storeOfRelations.merge(listOf(tagRelation()))
        storeOfRelationOptions.merge(listOf(option(tagA, "0"), option(tagB, "1")))

        val vm = buildViewModel(initialIds = listOf(tagA))
        advanceUntilIdle()

        // Flat list: [Section.Selected, A, Section.AllValues, B].
        // from = selected item, to = unselected item.
        vm.onAction(TagStatusAction.OnMove(from = 1, to = 3))
        // from = item, to = section header.
        vm.onAction(TagStatusAction.OnMove(from = 3, to = 2))
        advanceUntilIdle()

        verifyNoInteractions(setObjectDetails)
        verifyNoInteractions(setRelationOptionOrder)
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run the test command from the File Map section.
Expected: the six new tests FAIL (the current handler reorders the header-free list by header-inclusive indices and always calls `setRelationOptionOrder`). Every pre-existing test must still PASS.

- [ ] **Step 3: Rewrite the OnMove handler**

In `TagOrStatusValueViewModel.kt`:

3a. Replace the whole `is TagStatusAction.OnMove -> { ... }` branch (line 253-280) with:

```kotlin
            is TagStatusAction.OnMove -> {
                Timber.d("OnMove from ${action.from} to ${action.to}")
                onMove(from = action.from, to = action.to)
            }
```

3b. Add the three private functions (below `onActionClick`, line 325):

```kotlin
    /**
     * [from] and [to] are indices into the flat, header-inclusive items list.
     * The section of the dragged item decides the write path: a selected item
     * permutes this object's value list, an unselected item permutes the
     * global option order. A move whose target is a header or an item of the
     * other section is ignored — the UI rejects those moves as well, this is
     * the second gate.
     */
    private fun onMove(from: Int, to: Int) {
        val currentState = viewState.value as? TagStatusViewState.Content ?: return
        val items = currentState.items
        val dragged = items.getOrNull(from) as? RelationsListItem.Item ?: return
        val target = items.getOrNull(to) as? RelationsListItem.Item ?: return
        if (dragged.isSelected != target.isSelected) return
        val moved = items.toMutableList().apply { add(to, removeAt(from)) }
        if (dragged.isSelected) {
            moveSelectedValue(moved)
        } else {
            moveOptionOrder(moved)
        }
    }

    private fun moveSelectedValue(moved: List<RelationsListItem>) {
        val displayedOrder = moved
            .filterIsInstance<RelationsListItem.Item>()
            .filter { it.isSelected }
            .map { it.optionId }
        val newIds = mergeReorderedSubset(
            full = selectedIds,
            displayedNewOrder = displayedOrder
        )
        proceedWithOptimisticUpdate(
            newIds = newIds,
            persistedValue = newIds,
            analyticsEvent = EventsDictionary.relationChangeValue,
            dismissOnSuccess = false
        )
    }

    private fun moveOptionOrder(moved: List<RelationsListItem>) {
        viewModelScope.launch {
            val fullOrder = storeOfRelationOptions
                .getByRelationKey(viewModelParams.relationKey)
                .sortedBy { it.orderId }
                .map { it.id }
            val displayedOrder = moved
                .filterIsInstance<RelationsListItem.Item>()
                .filter { !it.isSelected }
                .map { it.optionId }
            val orderedIds = mergeReorderedSubset(
                full = fullOrder,
                displayedNewOrder = displayedOrder
            )
            // Activate lock before sending to middleware to prevent race conditions
            activateOptionEventLock()
            setRelationOptionOrder.async(
                SetRelationOptionOrder.Params(
                    spaceId = viewModelParams.space,
                    relationKey = RelationKey(viewModelParams.relationKey),
                    orderedIds = orderedIds
                )
            ).fold(
                onSuccess = {
                    Timber.d("Option order saved successfully")
                },
                onFailure = { e ->
                    Timber.e(e, "Failed to save option order")
                    sendToast("Failed to save order")
                }
            )
        }
    }

    /**
     * Applies the new relative order of a displayed subset to the full list.
     * An id that the list displays takes the next displayed slot in the new
     * order. An id that the query or the section hides keeps its position.
     */
    private fun mergeReorderedSubset(
        full: List<Id>,
        displayedNewOrder: List<Id>
    ): List<Id> {
        val displayed = displayedNewOrder.toSet()
        val iterator = displayedNewOrder.iterator()
        return full.map { id -> if (displayed.contains(id)) iterator.next() else id }
    }
```

Note: `moveSelectedValue` reuses `proceedWithOptimisticUpdate`, so the optimistic rebuild, the event lock, the failure revert, and the analytics event all come for free and stay consistent with tap-selection.

- [ ] **Step 4: Run the tests to verify they pass**

Run the test command from the File Map section.
Expected: all tests PASS (six new, five from Task 1, ten pre-existing).

- [ ] **Step 5: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt \
        presentation/src/test/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModelTest.kt
git commit -m "DROID-4571 Properties | Fix | Route a drag to the value order or the option order by section

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: Full verification

**Files:**
- No new source changes are expected. Fix regressions here if verification finds any.

**Interfaces:**
- Consumes: the finished code of Tasks 1-2.
- Produces: a green build as the merge gate.

- [ ] **Step 1: Run the presentation unit tests for the whole module**

```bash
./gradlew :presentation:testDebugUnitTest
```

Expected: PASS. Watch for failures in neighboring relation tests (for example `ObjectValueViewModelTest`) — they share providers with this ViewModel.

- [ ] **Step 2: Compile the app and the UI modules**

```bash
./gradlew :core-ui:compileDebugKotlin :app:compileDebugSources
```

Expected: success. This catches usages of `TagStatusViewState.Content.items` outside the two edited files.

- [ ] **Step 3: Run lint on the touched modules**

```bash
./gradlew :presentation:lintDebug :core-ui:lintDebug
```

Expected: no new errors. Pre-existing warnings stay.

- [ ] **Step 4: Commit fixes, if any**

If steps 1-3 required fixes, commit them:

```bash
git add -A
git commit -m "DROID-4571 Properties | Fix | Repair issues found by full verification

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

If nothing changed, skip the commit.
