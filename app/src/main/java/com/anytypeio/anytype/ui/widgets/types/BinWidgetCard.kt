package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BinWidgetCard(
    mode: InteractionMode,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onClick: () -> Unit,
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    val isHeaderMenuExpanded = remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (isCardMenuExpanded.value || isHeaderMenuExpanded.value) 0.8f else 1f)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .then(
                if (mode is InteractionMode.Default)
                    Modifier.combinedClickable(
                        onClick = {
                            onClick()
                        },
                        onLongClick = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                else
                    Modifier.noRippleClickable {
                        isCardMenuExpanded.value = !isCardMenuExpanded.value
                    }
            )
    ) {
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .height(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_widget_bin),
                contentDescription = "Bin icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp)
            )
            Text(
                text = stringResource(id = R.string.bin),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 42.dp, end = 16.dp),
                style = HeadlineSubheading
            )
        }
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 48.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box {
                Image(
                    painterResource(R.drawable.ic_widget_three_dots),
                    contentDescription = "Widget menu icon",
                    modifier = Modifier
                        .noRippleClickable {
                            isHeaderMenuExpanded.value = !isHeaderMenuExpanded.value
                        }
                )
                WidgetMenu(
                    isExpanded = isHeaderMenuExpanded,
                    onDropDownMenuAction = onDropDownMenuAction,
                    canRemove = false,
                    canChangeType = false,
                    canChangeSource = false,
                    canEmptyBin = true,
                    canEditWidgets = mode is InteractionMode.Default,
                    canAddBelow = false
                )
            }
        }
        WidgetMenu(
            isExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction,
            canRemove = false,
            canChangeType = false,
            canChangeSource = false,
            canEmptyBin = true,
            canEditWidgets = mode is InteractionMode.Default,
            canAddBelow = false
        )
    }
}