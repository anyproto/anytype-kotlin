package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueItem
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueItemAction

@Composable
fun ObjectItem(
    item: ObjectValueItem.Object,
    action: (ObjectValueItemAction) -> Unit,
    isEditable: Boolean
) {
    val haptics = LocalHapticFeedback.current
    val isMenuExpanded = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .height(72.dp)
            .padding(horizontal = 16.dp)
            .noRippleCombinedClickable(
                onClick = { action(ObjectValueItemAction.Click(item)) },
                onLongClicked = {
                    if (isEditable) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isMenuExpanded.value = !isMenuExpanded.value
                    }
                }
            )
    ) {
        ObjectIconText(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp)
                .align(alignment = Alignment.CenterStart)
        )
        CircleIcon(
            number = if (item.isSelected) item.number.toString() else null,
            isSelected = item.isSelected,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        )
        Divider(
            modifier = Modifier.align(Alignment.BottomCenter),
            paddingStart = 0.dp,
            paddingEnd = 0.dp
        )
        val isFileLayout = SupportedLayouts.fileLayouts.contains(item.view.layout)
        ItemMenu(
            action = {
                when (it) {
                    ItemMenuAction.Delete -> action(ObjectValueItemAction.Delete(item))
                    ItemMenuAction.Duplicate -> action(ObjectValueItemAction.Duplicate(item))
                    ItemMenuAction.Open -> action(ObjectValueItemAction.Open(item))
                    ItemMenuAction.Edit -> {}
                }
            },
            isMenuExpanded = isMenuExpanded,
            showOpen = true,
            showDuplicate = !isFileLayout
        )
    }
}

@Composable
fun ObjectIconText(modifier: Modifier, item: ObjectValueItem.Object) {
    Row(
        modifier = modifier
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(42.dp),
            icon = item.view.icon,
            onTaskIconClicked = { }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {

            val name = item.view.name.trim().ifBlank { stringResource(R.string.untitled) }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = name,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val typeName = item.view.typeName
            if (!typeName.isNullOrBlank()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
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
fun ObjectTypeItem(
    item: ObjectValueItem.ObjectType
) {
    Box {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, bottom = 8.dp, top = 26.dp, end = 20.dp),
        ) {
            Text(
                modifier = Modifier
                    .wrapContentWidth(),
                text = "${stringResource(id = R.string.object_type)}: ",
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
            Text(
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Bottom)
                    .fillMaxWidth(),
                text = item.name,
                style = Caption1Medium,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        Divider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 0.dp,
                    end = 0.dp
                )
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewObjectItem() {
    ObjectItem(
        item = ObjectValueItem.Object(
            view = DefaultObjectView(
                id = "1",
                name = "Object",
                type = "Type",
                typeName = "Type Name",
                description = "Description",
                icon = ObjectIcon.Basic.Emoji("\uD83D\uDCA1")
            ),
            isSelected = true,
            number = 1,
        ),
        action = {},
        isEditable = true
    )
}