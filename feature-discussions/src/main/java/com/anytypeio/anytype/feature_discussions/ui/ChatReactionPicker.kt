package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.ChatReactionViewModel.ReactionPickerView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChatReactionPicker(
    views: List<ReactionPickerView> = emptyList()
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .background(color = colorResource(R.color.background_secondary))
                .systemBarsPadding()
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
                    when(item) {
                        is ReactionPickerView.Emoji -> {
                            GridItemSpan(1)
                        }
                        is ReactionPickerView.Category -> {
                            GridItemSpan(maxLineSpan)
                        }
                    }
                }
            ) { item ->
                when(item) {
                    is ReactionPickerView.Emoji -> {
                        if (item.emojified.isNotEmpty()) {
                            GlideImage(
                                model = item.emojified,
                                contentDescription = "",
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                item.unicode,
                                fontSize = 32.sp
                            )
                        }
                    }
                    is ReactionPickerView.Category -> {
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
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun PickerPreview() {
    ChatReactionPicker(
        views = buildList {
            add(
                ReactionPickerView.Emoji(
                    unicode = "😀",
                    page = 1,
                    index = 1
                )
            )
        }
    )
}