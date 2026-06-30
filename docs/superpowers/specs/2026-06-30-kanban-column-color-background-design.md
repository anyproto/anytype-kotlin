# Kanban Column Color Background — Design

**Date:** 2026-06-30
**Ticket:** DROID-4529 (follow-up)
**Branch:** `droid-4529-kanban-menu`

> Supersedes the earlier card-tint design: the colored background goes on the **column**,
> not the individual cards.

## Problem

The "Color columns" toggle (`groupBackgroundColors`) added to the Kanban Layout menu is
persisted to the viewer but currently has **no visual effect** — `buildBoardViews` never
applies it, and a column's group color only ever drives a small color dot in the column
header (`BoardColumnContent`). Columns sit on a flat `shape_tertiary` background.

## Goal

When "Color columns" is ON, each Kanban **column** background is tinted
(`light(groupColor)`) with its group color. Columns keep their existing color dot. When OFF,
columns revert to `shape_tertiary`. The drop-target highlight still takes precedence while a
card hovers over another column.

## Scope (decided)

- **Column background only**, gated by the existing `groupBackgroundColors` toggle. Cards are
  unchanged (flat `background_primary`), so they stand out against the tinted column.
- Tint = `light(columnColor)`, reusing the same color helper + option-color source already
  used by relation chips and the column dot.

## Data flow

The board mapper functions (`DVViewer.buildBoardViews` → `buildColumnsFromGroups`) are
`DVViewer` extensions, so `groupBackgroundColors` is already in scope — **no render-mapper
plumbing needed**. The column color is already computed at `BoardViewMapper.kt:101` via
`groupColor(...)`; reuse it for the column background.

## Changes

### 1. Model — `presentation/.../sets/model/Viewer.kt`
Add one field to `Viewer.Board.Column`:
```kotlin
val backgroundColor: String? = null   // group color tint when "Color columns" is on, else null
```

### 2. Mapper — `presentation/.../sets/BoardViewMapper.kt`
In `buildColumnsFromGroups`, set the column's `backgroundColor` only when the toggle is on:
```kotlin
val columnColor = groupColor(gid, group, groupOrder, groupOptions, objectStore)
val columnBackgroundColor = if (groupBackgroundColors) columnColor else null
// ...
Viewer.Board.Column(
    id = gid,
    label = groupLabel(...),
    color = columnColor,                 // dot unchanged
    backgroundColor = columnBackgroundColor,
    cards = cards,
    count = countsByColumn[gid] ?: cards.size
)
```
(`toCard` is unchanged — cards are not tinted.)

### 3. UI — `core-ui/.../widgets/dv/board/BoardColumnContent.kt`
Tint the column background when set, keeping drop-target precedence:
```kotlin
val columnBackgroundColor = column.backgroundColor
val background = when {
    isDropTarget -> colorResource(id = R.color.shape_secondary)
    columnBackgroundColor != null -> light(columnBackgroundColor)
    else -> colorResource(id = R.color.shape_tertiary)
}
```
Add the `light` import (sibling of the already-imported `dark`).

## Edge cases

- **Empty / "No value" column** and **Checkbox columns** have `color == null` → their
  background stays `shape_tertiary` even when the toggle is on (no tint to apply).
- Toggling "Color columns" off → `backgroundColor == null` for all columns → default.
- Drop-target highlight (`shape_secondary`) still wins while dragging.

## Testing

`presentation/src/test/.../BoardViewMapperTest.kt`:
- With `groupBackgroundColors = true`, colored columns carry `backgroundColor == <columnColor>`
  while keeping their `color` dot; the empty/no-color column stays `null`.
- With `groupBackgroundColors = false`, all columns carry `backgroundColor == null` while dots
  are unchanged.

Column UI verified via the existing board preview.

## Out of scope
- Coloring the **cards** (reverted; cards keep `background_primary`).
- Card cover images / any other card visual.
