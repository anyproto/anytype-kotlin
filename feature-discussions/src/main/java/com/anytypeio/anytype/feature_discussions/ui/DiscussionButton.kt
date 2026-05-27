package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@Composable
fun DiscussionButton(
    commentCount: Int,
    hasUnreadMessages: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(296.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_chat_outline_24),
                contentDescription = "Discussion",
                tint = colorResource(id = R.color.control_primary)
            )
            if (hasUnreadMessages) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = -2.5.dp, y = (0.5).dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.text_label_inversion)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.color_accent))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = commentCount.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.text_primary),
            letterSpacing = (-0.24).sp
        )
    }
}

@DefaultPreviews
@Composable
private fun DiscussionButtonEmptyPreview() {
    DiscussionButton(
        commentCount = 0,
        onClick = {}
    )
}

@DefaultPreviews
@Composable
private fun DiscussionButtonWithCommentsPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        DiscussionButton(
            commentCount = 5,
            onClick = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun DiscussionButtonWithUnreadPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        DiscussionButton(
            commentCount = 3,
            hasUnreadMessages = true,
            onClick = {}
        )
    }
}
