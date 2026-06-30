# Kanban Card Color Background — Design

**Date:** 2026-06-30
**Ticket:** DROID-4529 (follow-up)
**Branch:** `droid-4529-kanban-menu`

## Problem

The "Color columns" toggle (`groupBackgroundColors`) added to the Kanban Layout menu is
persisted to the viewer but currently has **no visual effect** — `buildBoardViews` never
receives it, and a column's group color only ever drives a small color dot in the column
header (`BoardColumnContent`). Cards render on a flat `background_primary`.

## Goal

When "Color columns" is ON, each Kanban **card** gets a subtle tinted background
(`light(groupColor)`) matching its column's group color. Columns keep their existing dot.
When OFF, cards revert to `background_primary`.

## Scope (decided)

- **Cards only**, gated by the existing `groupBackgroundColors` toggle. Columns unchanged.
- Tint = `light(columnColor)`, reusing the same color helper + option-color source already
  used by relation chips and the column dot.

## Data flow

The board mapper functions (`DVViewer.buildBoardViews` → `buildColumnsFromGroups`) are
`DVViewer` extensions, so `groupBackgroundColors` is already in scope — **no render-mapper
plumbing needed**. The column color is already computed at `BoardViewMapper.kt:111` via
`groupColor(...)`; reuse it for the card.

## Changes

### 1. Model — `presentation/.../sets/model/Viewer.kt`
Add one field to `Viewer.Board.Card`:
```kotlin
val backgroundColor: String? = null   // group color when "Color columns" is on, else null
```

### 2. Mapper — `presentation/.../sets/BoardViewMapper.kt`
In `buildColumnsFromGroups` (~95-114), hoist the column color and pass a card color only
when the toggle is on:
```kotlin
val columnColor = groupColor(gid, group, groupOrder, groupOptions, objectStore)
val cardBackgroundColor = if (groupBackgroundColors) columnColor else null
val cards = recordIds
    .mapNotNull { objectStore.get(it) }
    .filter { it.isValid }
    .sortedBy { orderIndex[it.id] ?: Int.MAX_VALUE }
    .map { obj ->
        obj.toCard(urlBuilder, viewerRelations, objectStore, filteredRelations,
                   fieldParser, storeOfObjectTypes, hideIcon, cardBackgroundColor)
    }
Viewer.Board.Column(
    id = gid,
    label = groupLabel(...),
    color = columnColor,        // column dot unchanged
    cards = cards,
    count = countsByColumn[gid] ?: cards.size
)
```
`toCard` (~225) gains a `backgroundColor: String?` param and sets it on the returned `Card`.

### 3. UI — `core-ui/.../widgets/dv/board/BoardCardItem.kt`
Replace the flat background (line 47):
```kotlin
.background(
    if (card.backgroundColor != null) light(card.backgroundColor)
    else colorResource(id = R.color.background_primary)
)
```
`light()` is already imported and handles the non-blank option-color names that
`groupColor`/`optionColor` produce.

## Edge cases

- **Empty / "No value" column** and **Checkbox columns** have `color == null` → their cards
  stay on `background_primary` even when the toggle is on (no tint to apply).
- Toggling "Color columns" off → `cardBackgroundColor == null` for all cards → default
  background.

## Testing

Extend `presentation/src/test/.../BoardViewMapperTest.kt` (it already builds Status/Tag
boards and asserts `columns[n].color`, e.g. `"blue"`):
- With `groupBackgroundColors = true`, a colored column's cards carry
  `backgroundColor == <columnColor>`.
- With `groupBackgroundColors = false`, cards carry `backgroundColor == null`.
- Empty/no-color column's cards stay `null` even when the toggle is on.

UI verified via the existing board preview (no Compose unit test for the card).

## Out of scope
- Coloring the column background itself (only the dot stays).
- Card cover images / any other card visual.
