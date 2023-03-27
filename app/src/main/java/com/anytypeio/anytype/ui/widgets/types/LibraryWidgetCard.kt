package com.anytypeio.anytype.ui.widgets.types

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryWidgetCard(
    mode: InteractionMode,
    onClick: () -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
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
                    Modifier
            )
    ) {
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .height(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_widget_library),
                contentDescription = "Bin icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp)
            )
            Text(
                text = stringResource(id = R.string.library),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 42.dp, end = 16.dp),
                style = TextStyle(
                    fontSize = 17.sp,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
            )
        }
        WidgetMenu(
            isExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction,
            canRemove = false,
            canChangeType = false,
            canChangeSource = false,
            canEmptyBin = false,
            canEditWidgets = mode is InteractionMode.Default,
            canAddBelow = false
        )
    }
}