package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllContentWidgetCard(
    modifier: Modifier = Modifier,
    index: Int,
    widgetView: WidgetView,
    mode: InteractionMode,
    onWidgetClicked: () -> Unit = {},
    onDropDownMenuAction: (DropDownMenuAction) -> Unit = {},
    alpha: Float,
) {
    val haptic = LocalHapticFeedback.current
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        Box(
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
                            onClick = {
                                onWidgetClicked()
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isCardMenuExpanded.value = true
                            }
                        )
                    } else {
                        Modifier
                    }
                )
                .alpha(alpha)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_widget_all_content),
                contentDescription = "All content icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )

            Text(
                text = stringResource(id = R.string.all_content),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 44.dp, end = 16.dp),
                style = HeadlineSubheading,
                color = colorResource(id = R.color.text_primary),
            )
        }
        WidgetLongClickMenu(
            widgetView = widgetView,
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

