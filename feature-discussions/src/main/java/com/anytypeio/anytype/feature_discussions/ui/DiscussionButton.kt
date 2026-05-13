package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun DiscussionButton(
    commentCount: Int,
    hasUnreadMessages: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat_outline_24),
                contentDescription = "Discussion",
                tint = colorResource(id = R.color.control_primary)
            )
            if (hasUnreadMessages) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.palette_system_blue))
                )
            }
        }
        if (commentCount > 0) {
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
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun DiscussionButtonEmptyPreview() {
    DiscussionButton(
        commentCount = 0,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun DiscussionButtonWithCommentsPreview() {
    DiscussionButton(
        commentCount = 5,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun DiscussionButtonWithUnreadPreview() {
    DiscussionButton(
        commentCount = 3,
        hasUnreadMessages = true,
        onClick = {}
    )
}
