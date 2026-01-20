# Set Object Name Bottom Sheet Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace Fragment-based naming dialog with Compose ModalBottomSheet using ObjectSetViewModel directly, enabling scroll-to-object after rename.

**Architecture:** New Compose bottom sheet hosted in ObjectSetFragment, sharing state with ObjectSetViewModel. Auto-saves name on keystroke, triggers scroll on dismiss. Nested bottom sheet for emoji picker.

**Tech Stack:** Jetpack Compose, Material 3 ModalBottomSheet, Kotlin Coroutines/StateFlow, existing EmojiProvider/EmojiSuggester

---

## Task 1: Add State to ObjectSetViewModel

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt`

**Step 1: Add the state data class**

Add after line ~245 (near `pendingScrollToObject`):

```kotlin
/**
 * State for the Set Object Name bottom sheet.
 */
data class SetObjectNameState(
    val isVisible: Boolean = false,
    val targetObjectId: Id? = null,
    val currentIcon: ObjectIcon = ObjectIcon.None,
    val inputText: String = "",
    val isIconPickerVisible: Boolean = false
)
```

**Step 2: Add StateFlow for the state**

Add after the data class:

```kotlin
private val _setObjectNameState = MutableStateFlow(SetObjectNameState())
val setObjectNameState: StateFlow<SetObjectNameState> = _setObjectNameState.asStateFlow()
```

**Step 3: Add required import**

Add at top of file:

```kotlin
import com.anytypeio.anytype.presentation.objects.ObjectIcon
```

**Step 4: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt
git commit -m "feat(DROID-3986): add SetObjectNameState to ObjectSetViewModel"
```

---

## Task 2: Add ViewModel Methods for Name Sheet

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt`

**Step 1: Add method to show the bottom sheet**

Add in the public methods section:

```kotlin
/**
 * Shows the set object name bottom sheet for a newly created object.
 */
fun showSetObjectNameSheet(objectId: Id, icon: ObjectIcon) {
    _setObjectNameState.value = SetObjectNameState(
        isVisible = true,
        targetObjectId = objectId,
        currentIcon = icon,
        inputText = "",
        isIconPickerVisible = false
    )
}
```

**Step 2: Add method to handle text changes (auto-save)**

```kotlin
/**
 * Called when user types in the name field. Auto-saves to backend.
 */
fun onSetObjectNameChanged(text: String) {
    val state = _setObjectNameState.value
    val targetId = state.targetObjectId ?: return

    _setObjectNameState.value = state.copy(inputText = text)

    viewModelScope.launch {
        val params = SetObjectDetails.Params(
            ctx = targetId,
            details = mapOf(Relations.NAME to text)
        )
        setObjectDetails.async(params).fold(
            onFailure = { Timber.e(it, "Error while updating object name") },
            onSuccess = { /* saved successfully */ }
        )
    }
}
```

**Step 3: Add method to dismiss the sheet**

```kotlin
/**
 * Called when bottom sheet is dismissed (Done pressed or swipe).
 * Triggers scroll to the object.
 */
fun onSetObjectNameDismissed() {
    val objectId = _setObjectNameState.value.targetObjectId
    _setObjectNameState.value = SetObjectNameState()

    if (objectId != null) {
        pendingScrollToObject.value = objectId
    }
}
```

**Step 4: Add methods for icon picker**

```kotlin
/**
 * Shows the nested emoji picker.
 */
fun onSetObjectNameIconClicked() {
    _setObjectNameState.value = _setObjectNameState.value.copy(isIconPickerVisible = true)
}

/**
 * Hides the nested emoji picker.
 */
fun onIconPickerDismissed() {
    _setObjectNameState.value = _setObjectNameState.value.copy(isIconPickerVisible = false)
}

/**
 * Called when user selects an emoji from the picker.
 */
fun onIconSelected(emoji: String) {
    val state = _setObjectNameState.value
    val targetId = state.targetObjectId ?: return

    _setObjectNameState.value = state.copy(
        currentIcon = ObjectIcon.Basic.Emoji(unicode = emoji),
        isIconPickerVisible = false
    )

    viewModelScope.launch {
        val params = SetObjectDetails.Params(
            ctx = targetId,
            details = mapOf(Relations.ICON_EMOJI to emoji)
        )
        setObjectDetails.async(params).fold(
            onFailure = { Timber.e(it, "Error while updating object icon") },
            onSuccess = { /* saved successfully */ }
        )
    }
}
```

**Step 5: Add method to open object in full screen**

```kotlin
/**
 * Opens the object in full editor from the name sheet.
 */
fun onSetObjectNameOpenClicked() {
    val state = _setObjectNameState.value
    val targetId = state.targetObjectId ?: return

    _setObjectNameState.value = SetObjectNameState()

    viewModelScope.launch {
        proceedWithOpeningObject(
            target = targetId,
            layout = null,
            space = vmParams.space.id
        )
    }
}
```

**Step 6: Add required import for Relations**

Ensure this import exists:

```kotlin
import com.anytypeio.anytype.core_models.Relations
```

**Step 7: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt
git commit -m "feat(DROID-3986): add methods for SetObjectName bottom sheet"
```

---

## Task 3: Update proceedWithNewDataViewObject to Use New Sheet

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt`

**Step 1: Find and modify proceedWithNewDataViewObject**

Find the `proceedWithNewDataViewObject` function (around line 1592) and replace the else branch that dispatches `SetNameForCreatedObject`:

Before:
```kotlin
else -> {
    dispatch(
        ObjectSetCommand.Modal.SetNameForCreatedObject(
            ctx = vmParams.ctx,
            target = response.objectId,
            space = vmParams.space.id
        )
    )
}
```

After:
```kotlin
else -> {
    val obj = ObjectWrapper.Basic(response.struct.orEmpty())
    val icon = obj.iconEmoji?.let { ObjectIcon.Basic.Emoji(unicode = it) }
        ?: ObjectIcon.None
    showSetObjectNameSheet(objectId = response.objectId, icon = icon)
}
```

**Step 2: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt
git commit -m "feat(DROID-3986): use Compose sheet instead of Fragment for naming"
```

---

## Task 4: Create SetObjectNameBottomSheet Composable

**Files:**
- Create: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/features/sets/SetObjectNameBottomSheet.kt`

**Step 1: Create the file with imports and state**

```kotlin
package com.anytypeio.anytype.core_ui.features.sets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetObjectNameBottomSheet(
    isVisible: Boolean,
    icon: ObjectIcon,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onIconClicked: () -> Unit,
    onOpenClicked: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        windowInsets = WindowInsets(0)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_secondary),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon button
                    ListWidgetObjectIcon(
                        modifier = Modifier
                            .size(20.dp)
                            .noRippleClickable { onIconClicked() },
                        icon = if (icon == ObjectIcon.None) {
                            ObjectIcon.Basic.Emoji(unicode = "\uD83D\uDE42") // placeholder smiley
                        } else {
                            icon
                        },
                        iconSize = 20.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text field
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onTextChanged(newValue.text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = PreviewTitle1Medium.copy(
                            color = colorResource(id = R.color.text_primary)
                        ),
                        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onDismiss()
                            }
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.untitled),
                                        style = PreviewTitle1Medium,
                                        color = colorResource(id = R.color.text_tertiary)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Open button
                    Icon(
                        painter = painterResource(id = R.drawable.ic_open_to_edit_24),
                        contentDescription = "Open object",
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable { onOpenClicked() },
                        tint = colorResource(id = R.color.glyph_active)
                    )
                }
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add core-ui/src/main/java/com/anytypeio/anytype/core_ui/features/sets/SetObjectNameBottomSheet.kt
git commit -m "feat(DROID-3986): create SetObjectNameBottomSheet composable"
```

---

## Task 5: Add Icon Picker Bottom Sheet Wrapper

**Files:**
- Modify: `core-ui/src/main/java/com/anytypeio/anytype/core_ui/features/sets/SetObjectNameBottomSheet.kt`

**Step 1: Add the nested icon picker composable**

Add after the `SetObjectNameBottomSheet` function:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetObjectIconPickerBottomSheet(
    isVisible: Boolean,
    views: List<com.anytypeio.anytype.presentation.picker.EmojiPickerView>,
    onEmojiClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        com.anytypeio.anytype.core_ui.widgets.EmojiPickerScreen(
            views = views,
            onEmojiClicked = onEmojiClicked,
            onQueryChanged = onQueryChanged,
            showDragger = false
        )
    }
}
```

**Step 2: Add missing import**

```kotlin
import com.anytypeio.anytype.presentation.picker.EmojiPickerView
```

**Step 3: Commit**

```bash
git add core-ui/src/main/java/com/anytypeio/anytype/core_ui/features/sets/SetObjectNameBottomSheet.kt
git commit -m "feat(DROID-3986): add SetObjectIconPickerBottomSheet"
```

---

## Task 6: Add Open-to-Edit Icon Resource

**Files:**
- Check if exists: `core-ui/src/main/res/drawable/ic_open_to_edit_24.xml`

**Step 1: Check if icon exists**

```bash
ls -la core-ui/src/main/res/drawable/ic_open_to_edit*.xml 2>/dev/null || echo "Icon not found"
```

**Step 2: If not found, create the icon**

Create `core-ui/src/main/res/drawable/ic_open_to_edit_24.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:pathData="M14,3L14,5L17.59,5L7.76,14.83L9.17,16.24L19,6.41L19,10L21,10L21,3L14,3Z"
        android:fillColor="#A7A7A7"/>
    <path
        android:pathData="M19,19L5,19L5,5L12,5L12,3L5,3C3.9,3 3,3.9 3,5L3,19C3,20.1 3.9,21 5,21L19,21C20.1,21 21,20.1 21,19L21,12L19,12L19,19Z"
        android:fillColor="#A7A7A7"/>
</vector>
```

**Step 3: Commit (if icon was created)**

```bash
git add core-ui/src/main/res/drawable/ic_open_to_edit_24.xml
git commit -m "feat(DROID-3986): add open-to-edit icon"
```

---

## Task 7: Add Emoji State to ObjectSetViewModel

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt`

**Step 1: Add emoji provider and suggester as dependencies**

The ObjectSetViewModel needs access to EmojiProvider and EmojiSuggester for the icon picker. Check if they're already injected, if not add them to constructor.

**Step 2: Add emoji views state**

Add after `setObjectNameState`:

```kotlin
private val _emojiPickerViews = MutableStateFlow<List<EmojiPickerView>>(emptyList())
val emojiPickerViews: StateFlow<List<EmojiPickerView>> = _emojiPickerViews.asStateFlow()

private val emojiQuery = MutableStateFlow("")
```

**Step 3: Add emoji loading logic**

Add method:

```kotlin
private fun loadEmojiViews() {
    viewModelScope.launch {
        val views = withContext(Dispatchers.IO) {
            val result = mutableListOf<EmojiPickerView>()
            emojiProvider.emojis.forEachIndexed { categoryIndex, emojis ->
                result.add(EmojiPickerView.Category(index = categoryIndex))
                emojis.forEachIndexed { emojiIndex, emoji ->
                    val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                    if (!skin) {
                        result.add(
                            EmojiPickerView.Emoji(
                                unicode = emoji,
                                page = categoryIndex,
                                index = emojiIndex
                            )
                        )
                    }
                }
            }
            result
        }
        _emojiPickerViews.value = views
    }
}

fun onEmojiQueryChanged(query: String) {
    emojiQuery.value = query
    viewModelScope.launch {
        if (query.isEmpty()) {
            loadEmojiViews()
        } else {
            val suggests = emojiSuggester.search(query)
            val filtered = suggests.mapNotNull { suggest ->
                emojiProvider.emojis.forEachIndexed { categoryIndex, emojis ->
                    emojis.forEachIndexed { emojiIndex, emoji ->
                        if (emoji == suggest.emoji) {
                            return@mapNotNull EmojiPickerView.Emoji(
                                unicode = emoji,
                                page = categoryIndex,
                                index = emojiIndex
                            )
                        }
                    }
                }
                null
            }
            _emojiPickerViews.value = filtered
        }
    }
}
```

**Step 4: Add required imports**

```kotlin
import com.anytypeio.anytype.presentation.picker.EmojiPickerView
import com.anytypeio.anytype.emojifier.data.Emoji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

**Step 5: Call loadEmojiViews when icon picker opens**

Update `onSetObjectNameIconClicked`:

```kotlin
fun onSetObjectNameIconClicked() {
    _setObjectNameState.value = _setObjectNameState.value.copy(isIconPickerVisible = true)
    if (_emojiPickerViews.value.isEmpty()) {
        loadEmojiViews()
    }
}
```

**Step 6: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt
git commit -m "feat(DROID-3986): add emoji picker state to ObjectSetViewModel"
```

---

## Task 8: Integrate Bottom Sheet in ObjectSetFragment

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt`

**Step 1: Add ComposeView for the bottom sheet in XML or programmatically**

Find where other ComposeViews are set up (around line 410-480) and add:

```kotlin
binding.setObjectNameSheet.apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        val state by vm.setObjectNameState.collectAsStateWithLifecycle()
        val emojiViews by vm.emojiPickerViews.collectAsStateWithLifecycle()

        SetObjectNameBottomSheet(
            isVisible = state.isVisible,
            icon = state.currentIcon,
            onTextChanged = vm::onSetObjectNameChanged,
            onDismiss = vm::onSetObjectNameDismissed,
            onIconClicked = vm::onSetObjectNameIconClicked,
            onOpenClicked = vm::onSetObjectNameOpenClicked
        )

        SetObjectIconPickerBottomSheet(
            isVisible = state.isIconPickerVisible,
            views = emojiViews,
            onEmojiClicked = vm::onIconSelected,
            onQueryChanged = vm::onEmojiQueryChanged,
            onDismiss = vm::onIconPickerDismissed
        )
    }
}
```

**Step 2: Add imports**

```kotlin
import com.anytypeio.anytype.core_ui.features.sets.SetObjectNameBottomSheet
import com.anytypeio.anytype.core_ui.features.sets.SetObjectIconPickerBottomSheet
```

**Step 3: Add ComposeView to layout XML**

Add to `fragment_object_set.xml` (or equivalent):

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/setObjectNameSheet"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**Step 4: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt
git add app/src/main/res/layout/fragment_object_set.xml
git commit -m "feat(DROID-3986): integrate SetObjectNameBottomSheet in fragment"
```

---

## Task 9: Remove Old Navigation to SetNameForCreatedObject

**Files:**
- Modify: `app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt`
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetCommand.kt`

**Step 1: Remove command handler in fragment**

Find and remove the `is ObjectSetCommand.Modal.SetNameForCreatedObject` case in `observeCommands()` function.

**Step 2: Remove command from ObjectSetCommand**

In `ObjectSetCommand.kt`, remove:

```kotlin
data class SetNameForCreatedObject(
    val ctx: Id,
    val target: Id,
    val space: Id
) : Modal()
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/anytypeio/anytype/ui/sets/ObjectSetFragment.kt
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetCommand.kt
git commit -m "refactor(DROID-3986): remove SetNameForCreatedObject command"
```

---

## Task 10: Add EmojiProvider and EmojiSuggester to ObjectSetViewModel

**Files:**
- Modify: `presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt`
- Modify: `app/src/main/java/com/anytypeio/anytype/di/feature/ObjectSetDI.kt` (or equivalent DI file)

**Step 1: Add dependencies to ViewModel constructor**

Add to ObjectSetViewModel constructor:

```kotlin
private val emojiProvider: EmojiProvider,
private val emojiSuggester: EmojiSuggester,
```

**Step 2: Add imports**

```kotlin
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
```

**Step 3: Update ViewModel Factory**

Update the factory to include these dependencies.

**Step 4: Update DI module**

Ensure EmojiProvider and EmojiSuggester are provided in the ObjectSet DI component.

**Step 5: Commit**

```bash
git add presentation/src/main/java/com/anytypeio/anytype/presentation/sets/ObjectSetViewModel.kt
git add app/src/main/java/com/anytypeio/anytype/di/feature/ObjectSetDI.kt
git commit -m "feat(DROID-3986): inject emoji dependencies into ObjectSetViewModel"
```

---

## Task 11: Manual Testing

**Test scenarios:**

1. **Basic flow:**
   - Create object in Set with Name ASC sort
   - Type a name starting with "Z"
   - Press Done
   - Verify: list scrolls to the object

2. **Dismiss flow:**
   - Create object
   - Type a name
   - Swipe to dismiss
   - Verify: list scrolls to the object

3. **Icon picker:**
   - Create object
   - Tap icon button
   - Select emoji
   - Verify: icon updates
   - Press Done
   - Verify: object has new icon

4. **Open button:**
   - Create object
   - Tap open button
   - Verify: navigates to full object editor

5. **View types:**
   - Test in Grid view
   - Test in List view
   - Test in Gallery view

**Step 1: Run app and test**

```bash
./gradlew assembleDebug
# Install and test manually
```

**Step 2: Fix any issues found**

**Step 3: Final commit**

```bash
git add -A
git commit -m "fix(DROID-3986): address testing feedback"
```

---

## Task 12: Clean Up Old Files (Optional)

**Files:**
- Consider removing: `app/src/main/java/com/anytypeio/anytype/ui/sets/modals/SetObjectCreateRecordFragment*.kt`
- Consider removing: Navigation entries for old screen

**Only do this after confirming new implementation works correctly.**

**Step 1: Remove old fragment files**

```bash
rm app/src/main/java/com/anytypeio/anytype/ui/sets/modals/SetObjectCreateRecordFragment*.kt
```

**Step 2: Remove navigation entries**

Update `graph.xml` to remove `setNameForNewRecordScreen` destination.

**Step 3: Commit**

```bash
git add -A
git commit -m "chore(DROID-3986): remove deprecated SetObjectCreateRecord fragments"
```
