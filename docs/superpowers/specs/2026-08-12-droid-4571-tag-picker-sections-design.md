# DROID-4571 — Tag picker: "Selected" and "All values" sections

Date: 2026-08-12
Ticket: https://linear.app/anytype/issue/DROID-4571/properties-fix-selected-tag-options-no-longer-move-to-the-top-of-the
Status: approved by the user (approach B, desktop parity)

## Problem

The tag/status value picker shows one flat list in the global option order.
The selected options do not move to the top. The user must scroll a long
list to find the active selections.

The selected-first sort existed before. Commit `9bac416af` (DROID-3916,
manual option sorting) removed it and replaced it with a sort by `orderId`.

The desktop client shows two sections: "Selected" on top, then "All values".
Each section supports drag-to-reorder. Android must match this layout.

## Current state

- Screen: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/relations/TagOrStatusCompose.kt`.
  One `LazyColumn` with the `sh.calvin.reorderable` library. Drag is active
  only when `state.isRelationEditable`.
- ViewModel: `presentation/src/main/java/com/anytypeio/anytype/presentation/relations/value/tagstatus/TagOrStatusValueViewModel.kt`.
  - `initViewState` builds a flat `List<RelationsListItem.Item>`.
  - `mapTagOptions` sets `isSelected` and the `number` badge. It does not sort.
  - `TagStatusAction.OnMove` sends the full displayed order to
    `SetRelationOptionOrder`. This edits the global option order of the
    relation, for all objects.
  - `proceedWithOptimisticUpdate` writes the selected id list through
    `UpdateDetail`, with revert on failure.
- No mechanism reorders the selected values on one object. The selection is
  a plain id list, so a permuted id list through `UpdateDetail` is enough.
  No new middleware is needed.
- Latent bug: a drag during an active search sends only the filtered ids to
  `SetRelationOptionOrder` as the full order.

## Decisions

1. Two sections in the editable picker: "Selected" on top, then "All values".
2. "All values" shows only the unselected options. A selected option appears
   only in the "Selected" section.
3. The numbered circle badges on the selected tags stay.
4. A drag inside "Selected" reorders the values of this object.
5. A drag inside "All values" reorders the global option order.
6. A drag cannot cross a section header.

## Design

### 1. State model (`TagOrStatusValueViewModel.kt`)

Add two header items to `RelationsListItem`:

- `RelationsListItem.Section.Selected`
- `RelationsListItem.Section.AllValues`

`TagStatusViewState.Content.items` stays one flat list, with the headers
interleaved. This keeps one `LazyColumn` and the existing reorder library.

### 2. List construction (`initViewState`)

For an editable relation, the list is:

1. `Section.Selected` header
2. The selected options, sorted by `number` (the position in the value list)
3. `Section.AllValues` header
4. The unselected options, sorted by `orderId`

Rules:

- The query filters both groups by name.
- A header is hidden when its group is empty. Consequence: with no
  selection, only the "All values" header shows. With a full selection,
  only the "Selected" header shows.
- The read-only path stays flat: only the selected values, no headers.
- The `CreateItem` row stays at the bottom.

### 3. Drag semantics

The headers are not draggable. The Compose layer rejects a move that
crosses a header, so an item stays inside its section.

`OnMove(from, to)` in the ViewModel resolves the section of the dragged
item from the flat list:

- **Selected section**: build the new selected id order. Write it with the
  existing optimistic `UpdateDetail` path, with revert on failure. The
  number badges recompute from the new positions.
- **All values section**: merge the new relative order of the displayed
  unselected options back into the full option order. Call
  `SetRelationOptionOrder` under the existing option-event lock. The merge
  keeps the positions of the options that are not displayed (selected
  options, and options hidden by the query). This also fixes the latent
  filtered-drag bug.

### 4. Status relations

The status picker uses the same screen and gets the same sections. The
"Selected" section holds at most one status. A drag there is a no-op.

### 5. Strings

Add "Selected" and "All values" to
`localization/src/main/res/values/strings.xml`.

### 6. Tests (`TagOrStatusValueViewModelTest.kt`)

New cases:

- Section composition: headers and group membership.
- Selected-first order with correct `number` badges.
- Value reorder through `UpdateDetail` after a drag in "Selected".
- Option reorder through `SetRelationOptionOrder` after a drag in
  "All values", with the merge of hidden options.
- Sections under an active query.
- Revert on `UpdateDetail` failure after a value reorder.

The `OnMove` path has no tests today, so these are the first.

## Out of scope

- Cross-section drag (drag to select or deselect).
- Changes to the read-only picker layout.
- Changes to other multi-select pickers (`ObjectValueViewModel` already
  sorts selected-first).
