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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption2Medium
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.FitImage
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Icon
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

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
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

        val sizePx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        var currentCoordinates: androidx.compose.ui.geometry.Rect by remember {
            mutableStateOf(androidx.compose.ui.geometry.Rect.Zero)
        }

        var currentCoverItem by remember {
            mutableStateOf(uiState.getActiveImagePreviewItem())
        }

        LaunchedEffect(key1 = uiState) {
            currentCoverItem = uiState.getActiveImagePreviewItem()
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
                        .padding(bottom = 20.dp)
                ) {
                    WidgetHeader(title = stringResource(R.string.view_layout_widget_title))
                    Spacer(modifier = Modifier.height(12.dp))
                    LayoutIcons(uiState = currentState, action = action)
                    Spacer(modifier = Modifier.height(8.dp))
                    LayoutSwitcherItem(
                        text = stringResource(id = R.string.icon),
                        checked = currentState.withIcon.toggled,
                        onCheckedChanged = { action(Icon(it)) }
                    )
                    val isGallery = currentState.layoutType == DVViewerType.GALLERY
                    Divider(visible = isGallery)
                    ColumnItem(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp)
                            .alpha(if (isGallery) 1f else 0f),
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
                    Divider(visible = isGallery)
                    ColumnItem(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp)
                            .alpha(if (isGallery) 1f else 0f),
                        title = stringResource(id = R.string.cover),
                        value = currentCoverItem.getTitle(),
                        onClick = {
                            action(ViewerLayoutWidgetUi.Action.CoverMenu)
                        },
                        arrow = painterResource(id = R.drawable.ic_arrow_disclosure_18)
                    )
                    Divider(visible = isGallery)
                    LayoutSwitcherItem(
                        modifier = Modifier.alpha(if (isGallery) 1f else 0f),
                        text = stringResource(id = R.string.fit_image),
                        checked = currentState.fitImage.toggled,
                        onCheckedChanged = { action(FitImage(it)) }
                    )
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

private fun ViewerLayoutWidgetUi.getActiveImagePreviewItem() =
    imagePreviewItems.firstOrNull { it.isChecked }
        ?: ViewerLayoutWidgetUi.State.ImagePreview.None(isChecked = true)

@Composable
fun ViewerLayoutWidgetUi.State.ImagePreview.getTitle(): String {
    return when (this) {
        is ViewerLayoutWidgetUi.State.ImagePreview.None -> stringResource(id = R.string.none)
        is ViewerLayoutWidgetUi.State.ImagePreview.PageCover -> stringResource(id = R.string.page_cover)
        is ViewerLayoutWidgetUi.State.ImagePreview.Custom -> name
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
    val (borderColor, borderWidth) = if (uiState.layoutType == layoutType) {
        Pair(
            colorResource(id = R.color.palette_system_amber_50),
            2.dp
        )
    } else {
        Pair(
            colorResource(id = R.color.shape_primary),
            0.5.dp
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x0D000000),
                ambientColor = Color(0x0D000000)
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(top = 14.dp, bottom = 14.dp)
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
        val (textColor, textStyle) = if (uiState.layoutType == layoutType) {
            Pair(colorResource(id = R.color.amber_100), Caption2Medium)
        } else {
            Pair(colorResource(id = R.color.text_secondary), Caption2Regular)
        }
        Text(
            text = contentDescription,
            style = textStyle,
            color = textColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LayoutIcons(uiState: ViewerLayoutWidgetUi, action: (ViewerLayoutWidgetUi.Action) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        maxItemsInEachRow = 3
    ) {
        val itemModifier = Modifier.weight(1f)
        LayoutIcon(
            modifier = itemModifier,
            uiState = uiState,
            layoutType = DVViewerType.GRID,
            imageResource = R.drawable.ic_layout_grid,
            imageResourceSelected = R.drawable.ic_layout_grid_selected,
            contentDescription = "Grid",
            click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.GRID)) }
        )
        LayoutIcon(
            modifier = itemModifier,
            uiState = uiState,
            layoutType = DVViewerType.GALLERY,
            imageResourceSelected = R.drawable.ic_layout_gallery_selected,
            imageResource = R.drawable.ic_layout_gallery,
            contentDescription = "Gallery",
            click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.GALLERY)) }
        )
        LayoutIcon(
            modifier = itemModifier,
            uiState = uiState,
            layoutType = DVViewerType.LIST,
            imageResourceSelected = R.drawable.ic_layout_list_selected,
            imageResource = R.drawable.ic_layout_list,
            contentDescription = "List",
            click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.LIST)) }
        )
        LayoutIcon(
            modifier = itemModifier.padding(top = 8.dp),
            uiState = uiState,
            layoutType = DVViewerType.BOARD,
            imageResourceSelected = R.drawable.ic_layout_kanban_selected,
            imageResource = R.drawable.ic_layout_kanban,
            contentDescription = "Kanban",
            click = { action(ViewerLayoutWidgetUi.Action.Type(DVViewerType.BOARD)) }
        )
        LayoutIcon(
            modifier = itemModifier
                .padding(top = 8.dp)
                .alpha(0F),
            uiState = uiState,
            layoutType = DVViewerType.BOARD,
            imageResourceSelected = R.drawable.ic_layout_kanban_selected,
            imageResource = R.drawable.ic_layout_kanban,
            contentDescription = "",
            click = { }
        )
        LayoutIcon(
            modifier = itemModifier
                .padding(top = 8.dp)
                .alpha(0F),
            uiState = uiState,
            layoutType = DVViewerType.BOARD,
            imageResourceSelected = R.drawable.ic_layout_kanban_selected,
            imageResource = R.drawable.ic_layout_kanban,
            contentDescription = "",
            click = { }
        )
    }
}

@Composable
fun LayoutSwitcherItem(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    var switchCheckedState by remember(checked) { mutableStateOf(checked) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = text,
            style = UXBody,
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
                checkedTrackAlpha = 1f,
                uncheckedThumbColor = colorResource(id = R.color.white),
                uncheckedTrackColor = colorResource(id = R.color.shape_secondary)
            )
        )
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true, device = Devices.NEXUS_7)
@Composable
fun PreviewLayoutScreen() {
    ViewerLayoutWidget(
        uiState = ViewerLayoutWidgetUi(
            showWidget = true,
            layoutType = DVViewerType.GRID,
            withIcon = ViewerLayoutWidgetUi.State.Toggle.WithIcon(
                toggled = true
            ),
            fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(
                toggled = false
            ),
            cardSize = ViewerLayoutWidgetUi.State.CardSize.Small,
            showCardSize = false,
            viewer = "",
            showCoverMenu = false,
            imagePreviewItems = emptyList()
        ),
        action = {},
        scope = CoroutineScope(
            Dispatchers.Main
        )
    )
}

