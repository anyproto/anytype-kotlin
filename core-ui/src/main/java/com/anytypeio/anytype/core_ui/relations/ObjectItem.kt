package com.anytypeio.anytype.core_ui.relations

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction

@Composable
fun ObjectItem(
    item: RelationsListItem.Object,
    action: (TagStatusAction) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val isMenuExpanded = remember { mutableStateOf(false) }
    CommonContainer(
        modifier = Modifier
            .noRippleCombinedClickable(
                onClick = { action(TagStatusAction.Click(item)) },
                onLongClicked = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    isMenuExpanded.value = !isMenuExpanded.value
                }
            )
    ) {
        ObjectIconText(item = item,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 56.dp)
                .align(alignment = Alignment.CenterStart)
        )
        CircleIcon(
            number = if (item.isSelected) item.number.toString() else null,
            isSelected = item.isSelected,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        )
        Divider(modifier = Modifier.align(Alignment.BottomCenter))
        ItemMenu(
            item = item,
            action = action,
            isMenuExpanded = isMenuExpanded
        )
    }
}

@Composable
fun ObjectIconText(modifier: Modifier, item: RelationsListItem.Object) {
    Row(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp, 12.dp, 12.dp, 12.dp)
                .size(48.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(icon = item.view.icon)
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(0.dp, 0.dp, 60.dp, 0.dp)
        ) {

            val name = item.view.name.trim().ifBlank { stringResource(R.string.untitled) }

            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val typeName = item.view.typeName
            if (!typeName.isNullOrBlank()) {
                Text(
                    text = typeName,
                    style = Relations3,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun Icon(icon: ObjectIcon?) {
    icon?.let {
        AndroidView(factory = { ctx ->
            val iconWidget = LayoutInflater.from(ctx)
                .inflate(R.layout.collections_icon, null) as ObjectIconWidget
            iconWidget.setIcon(it)
            iconWidget
        })
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewObjectItem() {
    ObjectItem(
        item = RelationsListItem.Object(
            view = DefaultObjectView(
                id = "1",
                name = "Object",
                type = "Type",
                typeName = "Type Name",
                description = "Description",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDCA1")),
            isSelected = true,
            number = 1,
        ),
        action = {}
    )
}