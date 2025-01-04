package com.anytypeio.anytype.feature_discussions.ui

import com.anytypeio.anytype.feature_discussions.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular

@Composable
fun ChatReactionScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Dragger(modifier = Modifier.padding(vertical = 6.dp))
        EmojiToolbar()

    }
}

@Composable
fun EmojiToolbar() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text( "😀")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "0",
                style = BodyRegular
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            stringResource(R.string.chat_message_reactions_no_reactions_yet),
            style = BodyRegular,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.text_primary)
        )
        Text(
            stringResource(R.string.chat_message_reactions_no_reactions_message),
            style = BodyRegular,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.text_primary)
        )
    }
}

@DefaultPreviews
@Composable
private fun EmojiToolbarPreview() {
    EmojiToolbar()
}

@DefaultPreviews
@Composable
private fun EmptyStatePreview() {
    EmptyState()
}

@DefaultPreviews
@Composable
private fun ChatReactionScreenPreview() {
    ChatReactionScreen()
}