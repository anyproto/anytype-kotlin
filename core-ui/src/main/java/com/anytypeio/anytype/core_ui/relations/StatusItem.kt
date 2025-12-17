package com.anytypeio.anytype.core_ui.relations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction

@Composable
fun StatusItem(
    state: RelationsListItem.Item.Status,
    action: (TagStatusAction) -> Unit,
    isEditable: Boolean,
    showDivider: Boolean = true,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val isMenuExpanded = remember { mutableStateOf(false) }
    val alpha = animateFloatAsState(if (isDragging) 0.8f else 1.0f, label = "drag_alpha")
    CommonContainer(
        modifier = Modifier
            .alpha(alpha.value)
            .noRippleCombinedClickable(
                onClick = { action(TagStatusAction.Click(state)) },
                onLongClicked = {
                    if (isEditable && !isDragging) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isMenuExpanded.value = !isMenuExpanded.value
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 56.dp)
                .align(alignment = Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditable) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { /* Consumed - do nothing */ }
                            )
                        }
                ) {
                    Image(
                        modifier = dragHandleModifier.size(24.dp),
                        painter = painterResource(id = R.drawable.ic_drag_handle_dots),
                        contentDescription = "Drag to reorder"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            StatusItemText(state = state)
        }
        CheckedIcon(
            isSelected = state.isSelected,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterEnd)
        )
        if (showDivider) Divider(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = colorResource(R.color.shape_primary)
        )
        if (isEditable) {
            ItemMenu(
                action = {
                    when (it) {
                        ItemMenuAction.Delete -> action(TagStatusAction.Delete(state.optionId))
                        ItemMenuAction.Duplicate -> action(TagStatusAction.Duplicate(state))
                        ItemMenuAction.Edit -> action(TagStatusAction.Edit(state))
                        ItemMenuAction.Open -> {}
                    }
                },
                isMenuExpanded = isMenuExpanded,
                showEdit = true,
                showDuplicate = true,
                showDelete = true
            )
        }
    }
}

@Composable
fun StatusItemText(state: RelationsListItem.Item.Status) {
    Text(
        text = state.name,
        color = dark(state.color),
        modifier = Modifier
            .wrapContentWidth()
            .padding(start = 6.dp, end = 6.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = Relations1
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewStatusItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        StatusItem(
            state = RelationsListItem.Item.Status(
                optionId = "1",
                name = "In development",
                color = ThemeColor.RED,
                isSelected = true
            ),
            action = {},
            isEditable = true
        )
        StatusItem(
            state = RelationsListItem.Item.Status(
                optionId = "2",
                name = "Designer",
                color = ThemeColor.TEAL,
                isSelected = false
            ),
            action = {},
            isEditable = true
        )
    }
}

