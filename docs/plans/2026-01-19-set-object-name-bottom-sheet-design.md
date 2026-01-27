# Set Object Name Bottom Sheet - Design Document

**Date:** 2026-01-19
**Issue:** [DROID-3986](https://linear.app/anytype/issue/DROID-3986/set-or-newly-created-tasks-not-visible-without-scrolling)
**Related PR:** [#3044](https://github.com/anyproto/anytype-kotlin/pull/3044)

## Problem

When creating a new object in a Set/Collection:
1. Object is created as "Untitled" and scrolled into view
2. User types a name in the naming dialog
3. Object position changes due to sorting (e.g., Name ASC)
4. List does NOT scroll to the object's new position
5. Result: object appears to "disappear" after naming

**Root cause:** The naming dialog uses a separate `ObjectSetRecordViewModel`, which cannot communicate back to `ObjectSetViewModel` to trigger a re-scroll after rename.

## Solution

Replace the Fragment-based naming dialog with a Compose `ModalBottomSheet` that uses `ObjectSetViewModel` directly. This keeps all state in one place, allowing the VM to trigger a scroll after the name is saved and the sheet is dismissed.

## Design

### Architecture

```
ObjectSetFragment
    └── SetObjectNameBottomSheet (Compose)
            ├── Uses ObjectSetViewModel directly
            ├── Auto-saves name on each keystroke
            └── Triggers scroll on dismiss
```

### State Management

New state in `ObjectSetViewModel`:

```kotlin
data class SetObjectNameState(
    val isVisible: Boolean = false,
    val targetObjectId: Id? = null,
    val currentIcon: ObjectIcon? = null,
    val inputText: String = "",
    val isIconPickerVisible: Boolean = false
)

private val _setObjectNameState = MutableStateFlow(SetObjectNameState())
val setObjectNameState: StateFlow<SetObjectNameState> = _setObjectNameState
```

### User Flow

1. **Object created** → `showSetObjectNameSheet(objectId, icon)` called
2. **User types** → Name saved immediately via `SetObjectDetails` (debounced)
3. **User presses Done** → Close sheet, set `pendingScrollToObject`, scroll triggers
4. **User dismisses (swipe/tap outside)** → Same as Done - close sheet, scroll to object
5. **User taps icon button** → Show nested `ModalBottomSheet` with `EmojiPickerScreen`
6. **User selects emoji** → Save icon, close picker, return to name sheet

### UI Layout (from Figma)

```
┌─────────────────────────────────────────────┐
│  ┌──────────────────────────────────────┐   │
│  │ [icon] │ Untitled              │ [↗] │   │  ← Floating card
│  └──────────────────────────────────────┘   │     16dp corners, shadow
│                                             │
│              [ Keyboard ]                   │
│                              [ Done ]       │  ← Blue Done button
└─────────────────────────────────────────────┘
```

**Components:**
- **Container:** Material 3 `ModalBottomSheet`, `skipPartiallyExpanded = true`
- **Card:** `Surface` with 16dp corner radius, elevation shadow
- **Icon button (left):** Shows current icon or placeholder, opens emoji picker
- **Text field (center):** `BasicTextField`, "Untitled" placeholder (tertiary color)
- **Open button (right):** 24dp icon, navigates to full object editor
- **Keyboard animation:** `WindowInsets.ime` with smooth animation

### Nested Icon Picker

When user taps the icon button:

```
┌─────────────────────────────────────────────┐
│                                             │
│         [ EmojiPickerScreen ]               │  ← Second ModalBottomSheet
│         - Search field                      │
│         - Emoji grid                        │
│                                             │
├─────────────────────────────────────────────┤
│  ┌──────────────────────────────────────┐   │
│  │ [icon] │ Title text            │ [↗] │   │  ← First sheet (behind)
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

Uses existing `EmojiPickerScreen` from `core-ui`.

### Error Handling

| Scenario | Behavior |
|----------|----------|
| Save failure | Silent retry, toast on persistent failure |
| Object deleted externally | Dismiss sheet |
| Configuration change | State preserved via StateFlow |
| Back button/gesture | Same as dismiss - close + scroll |

## Files to Change

| File | Action | Description |
|------|--------|-------------|
| `core-ui/.../SetObjectNameBottomSheet.kt` | Create | New Compose bottom sheet UI |
| `presentation/.../ObjectSetViewModel.kt` | Modify | Add state & handlers for name sheet |
| `presentation/.../ObjectSetCommand.kt` | Modify | Remove `SetNameForCreatedObject` command |
| `app/.../ObjectSetFragment.kt` | Modify | Host Compose sheet, remove old navigation |
| `app/.../modals/SetObjectCreateRecordFragment*.kt` | Remove | No longer needed |
| `app/.../navigation/graph.xml` | Modify | Remove `setNameForNewRecordScreen` destination |

## Out of Scope

- Changes to other object creation flows (editor, widgets)
- Refactoring the existing icon picker to Compose
- Changes to the grid/list/gallery view rendering

## Testing

- Unit tests for new ViewModel state management
- Manual testing:
  - Create object in Set with Name ASC sort → verify scroll after naming
  - Create object in Collection with various view types
  - Test icon picker flow
  - Test keyboard animation smoothness
  - Test dismiss behaviors (Done, swipe, tap outside, back)
