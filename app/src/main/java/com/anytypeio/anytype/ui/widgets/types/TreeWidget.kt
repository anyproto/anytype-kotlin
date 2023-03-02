package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.getWidgetObjectName
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@Composable
fun TreeWidgetCard(
    mode: InteractionMode,
    item: WidgetView.Tree,
    onExpandElement: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit
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
                if (mode is InteractionMode.Edit)
                    Modifier.noRippleClickable {
                        isCardMenuExpanded.value = !isCardMenuExpanded.value
                    }
                else
                    Modifier
            )
    ) {
        Column(
            Modifier.padding(
                top = 6.dp,
                bottom = 6.dp,
            )
        ) {
            WidgetHeader(
                title = item.obj.getWidgetObjectName().orEmpty(),
                isCardMenuExpanded = isCardMenuExpanded,
                isHeaderMenuExpanded = isHeaderMenuExpanded,
                onWidgetHeaderClicked = { onWidgetObjectClicked(item.obj) },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                onDropDownMenuAction = onDropDownMenuAction,
                isEditable = mode is InteractionMode.Edit
            )
            if (item.elements.isNotEmpty()) {
                TreeWidgetTreeItems(
                    item = item,
                    mode = mode,
                    onExpand = onExpandElement,
                    onWidgetElementClicked = onWidgetObjectClicked
                )
            } else {
                if (item.isExpanded) {
                    EmptyWidgetPlaceholder(R.string.empty_tree_widget)
                }
            }
        }
        WidgetMenu(
            isExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
private fun TreeWidgetTreeItems(
    mode: InteractionMode,
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit
) {
    item.elements.forEachIndexed { idx, element ->
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .then(
                    if (mode is InteractionMode.Default)
                        Modifier.noRippleClickable { onWidgetElementClicked(element.obj) }
                    else
                        Modifier
                )
        ) {
            if (element.indent > 0) {
                Spacer(
                    Modifier.width(TreeWidgetTreeItemDefaults.Indent.times(element.indent))
                )
            }
            when (val icon = element.elementIcon) {
                is WidgetView.Tree.ElementIcon.Branch -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_expand),
                        contentDescription = "Expand icon",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .rotate(
                                if (icon.isExpanded)
                                    ArrowIconDefaults.Expanded
                                else
                                    ArrowIconDefaults.Collapsed
                            )
                            .then(
                                if (mode is InteractionMode.Default)
                                    Modifier.noRippleClickable { onExpand(element.path) }
                                else
                                    Modifier
                            )
                    )
                }
                is WidgetView.Tree.ElementIcon.Leaf -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_dot),
                        contentDescription = "Dot icon",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                is WidgetView.Tree.ElementIcon.Set -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_set),
                        contentDescription = "Set icon",
                        modifier = Modifier.align(Alignment.CenterVertically)

                    )
                }
            }
            if (element.objectIcon != ObjectIcon.None && element.objectIcon !is ObjectIcon.Basic.Avatar) {
                TreeWidgetObjectIcon(
                    element = element,
                    paddingStart = 8.dp,
                    paddingEnd = 4.dp
                )
            }
            Text(
                text = element.obj.getWidgetObjectName() ?: stringResource(id = R.string.untitled),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_primary),
                )
            )
        }
        Divider(
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = colorResource(id = R.color.shape_primary)
        )
        if (idx == item.elements.lastIndex) {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetHeader(
    title: String,
    isCardMenuExpanded: MutableState<Boolean>,
    isHeaderMenuExpanded: MutableState<Boolean>,
    onWidgetHeaderClicked: () -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onExpandElement: () -> Unit = {},
    isExpanded: Boolean = false,
    isEditable: Boolean = true
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
    )
    {
        Text(
            text = title.ifEmpty { stringResource(id = R.string.untitled) },
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary),
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = if (isEditable) 76.dp else 32.dp
                )
                .align(Alignment.CenterStart)
                .then(
                    if (isEditable)
                        Modifier
                    else
                        Modifier.combinedClickable(
                            onClick = onWidgetHeaderClicked,
                            onLongClick = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )
        )

        val rotation = getAnimatableRotation(isExpanded)

        Image(
            painterResource(R.drawable.ic_widget_tree_expand),
            contentDescription = "Expand icon",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .rotate(rotation.value)
                .then(
                    if (isEditable)
                        Modifier
                    else
                        Modifier.noRippleClickable { onExpandElement() }
                )
        )
        AnimatedVisibility(
            visible = isEditable,
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
                    onDropDownMenuAction = onDropDownMenuAction
                )
            }
        }
    }
}

@Composable
fun getAnimatableRotation(isExpanded: Boolean): Animatable<Float, AnimationVector1D> {
    val currentRotation = remember {
        mutableStateOf(
            if (isExpanded) ArrowIconDefaults.Expanded else ArrowIconDefaults.Collapsed
        )
    }
    val rotation = remember { Animatable(currentRotation.value) }
    LaunchedEffect(isExpanded) {
        rotation.animateTo(
            targetValue = if (isExpanded)
                ArrowIconDefaults.Expanded
            else
                ArrowIconDefaults.Collapsed,
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ) {
            currentRotation.value = value
        }
    }
    return rotation
}


@Immutable
private object TreeWidgetTreeItemDefaults {
    val Indent = 20.dp
}

@Immutable
private object ArrowIconDefaults {
    const val Collapsed = 0f
    const val Expanded = 90f
}