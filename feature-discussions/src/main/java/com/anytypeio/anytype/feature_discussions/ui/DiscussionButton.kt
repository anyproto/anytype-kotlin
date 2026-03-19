package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_ui.R

@Composable
fun DiscussionButton(
    hasComments: Boolean,
    commentCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(colorResource(id = R.color.background_primary))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hasComments) {
            Text(
                text = commentCount.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary)
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat_type_24),
                contentDescription = "Discussion",
                tint = colorResource(id = R.color.glyph_active)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun DiscussionButtonEmptyPreview() {
    DiscussionButton(
        hasComments = false,
        commentCount = 0,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE)
@Composable
private fun DiscussionButtonWithCommentsPreview() {
    DiscussionButton(
        hasComments = true,
        commentCount = 5,
        onClick = {}
    )
}
