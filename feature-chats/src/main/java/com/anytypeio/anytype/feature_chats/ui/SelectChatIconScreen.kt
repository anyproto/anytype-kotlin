package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_ui.widgets.EmojiPickerScreen
import com.anytypeio.anytype.presentation.picker.EmojiPickerView

@Composable
fun SelectChatIconScreen(
    views: List<EmojiPickerView> = emptyList(),
    onEmojiClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    EmojiPickerScreen(
        views = views,
        onEmojiClicked = onEmojiClicked,
        onQueryChanged = onQueryChanged
    )
}
