package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpaceChatWidgetCard(
    item: WidgetView,
    mode: InteractionMode,
    onWidgetClicked: () -> Unit = {},
    onDropDownMenuAction: (DropDownMenuAction) -> Unit = {},
    unReadMessageCount: Int = 0,
    unReadMentionCount: Int = 0,
    isMuted: Boolean = false
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    val haptic = LocalHapticFeedback.current
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
                    Modifier.combinedClickable(
                        onClick = onWidgetClicked,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isCardMenuExpanded.value = true
                        }
                    )
                } else {
                    Modifier
                }
            )
            .alpha(
                if (isCardMenuExpanded.value) 0.8f else 1f
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
                        color = if (isMuted) colorResource(R.color.glyph_active) else colorResource(
                            R.color.color_accent
                        ),
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
            if (unReadMessageCount == 0) {
                Spacer(modifier = Modifier.width(16.dp))
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
                        color = if (isMuted) colorResource(R.color.glyph_active) else colorResource(
                            R.color.color_accent
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = unReadMessageCount.toString(),
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_white),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        WidgetLongClickMenu(
            menuItems = emptyList(),
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}
