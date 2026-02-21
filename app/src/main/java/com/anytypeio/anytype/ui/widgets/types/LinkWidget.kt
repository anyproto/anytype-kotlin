package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem

@Composable
fun LinkWidgetCard(
    item: WidgetView.Link,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    menuItems: List<WidgetMenuItem> = emptyList(),
    isCardMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
    hideCounters: Boolean = false
) {
    Box(modifier = modifier) {

        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ListWidgetObjectIcon(
                iconSize = 20.dp,
                icon = item.icon,
                modifier = Modifier.padding(end = 12.dp),
                onTaskIconClicked = { isChecked ->
                    onObjectCheckboxClicked(
                        item.source.id,
                        isChecked
                    )
                }
            )

            Text(
                text = item.getPrettyName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = BodySemiBold,
                color = colorResource(id = R.color.text_primary),
            )

            // Display chat counter badges with notification-aware colors
            if (!hideCounters) {
                ChatCounterBadges(
                    counter = item.counter,
                    notificationState = item.notificationState,
                    modifier = Modifier
                )
            }
        }
        WidgetLongClickMenu(
            menuItems = menuItems,
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Preview(name = "Link Widget Card Preview", showBackground = true)
@Composable
private fun LinkWidgetCardPreview() {
    val previewLinkItem = WidgetView.Link(
        source = Widget.Source.Default(
            obj = ObjectWrapper.Basic(
                mapOf(
                    Relations.LAYOUT to ObjectType.Layout.CHAT_DERIVED,
                    Relations.NAME to "General chat",
                    Relations.ID to "fsdfsdf"
                )
            )
        ),
        icon = ObjectIcon.TypeIcon.Default(
            rawValue = "chatbubble",
            color = CustomIconColor.Teal
        ),
        name = WidgetView.Name.Default("General chat"),
        counter = WidgetView.ChatCounter(
            unreadMentionCount = 1,
            unreadMessageCount = 5
        ),
        id = "fsdfds",
        notificationState = NotificationState.ALL
    )

    MaterialTheme {
        LinkWidgetCard(
            item = previewLinkItem,
            onDropDownMenuAction = {},
            onObjectCheckboxClicked = { _, _ -> }
        )
    }
}