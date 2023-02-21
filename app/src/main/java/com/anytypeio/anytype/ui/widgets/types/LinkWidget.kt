package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkWidgetCard(
    item: WidgetView.Link,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    isEditable: Boolean
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    val isHeaderMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (isCardMenuExpanded.value || isHeaderMenuExpanded.value) 0.8f else 1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .height(40.dp)
                .combinedClickable(
                    onClick = { onWidgetObjectClicked(item.obj) },
                    onLongClick = {
                        isCardMenuExpanded.value = !isCardMenuExpanded.value
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Text(
                text = item.obj.name.orEmpty().trim(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 16.dp),
                style = TextStyle(
                    fontSize = 17.sp,
                    color = colorResource(id = R.color.text_primary),
                    fontWeight = FontWeight.Bold
                )
            )
            AnimatedVisibility(
                visible = isEditable,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 48.dp),
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
                        onDropDownMenuAction = onDropDownMenuAction
                    )
                }
            }
        }
        WidgetMenu(
            isExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}