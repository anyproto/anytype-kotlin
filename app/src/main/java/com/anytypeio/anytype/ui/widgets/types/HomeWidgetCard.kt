package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.WidgetView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeWidgetCard(
    item: WidgetView.Home,
    onWidgetClicked: () -> Unit,
    onChangeHomeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .fillMaxWidth()
            .height(52.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = onWidgetClicked,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isMenuExpanded.value = true
                }
            )
            .alpha(if (isMenuExpanded.value) 0.8f else 1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier.padding(start = 16.dp),
            iconSize = 24.dp,
            icon = item.icon
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = when (val n = item.name) {
                is WidgetView.Name.Default -> n.prettyPrintName
                is WidgetView.Name.Bundled -> stringResource(id = R.string.untitled)
                WidgetView.Name.Empty -> stringResource(id = R.string.untitled)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = BodySemiBold,
            color = colorResource(id = R.color.text_primary)
        )
        Icon(
            painter = painterResource(id = R.drawable.ci_home),
            contentDescription = null,
            tint = colorResource(id = R.color.glyph_active),
            modifier = Modifier
                .padding(end = 16.dp)
                .size(20.dp)
        )
        HomeWidgetMenu(
            isMenuExpanded = isMenuExpanded,
            onAction = { action ->
                when (action) {
                    DropDownMenuAction.ChangeHome -> onChangeHomeClicked()
                    else -> Unit
                }
            }
        )
    }
}

@Composable
private fun HomeWidgetMenu(
    isMenuExpanded: MutableState<Boolean>,
    onAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = isMenuExpanded.value,
        onDismissRequest = { isMenuExpanded.value = false },
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(x = 16.dp, y = 8.dp)
    ) {
        DropdownMenuItem(
            onClick = {
                onAction(DropDownMenuAction.ChangeHome)
                isMenuExpanded.value = false
            },
            text = {
                Text(
                    text = stringResource(id = R.string.widget_menu_change_home),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary)
                )
            }
        )
    }
}
