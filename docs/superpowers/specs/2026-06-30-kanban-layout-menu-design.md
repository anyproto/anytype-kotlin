# Kanban Layout Menu — Design

**Date:** 2026-06-30
**Ticket:** DROID-4529 (Kanban menu)
**Branch:** `droid-4529-kanban-menu`

## Problem

The Kanban (Board) view itself is fully implemented and merged (DROID-4529, #3282):
rendering (`Viewer.Board` / `BoardScreen`), grouping by `viewer.groupRelationKey`,
drag/drop, and paged column records, all gated behind the `kanbanEnabled` experimental
flag. The layout picker even shows the Kanban card when the flag is on.

The gap: when **BOARD** is selected in the "Edit View → Layout" bottom sheet, the menu
shows **no Kanban-specific controls**. Today it only reveals Gallery options (Card size /
Cover / Fit image, hidden via `.alpha(0f)` unless Gallery) plus the always-shown "Show
icon" toggle. There is no way to choose the grouping relation or toggle column colors.

Desktop, by contrast, shows for Kanban: Cover, Fit media, Group by, Color columns, Show
icon, Page limit.

## Scope (decided)

Mobile Kanban menu rows: **Group by + Color columns + Show icon**.

- All three map to fields the Android model + middleware already support
  (`groupRelationKey`, `groupBackgroundColors`, `hideIcon`). No model/middleware changes.
- **Excluded:** Cover / Fit media (mobile Kanban `Card` renders name + icon + relations,
  no cover image), and Page limit (`pageLimit` is in the proto but not mapped into the
  Android `DVViewer` model; the board uses a default limit). These are out of scope.
- **Group by picker = pick existing only.** Lists existing group-able properties
  (Select/Status, Multi-select/Tag, Checkbox); no "Add Property" / create-relation flow
  (that is a possible fast follow).

## UI structure

When `layoutType == BOARD`, the menu shows:

```
Layout
  Grid   Gallery   List
 [Kanban] Calendar  Graph     (Calendar/Graph still hidden on mobile)
─────────────────────────
Group by              Status >   opens nested picker
Color columns            [on]    toggle → groupBackgroundColors
Show icon                [on]    existing "Icon" toggle → hideIcon
```

**Row-visibility change.** Today the Gallery rows are always composed and hidden with
`.alpha(0f)` when not Gallery, reserving dead vertical space for Grid/List/Board. Switch
to **conditional composition per layout type** to avoid a block of empty space above the
Kanban rows:

- Gallery → Card size, Cover, Fit image
- Board → Group by, Color columns
- "Show icon" → shown for all (unchanged)

Gallery's visible result is identical; only the previously-reserved blank space for
non-Gallery layouts goes away.

## State & actions model

`presentation/.../sets/ViewerLayoutWidgetUi.kt`

New fields on `ViewerLayoutWidgetUi`:

```kotlin
val groupBackgroundColors: State.Toggle.ColorColumns,   // → groupBackgroundColors
val groupByItems: List<State.GroupBy>,                  // group-able relations
val showGroupByMenu: Boolean                            // nested picker visibility
```

New state types:

```kotlin
sealed class Toggle {
    // ...
    data class ColorColumns(override val toggled: Boolean) : Toggle()
}

data class GroupBy(
    val relationKey: RelationKey,
    val name: String,
    val format: RelationFormat,   // STATUS / TAG / CHECKBOX
    val isChecked: Boolean        // key == viewer.groupRelationKey
) : State()
```

New actions:

```kotlin
sealed class Action {
    // ...
    data class ColorColumns(val toggled: Boolean) : Action()    // toggle
    data object GroupByMenu : Action()                          // open/close picker
    data class GroupByUpdate(val item: State.GroupBy) : Action()// select relation
}
```

Populating state — in `updateState(viewer, storeOfRelations, relationLinks)`, add a
`getGroupByItems(...)` helper alongside the existing `getImagePreviewItems(...)`. It:

- filters `relationLinks` to formats `STATUS / TAG / CHECKBOX` (what `BoardViewMapper`
  actually renders as columns — Date/Object grouping aren't supported on mobile),
- drops hidden/archived/invalid/system relations (same guards as `getValidFileRelations`),
- marks `isChecked = (key == viewer.groupRelationKey)`.

Also set `groupBackgroundColors = ColorColumns(viewer.groupBackgroundColors)`.

`init()` / `empty()` get defaults: empty list, `false`, `showGroupByMenu = false`.

"Show icon" reuses the existing `withIcon` field / `Icon` action verbatim.

## Group-by picker widget

New `core-ui/.../widgets/dv/ViewerLayoutGroupByWidget.kt`, a near-copy of
`ViewerLayoutCoverWidget.kt` (nested `ModalBottomSheet` gated by `uiState.showGroupByMenu`):

```kotlin
@Composable
fun ViewerLayoutGroupByWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
) {
    if (uiState.showGroupByMenu) {
        ModalBottomSheet(/* ... */) {
            // Title: "Group by"
            LazyColumn {
                items(uiState.groupByItems, key = { it.relationKey.key }) { item ->
                    GroupByItem(
                        text = item.name,
                        iconRes = item.format.toRelationIconRes(), // status/tag/checkbox glyph
                        checked = item.isChecked,
                        onClick = { action(Action.GroupByUpdate(item)) }
                    )
                }
            }
        }
    }
}
```

Each row: relation-format icon on the left, name, and `ic_option_checked_black` checkmark
on the right for the selected one — identical visual language to `CoverItem`. Tapping a
row fires `GroupByUpdate(item)` and the picker closes.

Wiring into the main widget: next to the existing `ViewerLayoutCoverWidget(uiState, action)`
call, add `ViewerLayoutGroupByWidget(uiState, action)`. Inside `ViewerLayoutContent`, the
Board branch renders the "Group by" row as a `ColumnItem` (current relation name as value,
or `None`) opening the picker via `action(Action.GroupByMenu)`, and the "Color columns" row
as a `LayoutSwitcherItem`.

Empty case: if `groupByItems` is empty, the "Group by" row still shows with value "None";
opening it shows an empty list (matches Cover behavior with no file relations).

New strings: `group_by` → "Group by", `color_columns` → "Color columns". "Show icon"
reuses existing `icon` / "Icon".

## ViewModel wiring

`ObjectSetViewModel.onViewerLayoutWidgetAction(...)` — three new branches, each mirroring a
proven handler:

```kotlin
// mirror of Action.Icon / FitImage — trivial persist
is Action.ColorColumns -> viewModelScope.launch {
    proceedWithUpdateViewer(viewerId = viewerLayoutWidgetState.value.viewer) {
        it.copy(groupBackgroundColors = action.toggled)
    }
}

// mirror of Action.CoverMenu — toggles nested-sheet visibility
Action.GroupByMenu -> {
    viewerLayoutWidgetState.value = viewerLayoutWidgetState.value
        .copy(showGroupByMenu = !viewerLayoutWidgetState.value.showGroupByMenu)
}

// mirror of Action.ImagePreviewUpdate — persist + close picker
is Action.GroupByUpdate -> {
    if (!action.item.isChecked) viewModelScope.launch {
        proceedWithUpdateViewer(viewerId = viewerLayoutWidgetState.value.viewer) {
            it.copy(groupRelationKey = action.item.relationKey.key)
        }
    }
    viewerLayoutWidgetState.value =
        viewerLayoutWidgetState.value.copy(showGroupByMenu = false)
}
```

Extend the existing `Dismiss` branch: if `showGroupByMenu` is open, the first dismiss just
closes the picker (as it already does for `showCoverMenu`); otherwise close the whole widget.

Persistence path is already correct: `proceedWithUpdateViewer` →
`viewerDelegate.onEvent(UpdateView(...))` → `UpdateDataViewViewer` → middleware. Because
`groupRelationKey` / `groupBackgroundColors` are already mapped, the change round-trips and
the board re-subscribes to the new group relation automatically (existing
`subscribeToBoardGroups()` keys off `viewer.groupRelationKey`). No new use cases.

Analytics: leave group-by / color-columns unlogged for v1 (existing code logs only
`CHANGE_VIEW_TYPE` for layout-type changes).

## Testing

- `updateState(...)` builds `groupByItems` correctly — filters to STATUS/TAG/CHECKBOX,
  drops hidden/archived/system relations, marks current `groupRelationKey` checked, reads
  `groupBackgroundColors`. (Unit-testable on the state mapper, no mocks.)
- `onViewerLayoutWidgetAction` for `ColorColumns`, `GroupByUpdate`, `GroupByMenu`, and the
  extended `Dismiss` — assert `proceedWithUpdateViewer` yields a viewer with the expected
  `groupBackgroundColors` / `groupRelationKey`, and that visibility flags flip. Follow
  existing `ObjectSet*` ViewModel test patterns (BOARD viewer fixtures exist in
  `ObjectSetReducerTest`).
- Compose `@Preview` for the Board state of `ViewerLayoutWidget` and for
  `ViewerLayoutGroupByWidget`.

## Complete file-change list

| File | Change |
|---|---|
| `presentation/.../sets/ViewerLayoutWidgetUi.kt` | Add `groupBackgroundColors`, `groupByItems`, `showGroupByMenu`; `ColorColumns` toggle, `GroupBy` state, 3 actions; `getGroupByItems(...)` in `updateState`; update `init()`/`empty()` |
| `core-ui/.../widgets/dv/ViewerLayoutWidget.kt` | Board branch (Group by + Color columns rows); alpha→conditional per-layout rows; call `ViewerLayoutGroupByWidget` |
| `core-ui/.../widgets/dv/ViewerLayoutGroupByWidget.kt` | **New** — nested picker (copy of `ViewerLayoutCoverWidget`) |
| `presentation/.../sets/ObjectSetViewModel.kt` | 3 new branches in `onViewerLayoutWidgetAction` + extend `Dismiss` |
| `localization/.../values/strings.xml` | `group_by`, `color_columns` |
| Test files | State-mapper + ViewModel-action tests |

No changes to `core-models`, `middleware`, `domain`, or the board renderer — all reused.
