package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.home.InteractionMode

@Composable
fun SpaceChatWidgetCard(
    mode: InteractionMode,
    onWidgetClicked: () -> Unit = {},
    unReadMessageCount: Int = 0,
    unReadMentionCount: Int = 0
) {
    Row(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
            .height(52.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (mode !is InteractionMode.Edit) {
                    Modifier.clickable {
                        onWidgetClicked()
                    }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_widget_chat),
            contentDescription = "All content icon",
            modifier = Modifier
                .padding(start = 16.dp)
        )

        Text(
            text = stringResource(R.string.chat),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp),
            style = HeadlineSubheading,
            color = colorResource(id = R.color.text_primary),
        )

        if (unReadMentionCount > 0) {
            Box(
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.transparent_active),
                        shape = CircleShape
                    )
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_widget_mention),
                    contentDescription = null
                )
            }
        }

        if (unReadMessageCount > 0) {
            if (unReadMentionCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .defaultMinSize(minWidth = 20.dp)
                    .background(
                        color = colorResource(R.color.transparent_active),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unReadMentionCount.toString(),
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_white),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@DefaultPreviews
@Composable
fun SpaceChatWidgetPreview() {
    SpaceChatWidgetCard(
        onWidgetClicked = {},
        mode = InteractionMode.Default,
        unReadMessageCount = 1,
        unReadMentionCount = 1
    )
}
