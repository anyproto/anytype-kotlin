package com.anytypeio.anytype.feature_chats.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReactionList(
    reactions: List<ChatView.Message.Reaction>,
    onReacted: (String) -> Unit,
    onViewReaction: (String) -> Unit,
    onAddNewReaction: () -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reactions.forEach { reaction ->
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(46.dp)
                    .background(
                        color = if (reaction.isSelected)
                            colorResource(id = R.color.palette_very_light_orange)
                        else
                            colorResource(id = R.color.shape_transparent_primary),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clip(RoundedCornerShape(100.dp))
                    .then(
                        if (reaction.isSelected)
                            Modifier.border(
                                width = 1.dp,
                                color = colorResource(id = R.color.palette_system_amber_50),
                                shape = RoundedCornerShape(100.dp)
                            )
                        else
                            Modifier
                    )
                    .combinedClickable(
                        onClick = {
                            onReacted(reaction.emoji)
                        },
                        onLongClick = {
                            onViewReaction(reaction.emoji)
                        }
                    )
            ) {
                Text(
                    text = reaction.emoji,
                    style = BodyCalloutMedium,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterStart
                        )
                        .padding(
                            start = 8.dp
                        )
                )
                Text(
                    text = reaction.count.toString(),
                    style = Caption1Regular,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterEnd
                        )
                        .padding(
                            end = 8.dp
                        ),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .clickable {
                    onAddNewReaction()
                }
        ) {
            Image(
                painter = painterResource(R.drawable.ic_add_reaction),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@DefaultPreviews
@Composable
fun ReactionListPreview() {
    ReactionList(
        reactions = listOf(
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = true
            ),
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            ChatView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            )
        ),
        onReacted = {},
        onViewReaction = {},
        onAddNewReaction = {}
    )
}