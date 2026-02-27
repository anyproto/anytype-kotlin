package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.BinWidgetMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BinWidgetCard(
    item: WidgetView.Bin,
    hasReadOnlyAccess: Boolean = false,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    val isHeaderMenuExpanded = remember {
        mutableStateOf(false)
    }
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (isCardMenuExpanded.value || isHeaderMenuExpanded.value) 0.8f else 1f)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .then(
                if (hasReadOnlyAccess) {
                    Modifier.noRippleClickable {
                        onWidgetSourceClicked(item.id)
                    }
                } else {
                    Modifier.combinedClickable(
                        onClick = { onWidgetSourceClicked(item.id) },
                        onLongClick = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                }
            )
    ) {

        Row (
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_widget_bin),
                contentDescription = "Bin icon",
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(18.dp)
            )

            Text(
                text = stringResource(id = R.string.bin),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = BodySemiBold,
                color = colorResource(id = R.color.text_primary),
            )
        }
        BinWidgetMenu(
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}