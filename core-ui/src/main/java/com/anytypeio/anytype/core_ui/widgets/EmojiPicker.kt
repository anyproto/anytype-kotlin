package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.emojifier.data.Emoji
import coil3.compose.AsyncImage
import com.anytypeio.anytype.presentation.picker.EmojiPickerView

/**
 * Generic emoji picker screen that can be reused across different features.
 *
 * @param views List of items to display (emojis, categories, sections)
 * @param onEmojiClicked Callback when an emoji is clicked
 * @param onQueryChanged Callback when search query changes
 * @param showDragger Whether to show the bottom sheet dragger
 * @param showSearch Whether to show the search field
 */
@Composable
fun EmojiPickerScreen(
    views: List<EmojiPickerView> = emptyList(),
    onEmojiClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    showDragger: Boolean = true,
    showSearch: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        if (showDragger) {
            Dragger(
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        if (showSearch) {
            SearchField(
                onQueryChanged = onQueryChanged,
                onFocused = {
                    // Do nothing
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                views,
                span = { item ->
                    when (item) {
                        is EmojiPickerView.Emoji -> {
                            GridItemSpan(1)
                        }
                        is EmojiPickerView.Category, is EmojiPickerView.Section -> {
                            GridItemSpan(maxLineSpan)
                        }
                    }
                }
            ) { item ->
                when (item) {
                    is EmojiPickerView.Emoji -> {
                        if (item.emojified.isNotEmpty()) {
                            AsyncImage(
                                model = item.emojified,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(32.dp)
                                    .noRippleClickable {
                                        onEmojiClicked(item.unicode)
                                    }
                            )
                        } else {
                            Text(
                                item.unicode,
                                fontSize = 32.sp
                            )
                        }
                    }
                    is EmojiPickerView.Category -> {
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                        ) {
                            val text = when (item.index) {
                                Emoji.CATEGORY_SMILEYS_AND_PEOPLE -> stringResource(R.string.emoji_category_smileys_and_people)
                                Emoji.CATEGORY_ANIMALS_AND_NATURE -> stringResource(R.string.emoji_category_animals_and_nature)
                                Emoji.CATEGORY_FOOD_AND_DRINK -> stringResource(R.string.emoji_category_food_and_drink)
                                Emoji.CATEGORY_ACTIVITY_AND_SPORT -> stringResource(R.string.emoji_category_activity_and_sport)
                                Emoji.CATEGORY_TRAVEL_AND_PLACES -> stringResource(R.string.emoji_category_travel_and_places)
                                Emoji.CATEGORY_OBJECTS -> stringResource(R.string.emoji_category_objects)
                                Emoji.CATEGORY_SYMBOLS -> stringResource(R.string.emoji_category_symbols)
                                Emoji.CATEGORY_FLAGS -> stringResource(R.string.emoji_category_flags)
                                else -> ""
                            }
                            Text(
                                text = text,
                                color = colorResource(R.color.text_secondary),
                                modifier = Modifier.align(Alignment.Center),
                                style = Caption1Medium
                            )
                        }
                    }
                    is EmojiPickerView.Section -> {
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = item.title,
                                color = colorResource(R.color.text_secondary),
                                modifier = Modifier.align(Alignment.Center),
                                style = Caption1Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
