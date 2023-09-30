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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.core_ui.views.TitleInter15
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Icon
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.FitImage
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ViewerLayoutWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
    scope: CoroutineScope
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {
        val currentState by rememberUpdatedState(uiState)
        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)

        AnimatedVisibility(
            visible = currentState.showWidget,
            enter = fadeIn(),
            exit = fadeOut(
                tween(200)
            )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { action(Dismiss) }
            )
        }

        if (swipeableState.isAnimationRunning) {
            DisposableEffect(Unit) {
                onDispose {
                    action(Dismiss)
                }
            }
        }

        if (!currentState.showWidget) {
            DisposableEffect(Unit) {
                onDispose {
                    scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
                }
            }
        }

        val sizePx = with(LocalDensity.current) { 312.dp.toPx() }

        var currentCoordinates: androidx.compose.ui.geometry.Rect by remember {
            mutableStateOf(androidx.compose.ui.geometry.Rect.Zero)
        }

        AnimatedVisibility(
            visible = currentState.showWidget,
            enter = slideInVertically { it },
            exit = slideOutVertically(tween(200)) { it },
            modifier = Modifier
                .swipeable(state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = colorResource(id = R.color.background_secondary),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 20.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.view_layout_widget_title),
                                style = TitleInter15,
                                color = colorResource(R.color.text_primary)
                            )
                        }
                    }
                    LayoutIcons(uiState = currentState, action = action)
                    Spacer(modifier = Modifier.height(8.dp))
                    LayoutSwitcherItem(
                        text = stringResource(id = R.string.icon),
                        checked = currentState.withIcon.toggled,
                        onCheckedChanged = { action(Icon(it)) }
                    )
                    if (currentState.layoutType == DVViewerType.GALLERY) {
                        Divider()
                        ColumnItem(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp),
                            title = stringResource(id = R.string.card_size),
                            value = when (currentState.cardSize) {
                                ViewerLayoutWidgetUi.State.CardSize.Large -> stringResource(id = R.string.large)
                                ViewerLayoutWidgetUi.State.CardSize.Small -> stringResource(id = R.string.small)
                            },
                            onClick = {
                                action(ViewerLayoutWidgetUi.Action.CardSizeMenu)
                            },
                            arrow = painterResource(id = R.drawable.ic_list_arrow_18),
                            imageModifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    if (coordinates.isAttached) {
                                        with(coordinates.boundsInRoot()) {
                                            currentCoordinates = this
                                        }
                                    } else {
                                        currentCoordinates = androidx.compose.ui.geometry.Rect.Zero
                                    }
                                }
                        )
                        Divider()
                        ColumnItem(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                            title = stringResource(id = R.string.cover),
                            value = when (val cover = currentState.cover) {
                                ViewerLayoutWidgetUi.State.ImagePreview.Cover -> stringResource(id = R.string.cover)
                                is ViewerLayoutWidgetUi.State.ImagePreview.Custom -> cover.name
                                ViewerLayoutWidgetUi.State.ImagePreview.None -> stringResource(id = R.string.none)
                            },
                            onClick = {
                                action(ViewerLayoutWidgetUi.Action.CoverMenu)
                            },
                            arrow = painterResource(id = R.drawable.ic_arrow_disclosure_18)
                        )
                        Divider()
                        LayoutSwitcherItem(
                            text = stringResource(id = R.string.fit_image),
                            checked = currentState.fitImage.toggled,
                            onCheckedChanged = { action(FitImage(it)) }
                        )
                    }
                }
            }
        }
        ViewerLayoutListMenu(
            show = currentState.showCardSize,
            action = action,
            coordinates = currentCoordinates
        )
        ViewerLayoutCoverWidget(
            uiState = uiState,
            action = action,
            scope = scope
        )
    }
}

@Composable
fun LayoutIcon(
    modifier: Modifier = Modifier,
    uiState: ViewerLayoutWidgetUi,
    layoutType: DVViewerType,
    imageResource: Int,
    imageResourceSelected: Int,
    contentDescription: String,
    click: () -> Unit
) {
    val (borderColor, textcolor) = if (uiState.layoutType == layoutType) {
        Pair(
            colorResource(id = R.color.palette_system_amber_50),
            colorResource(id = R.color.amber_100)
        )
    } else {
        Pair(colorResource(id = R.color.shape_primary), colorResource(id = R.color.text_secondary))
    }
    Column(
        modifier = modifier
            .wrapContentSize()
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(top = 14.dp, start = 26.25.dp, end = 26.25.dp, bottom = 14.dp)
            .noRippleThrottledClickable { click() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = if (uiState.layoutType == layoutType) {
                painterResource(id = imageResourceSelected)
            } else {
                painterResource(id = imageResource)
            },
            contentDescription = contentDescription
        )
        Text(
            text = contentDescription,
            style = Caption2Medium,
            color = textcolor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LayoutIcons(uiState: ViewerLayoutWidgetUi, action: (ViewerLayoutWidgetUi.Action) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LayoutIcon(
                uiState = uiState,
                layoutType = DVViewerType.GRID,
                imageResource = R.drawable.ic_layout_grid,
                imageResourceSelected = R.drawable.ic_layout_grid_selected,
                contentDescription = "Grid",
                click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.GRID)) }
            )
            LayoutIcon(
                uiState = uiState,
                layoutType = DVViewerType.GALLERY,
                imageResourceSelected = R.drawable.ic_layout_gallery_selected,
                imageResource = R.drawable.ic_layout_gallery,
                contentDescription = "Gallery",
                click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.GALLERY)) }
            )
            LayoutIcon(
                uiState = uiState,
                layoutType = DVViewerType.LIST,
                imageResourceSelected = R.drawable.ic_layout_list_selected,
                imageResource = R.drawable.ic_layout_list,
                contentDescription = "List",
                click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.LIST)) }
            )
            LayoutIcon(
                modifier = Modifier.padding(top = 8.dp),
                uiState = uiState,
                layoutType = DVViewerType.BOARD,
                imageResourceSelected = R.drawable.ic_layout_kanban_selected,
                imageResource = R.drawable.ic_layout_kanban,
                contentDescription = "Kanban",
                click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.BOARD)) }
            )
        }
    }
}

@Composable
fun LayoutSwitcherItem(
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    var switchCheckedState by remember(checked) { mutableStateOf(checked) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = text,
            style = BodyCallout,
            color = colorResource(id = R.color.text_primary)
        )
        Switch(
            modifier = Modifier.align(Alignment.CenterEnd),
            checked = switchCheckedState,
            onCheckedChange = {
                switchCheckedState = it
                onCheckedChanged(switchCheckedState)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.white),
                checkedTrackColor = colorResource(id = R.color.palette_system_amber_50),
                uncheckedThumbColor = colorResource(id = R.color.white),
                uncheckedTrackColor = colorResource(id = R.color.shape_secondary)
            )
        )
    }
}

@Preview
@Composable
fun PreviewLayoutScreen() {
    ViewerLayoutWidget(
        uiState = ViewerLayoutWidgetUi(
            showWidget = true,
            layoutType = DVViewerType.GRID,
            withIcon = ViewerLayoutWidgetUi.State.Toggle.WithIcon(
                toggled = false
            ),
            fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(
                toggled = false
            ),
            cardSize = ViewerLayoutWidgetUi.State.CardSize.Small,
            cover = ViewerLayoutWidgetUi.State.ImagePreview.Cover,
            showCardSize = true,
            viewer = "",
            showCoverMenu = true
        ),
        action = {},
        scope = CoroutineScope(
            Dispatchers.Main
        )
    )
}

