# DROID-4201: Implementation Guide for Claude Code

## Epic: Redesign Global Create and Chat Attachment Flows

---

## What Has Been Done ✅

### Phase 1: Core UI Components

#### 1. AttachmentMenuPopup Component

**File:** `core-ui/src/main/java/com/anytypeio/anytype/core_ui/menu/AttachmentMenuPopup.kt`

Created a reusable Compose popup component that replaces the old `DropdownMenu`:

```kotlin
// Key classes:
sealed class AttachmentMenuAction {
    data object Photos : AttachmentMenuAction()
    data object Camera : AttachmentMenuAction()
    data object Files : AttachmentMenuAction()
    data object AttachObject : AttachmentMenuAction()
    data class CreateObjectOfType(val typeKey: Key, val typeName: String) : AttachmentMenuAction()
    data object SeeAll : AttachmentMenuAction()
}

data class ObjectTypeMenuItem(
    val typeKey: Key,
    val name: String,
    val icon: ObjectIcon
)
```

#### 2. Drawable Icons

**Location:** `core-ui/src/main/res/drawable/`

Created 5 new icons:

- `ic_attachment_menu_photos.xml` - Gallery/photos icon
- `ic_attachment_menu_camera.xml` - Camera icon
- `ic_attachment_menu_files.xml` - Folder/files icon
- `ic_attachment_menu_link.xml` - Link/attach object icon
- `ic_attachment_menu_see_all.xml` - More options icon

#### 3. Localization Strings

**File:** `localization/src/main/res/values/strings.xml` (lines 2316-2324)

```xml
<!-- Attachment Menu (DROID-4201) -->
<string name="attachment_menu_photos">Photos</string><string name="attachment_menu_camera">Camera
</string><string name="attachment_menu_files">Files</string><string
name="attachment_menu_attach_object">Attach object
</string><string name="attachment_menu_see_all">See all</string><string
name="attachment_menu_search_placeholder">Search
</string><string name="attachment_menu_no_results">No results for "%1$s"</string><string
name="attachment_menu_no_results_hint">Try a different name
</string>
```

#### 4. CreateObjectTypeSelector Modal

**File:**
`feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/CreateObjectTypeSelector.kt`

A full-screen type selector with:

- Scrollable list of object types
- Search bar at the bottom
- "No results" empty state
- Uses existing `ListWidgetObjectIcon` for type icons

---

### Phase 2: ChatBox Integration

#### 5. Updated ChatBox Component

**File:** `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/ChatBox.kt`

**Changes made:**

- Added imports for `AttachmentMenuPopup`, `AttachmentMenuAction`, `ObjectTypeMenuItem`
- Added new function parameters:
  ```kotlin
  quickCreateTypes: List<ObjectTypeMenuItem> = emptyList(),
  onCreateObjectOfType: (typeKey: String, typeName: String) -> Unit = { _, _ -> },
  onSeeAllTypesClicked: () -> Unit = {}
  ```
- Replaced old `DropdownMenu` (was lines 316-430) with `AttachmentMenuPopup` that handles all
  actions via `when(action)` block

---

### Phase 3: ViewModel Updates

#### 6. ChatViewModel Enhancements

**File:**
`feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/presentation/ChatViewModel.kt`

**New StateFlows added (around line 177):**

```kotlin
// Quick create types for attachment menu (DROID-4201)
private val _quickCreateTypes = MutableStateFlow<List<QuickCreateType>>(emptyList())
val quickCreateTypes = _quickCreateTypes

// Show all types selector (DROID-4201)
val showTypeSelectorSheet = MutableStateFlow(false)
```

**New data class (around line 2371):**

```kotlin
data class QuickCreateType(
    val typeKey: String,
    val name: String,
    val icon: ObjectIcon
)
```

**New functions added:**

1. `onCreateObjectOfType(typeKey: String, typeName: String)` - Creates object of specific type and
   attaches to chat
2. `onSeeAllTypesClicked()` - Shows the full type selector sheet
3. `onDismissTypeSelector()` - Hides the type selector sheet
4. `loadQuickCreateTypes()` - Loads available types from `StoreOfObjectTypes`

**Modified `onAttachmentMenuTriggered()`** to call `loadQuickCreateTypes()` when menu opens.

---

### Phase 4: Screen Wiring

#### 7. ChatScreen Updates

**File:** `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/ChatScreen.kt`

**Added parameters to `ChatScreen` function (around line 430):**

```kotlin
quickCreateTypes: List<ObjectTypeMenuItem> = emptyList(),
onCreateObjectOfType: (typeKey: String, typeName: String) -> Unit = { _, _ -> },
onSeeAllTypesClicked: () -> Unit = {}
```

**Updated `ChatScreenWrapper`** to collect `quickCreateTypes` from ViewModel and map to
`ObjectTypeMenuItem`.

---

## What Needs To Be Done 🔲

### Priority 1: Type Selector Sheet Integration

The `CreateObjectTypeSelector` component is created but not yet shown. Need to:

1. **Add ModalBottomSheet in ChatScreenWrapper** to show when `showTypeSelectorSheet` is true:

```kotlin
// In ChatScreenWrapper, after the ChatScreen call:
val showTypeSelector by vm.showTypeSelectorSheet.collectAsStateWithLifecycle()

if (showTypeSelector) {
    ModalBottomSheet(
        onDismissRequest = { vm.onDismissTypeSelector() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CreateObjectTypeSelector(
            objectTypes = /* load all types here */,
            onTypeSelected = { typeKey, typeName ->
                vm.onDismissTypeSelector()
                vm.onCreateObjectOfType(typeKey, typeName)
            },
            onDismiss = { vm.onDismissTypeSelector() }
        )
    }
}
```

2. **Add function to load ALL types** (not just quick create 3) for the full selector:

```kotlin
// In ChatViewModel, add:
private val _allCreateTypes = MutableStateFlow<List<QuickCreateType>>(emptyList())
val allCreateTypes = _allCreateTypes

fun loadAllCreateTypes() {
    viewModelScope.launch {
        val types = storeOfObjectTypes.getAll()
            .filter { /* filter creatable types */ }
            .map { type ->
                QuickCreateType(
                    typeKey = type.uniqueKey,
                    name = type.name.orEmpty(),
                    icon = type.objectIcon()
                )
            }
        _allCreateTypes.value = types
    }
}
```

---

### Priority 2: Widget Screen Integration

The Figma design shows this menu should also appear on the **Widgets/Home screen** when tapping
the "+" button. Need to:

1. **Check** `app/src/main/java/com/anytypeio/anytype/ui/home/WidgetsScreenFragment.kt` for current
   create flow
2. **Reuse** `AttachmentMenuPopup` or create similar flow for widget creation
3. **Wire up** to existing `SelectObjectTypeViewModel` for type selection

---

### Priority 3: Testing & Verification

1. **Build the project:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Run unit tests:**
   ```bash
   make test_debug_all
   ```

3. **Manual testing checklist:**
    - [ ] Open chat, tap "+" button - new popup should appear
    - [ ] Tap "Photos" - should open media picker
    - [ ] Tap "Camera" - should launch camera
    - [ ] Tap "Files" - should open file picker
    - [ ] Tap "Attach object" - should open object search
    - [ ] Tap quick create type (Page/Task/Collection) - should create and navigate
    - [ ] Tap "See all" - should show full type selector with search

---

### Priority 4: Design Polish

Compare implementation with Figma and adjust:

1. **Menu positioning** - Currently uses `DpOffset(8.dp, 40.dp)`, may need adjustment
2. **Item heights** - Should be 52dp per Figma
3. **Corner radius** - Should be 12dp
4. **Icon sizes** - Should be 24dp
5. **Spacing** - Check horizontal padding (24dp per Figma)

---

## Architecture Reference

```
┌─────────────────────────────────────────────────────────────┐
│                     ChatScreenWrapper                        │
│  - Collects quickCreateTypes from ViewModel                  │
│  - Maps QuickCreateType → ObjectTypeMenuItem                 │
│  - Should show ModalBottomSheet for type selector            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       ChatScreen                             │
│  - Passes parameters to ChatBox                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        ChatBox                               │
│  - Shows AttachmentMenuPopup on "+" click                    │
│  - Handles AttachmentMenuAction callbacks                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  AttachmentMenuPopup                         │
│  - Renders Photos, Camera, Files, Attach, Types, See all    │
│  - Calls onAction(AttachmentMenuAction) for each item       │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Files Summary

| Component       | File Path                                                                                        |
|-----------------|--------------------------------------------------------------------------------------------------|
| Popup Component | `core-ui/src/main/java/com/anytypeio/anytype/core_ui/menu/AttachmentMenuPopup.kt`                |
| Type Selector   | `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/CreateObjectTypeSelector.kt` |
| ChatBox UI      | `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/ChatBox.kt`                  |
| ChatScreen      | `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/ui/ChatScreen.kt`               |
| ViewModel       | `feature-chats/src/main/java/com/anytypeio/anytype/feature_chats/presentation/ChatViewModel.kt`  |
| Strings         | `localization/src/main/res/values/strings.xml`                                                   |
| Icons           | `core-ui/src/main/res/drawable/ic_attachment_menu_*.xml`                                         |

---

## Figma Reference

- **Design file:** https://www.figma.com/design/7fKsnyvYuJzMcYhI1xehae/-M--Object
- **Node ID:** 24596-91121
- **Section:** "Create new object"

---

## Git Branch

Current branch: `droid-4201-epic-redesign-global-create-chat-attachment-flows`

---

## Notes for Future Implementation

1. The `CreateObject` use case already supports a `type: TypeKey?` parameter, so no domain layer
   changes were needed
2. Quick create types are loaded on-demand when the attachment menu opens (via
   `onAttachmentMenuTriggered`)
3. The `StoreOfObjectTypes` is already injected in `ChatViewModel`, so type data is readily
   available
4. Consider caching the types list to avoid reloading on every menu open
