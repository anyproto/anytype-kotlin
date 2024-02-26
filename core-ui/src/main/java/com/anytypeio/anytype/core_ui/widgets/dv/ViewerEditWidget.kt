package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.ViewEditAction
import com.anytypeio.anytype.presentation.sets.ViewerEditWidgetUi
import com.anytypeio.anytype.presentation.sets.isVisible
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ViewerEditWidget(
    state: ViewerEditWidgetUi,
    action: (ViewEditAction) -> Unit,
    scope: CoroutineScope
) {

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {

        val currentState by rememberUpdatedState(state)
        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)
        val keyboardController = LocalSoftwareKeyboardController.current

        AnimatedVisibility(
            visible = currentState.isVisible(),
            enter = fadeIn(),
            exit = fadeOut(tween(100))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { action(ViewEditAction.Dismiss) }
            )
        }

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
            DisposableEffect(Unit) {
                onDispose {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    action(ViewEditAction.Dismiss)
                }
            }
        }

        if (!currentState.isVisible()) {
            DisposableEffect(Unit) {
                onDispose {
                    scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
                }
            }
        }

        val sizePx =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        AnimatedVisibility(
            visible = currentState.isVisible(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .swipeable(state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            if (state is ViewerEditWidgetUi.Data) {
                ViewerEditWidgetContent(state, focusRequester, keyboardController) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    action.invoke(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ViewerEditWidgetContent(
    state: ViewerEditWidgetUi.Data,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    action: (ViewEditAction) -> Unit,
) {

    val currentState by rememberUpdatedState(state)

    var currentCoordinates: Rect by remember { mutableStateOf(Rect.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 8.dp, end = 8.dp, bottom = 15.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(size = 16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp)
            ) {
                Dragger(modifier = Modifier.align(Alignment.Center))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    val text = if (currentState.isNewMode) {
                        stringResource(R.string.new_view)
                    } else {
                        stringResource(R.string.edit_view)
                    }
                    Text(
                        text = text,
                        style = Title1,
                        color = colorResource(R.color.text_primary)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .onGloballyPositioned { coordinates ->
                            if (coordinates.isAttached) {
                                with(coordinates.boundsInRoot()) {
                                    currentCoordinates = this
                                }
                            } else {
                                currentCoordinates = Rect.Zero
                            }
                        }
                        .noRippleThrottledClickable {
                            action.invoke(ViewEditAction.More)
                        },
                    ) {
                    Image(
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 12.dp
                        ),
                        painter = painterResource(id = R.drawable.ic_style_toolbar_more),
                        contentDescription = null
                    )
                }
            }
            NameTextField(
                state = currentState,
                action = action,
                keyboardController = keyboardController,
                focusRequester = focusRequester
            )
            Spacer(modifier = Modifier.height(12.dp))

            val layoutValue = when (state.layout) {
                DVViewerType.LIST -> stringResource(id = R.string.view_list)
                DVViewerType.GRID -> stringResource(id = R.string.view_grid)
                DVViewerType.GALLERY -> stringResource(id = R.string.view_gallery)
                DVViewerType.BOARD -> stringResource(id = R.string.view_kanban)
                else -> stringResource(id = R.string.none)
            }
            ColumnItem(
                title = stringResource(id = R.string.layout),
                value = layoutValue
            ) { action(ViewEditAction.Layout(id = state.id)) }
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val relationsValue = when (state.relations.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.relations[0]
                else -> stringResource(id = R.string.num_applied, state.relations.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.relations),
                value = relationsValue
            ) { action(ViewEditAction.Relations(id = state.id)) }

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val filtersValue = when (state.filters.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.filters[0]
                else -> stringResource(id = R.string.num_applied, state.filters.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.filter),
                value = filtersValue
            ) { action(ViewEditAction.Filters(id = state.id)) }

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val sortsValue = when (state.sorts.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.sorts[0]
                else -> stringResource(id = R.string.num_applied, state.sorts.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.sort),
                value = sortsValue
            ) { action(ViewEditAction.Sorts(id = state.id)) }
        }
        ViewerEditMoreMenu(
            show = currentState.showMore,
            currentState = currentState,
            action = action,
            coordinates = currentCoordinates
        )
    }
}

@Composable
fun NameTextField(
    state: ViewerEditWidgetUi.Data,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    action: (ViewEditAction) -> Unit
) {
    var innerValue by remember(state.name) { mutableStateOf(state.name) }
    val focusManager = LocalFocusManager.current

    if (state.name.isEmpty()) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    val strokeColorActive = colorResource(id = R.color.widget_edit_view_stroke_color_active)
    val strokeColorInactive = colorResource(id = R.color.widget_edit_view_stroke_color_inactive)

    val strokeColor = remember {
        mutableStateOf(strokeColorInactive)
    }

    val strokeWidthActive = 2.dp
    val strokeWidthInactive = 1.dp

    val strokeWidth = remember {
        mutableStateOf(strokeWidthInactive)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                width = strokeWidth.value,
                color = strokeColor.value,
                shape = RoundedCornerShape(size = 10.dp)
            )
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.name),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary)
        )
        BasicTextField(
            value = innerValue,
            onValueChange = { innerValue = it },
            textStyle = Title1.copy(color = colorResource(id = R.color.text_primary)),
            singleLine = true,
            enabled = true,
            cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 0.dp, top = 2.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        strokeColor.value = strokeColorActive
                        strokeWidth.value = strokeWidthActive
                    } else {
                        strokeColor.value = strokeColorInactive
                        strokeWidth.value = strokeWidthInactive
                    }
                },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
                action.invoke(
                    ViewEditAction.UpdateName(
                        id = state.id,
                        name = innerValue
                    )
                )
            },
            decorationBox = { innerTextField ->
                if (innerValue.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.new_view),
                        style = Title1,
                        color = colorResource(id = R.color.text_tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            //.padding(start = 0.dp, top = 2.dp)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun ColumnItem(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    title: String,
    isEnable: Boolean = true,
    value: String,
    arrow : Painter = painterResource(id = R.drawable.ic_arrow_forward),
    onClick: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable(onClick = onClick)
            .alpha(if (isEnable) 1f else 0.2f)
    ) {
        val (titleRef, valueRef, iconRef) = createRefs()
        val rightGuideline = createGuidelineFromStart(0.5f)
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .constrainAs(titleRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            text = title,
            style = UXBody,
            color = colorResource(id = R.color.text_primary),
        )
        Image(
            modifier = imageModifier
                .constrainAs(iconRef) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            painter = arrow,
            contentDescription = "Arrow icon",
        )
        Text(
            modifier = Modifier
                .constrainAs(valueRef) {
                    start.linkTo(rightGuideline)
                    end.linkTo(iconRef.start, margin = 6.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = value,
            style = UXBody,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewViewerEditWidget() {
    val state = ViewerEditWidgetUi.Data(
        showWidget = true,
        showMore = false,
        isNewMode = true,
        name = "Artist",
        filters = listOf(),
        sorts = emptyList(),
        layout = DVViewerType.LIST,
        relations = listOf(),
        id = "1",
        isActive = false
    )
    ViewerEditWidget(state = state, action = {}, scope = CoroutineScope(Dispatchers.Main))
}