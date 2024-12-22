package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView


@Composable
fun ChatReactionPicker(
    views: List<EmojiPickerView> = emptyList()
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        modifier = Modifier.fillMaxSize().padding(
            start = 16.dp,
            end = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
        item {
            Text(
                "😀",
                fontSize = 32.sp
            )
        }
    }
}

@DefaultPreviews
@Composable
fun PickerPreview() {
    ChatReactionPicker(
        views = buildList {
            add(
                EmojiPickerView.Emoji(
                    unicode = "😀",
                    page = 1,
                    index = 1
                )
            )
        }
    )
}