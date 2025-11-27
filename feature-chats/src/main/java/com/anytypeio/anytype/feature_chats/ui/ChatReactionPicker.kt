package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.EmojiPickerScreen
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.SelectChatReactionViewModel
import com.anytypeio.anytype.presentation.picker.EmojiPickerView

@Composable
fun SelectChatReactionScreen(
    views: List<EmojiPickerView> = emptyList(),
    onEmojiClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val recentlyUsedTitle = stringResource(R.string.emoji_recently_used_section)
    
    // Replace the section key with localized title
    val localizedViews = remember(views, recentlyUsedTitle) {
        views.map { item ->
            if (item is EmojiPickerView.Section && item.title == SelectChatReactionViewModel.RECENTLY_USED_SECTION_KEY) {
                EmojiPickerView.Section(recentlyUsedTitle)
            } else {
                item
            }
        }
    }

    EmojiPickerScreen(
        views = localizedViews,
        onEmojiClicked = onEmojiClicked,
        onQueryChanged = onQueryChanged
    )
}

@DefaultPreviews
@Composable
fun PickerPreview() {
    EmojiPickerScreen(
        views = buildList {
            add(
                EmojiPickerView.Emoji(
                    unicode = "😀",
                    page = 1,
                    index = 1
                )
            )
        },
        onEmojiClicked = {},
        onQueryChanged = {}
    )
}
