# Kanban Column Wrap-Height + Per-Column "+ New" — Design

**Date:** 2026-06-30
**Ticket:** DROID-4529 (follow-up)
**Branch:** `droid-4529-kanban-menu`

## Goals

1. **Wrap height:** Kanban columns size to their content instead of always filling the
   board height. A short column (few cards) is short; a tall column grows up to the board
   height and then scrolls internally (keeping per-column pagination + drag auto-scroll).
2. **Per-column "+ New":** When the user can create objects (`isCreateObjectAllowed`, i.e.
   permission != Viewer), each column shows a "＋ New" item at the bottom. Tapping it
   creates an object with the column's group value (e.g. Status = "To Do") plus the view's
   filter prefill, then shows the existing "name your object" sheet — the same post-create
   UX as the main "+ New" button.

## Decisions (locked)

- Tall column → **cap at board height, scroll internally** (smallest change; keeps the
  existing `LazyColumn` pagination + drag auto-scroll). The "+ New" item is the last item in
  the column's list.
- "+ New" tap → **quick-create** with the view's default type + group value + filter prefill,
  then the name sheet. No per-column type picker.
- Empty / "No value" column → "+ New" creates an object with **no** group value.

## Part A — Layout (`core-ui`)

`BoardScreen.kt` (column item, ~line 197): change the per-column modifier
`.fillMaxHeight()` → `.wrapContentHeight(Alignment.Top)`. The `LazyRow` keeps its fixed
height, so a column is capped at the board height.

`BoardColumnContent.kt`:
- Outer `Column`: no forced fill-height (wraps content).
- Cards `LazyColumn`: `Modifier.fillMaxHeight()` → `Modifier.wrapContentHeight()`. It still
  scrolls + paginates when content exceeds the capped height.
- Append a **`BoardAddCardButton`** as the **last `item {}`** of the `LazyColumn`, shown only
  when `canCreateObject`.
- Empty-column branch: drop the centered `fillMaxHeight()` Box; show the "+ New" button
  (when allowed) so the empty column is short.
- New `BoardAddCardButton` composable: a card-styled row (`background_primary`, rounded) with
  a `+` glyph and "New" label, `noRippleThrottledClickable { onCreateInColumn(column.id) }`.
- New params on `BoardColumnContent` (threaded from `BoardScreen`): `canCreateObject: Boolean`,
  `onCreateInColumn: (columnId: String) -> Unit`.

`BoardViewWidget.kt`: add `var canCreateObject: Boolean by mutableStateOf(true)` (reactive)
and `var onCreateInColumn: (String) -> Unit = {}`; pass both into `BoardScreen`.

## Part B — Create wiring (`app` + `presentation`)

`ObjectSetFragment.kt` board `apply {}` block (~line 354): wire
`onCreateInColumn = { columnId -> vm.onBoardCreateObjectInColumn(columnId) }`, and set
`binding.boardView.canCreateObject = state.isCreateObjectAllowed` wherever the board state is
fed (where `setBoard(...)` is called for the board Default state).

`ObjectSetViewModel.kt`:
- New `fun onBoardCreateObjectInColumn(columnId: String)`:
  1. Guard `isOwnerOrEditor` (defensive; UI already gates) → else `toast(NOT_ALLOWED)`.
  2. Resolve `state.dataViewState()`, the active `viewer`, `viewer.groupRelationKey`,
     `boardGroups.value`. Bail (log) if groups not loaded or no group relation.
  3. Compute the column's group value by **reusing `computeBoardCardMove`**:
     ```kotlin
     val move = computeBoardCardMove(
         format = storeOfRelations.getByKey(groupRelationKey)?.format,
         currentValue = emptyList(),
         sourceColumnId = BOARD_EMPTY_GROUP_ID,
         sourceGroup = null,
         targetColumnId = columnId,
         targetGroup = groups.firstOrNull { it.id == columnId }?.value,
         groupsLoaded = true
     )
     val groupValue = (move as? BoardCardMove.Write)?.value
        ?.takeUnless { it == null || (it is List<*> && it.isEmpty()) }
     ```
  4. Build `extraPrefilled = groupValue?.let { mapOf(groupRelationKey to it) }.orEmpty()`.
  5. Route through the existing create path with the extra prefill merged in (see below).
- Thread an optional `extraPrefilled: Map<Key, Any?> = emptyMap()` into the existing create
  helpers (`proceedWithCreatingSetObject`, `proceedWithCreatingObjectTypeSetObject`,
  `proceedWithAddingObjectToCollection`) and merge it into the `prefilled` map they pass to
  `CreateDataViewObject.Params`. `onBoardCreateObjectInColumn` resolves the state's layout
  (Set / TypeSet / Collection) and calls the matching helper with `extraPrefilled`.
- Post-create UX (`proceedWithNewDataViewObject` → "name your object" sheet) is reused as-is.

The new object then matches the column's group and appears there via the existing board
subscriptions.

## Group-value mapping (via `computeBoardCardMove`, already unit-tested)

| Column group | `prefilled[groupRelationKey]` |
|---|---|
| Status(optionId) | `listOf(optionId)` |
| Tag(ids) | `ids` |
| Checkbox(checked) | `checked` (bool) |
| Empty / "No value" | (omitted — no group value) |

## Testing

- **Group-value/prefill helper** (presentation unit test): status column → `extraPrefilled`
  has `groupRelationKey = listOf(optionId)`; tag → ids; checkbox → bool; empty column → empty
  map. (`computeBoardCardMove` itself is already covered.)
- **VM test** (mirror `ObjectSetBoardSubscriptionTest`): seed a board, call
  `onBoardCreateObjectInColumn(columnId)`, verify `createDataViewObject` is invoked with
  `prefilled` containing the group value; and that a Viewer permission blocks it.
- **Layout/UI:** `@Preview` + manual QA (short vs tall column; [+] hidden for Viewer).

## Files

- `core-ui/.../board/BoardScreen.kt` — column modifier; thread params.
- `core-ui/.../board/BoardColumnContent.kt` — wrap height; `BoardAddCardButton`; params.
- `core-ui/.../board/BoardViewWidget.kt` — `canCreateObject` + `onCreateInColumn`.
- `app/.../ui/sets/ObjectSetFragment.kt` — wire callback + `canCreateObject`.
- `presentation/.../sets/ObjectSetViewModel.kt` — `onBoardCreateObjectInColumn` + `extraPrefilled` threading.
- `localization/.../strings.xml` — reuse an existing "New"/create string if present, else add one.
- Tests: presentation unit + VM tests.

## Out of scope
- Inline-editable new card (desktop creates an inline card); mobile reuses the name sheet.
- Reordering the new card to a specific position (it lands per the column's default order).
- Date-group boards (unsupported).
