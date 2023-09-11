package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.TitleInter15
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.Action.Dismiss
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

        var currentClickedMoreButtonCoordinates: IntOffset by remember {
            mutableStateOf(IntOffset(0, 0))
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
                    LayoutIcons()
                }
            }
        }
    }
}

@Composable
fun LayoutIcons() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x0D000000),
                        ambientColor = Color(0x0D000000)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFD15B),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .weight(1f)
            )
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x0D000000),
                        ambientColor = Color(0x0D000000)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFD15B),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .weight(1f)
            )
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .shadow(
                        elevation = 4.dp,
                        spotColor = Color(0x0D000000),
                        ambientColor = Color(0x0D000000)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFD15B),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .weight(1f)
            )
        }
    }
}

@Preview
@Composable
fun PreviewLayoutScreen() {
    ViewerLayoutWidget(
        uiState = ViewerLayoutWidgetUi(
            showWidget = true,
            layoutType = DVViewerType.GRID,
            withIcon = ViewerLayoutWidgetUi.State.Toggle.HideIcon(
                toggled = false
            ),
            fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(
                toggled = false
            ),
            cardSize = ViewerLayoutWidgetUi.State.CardSize.Small,
            cover = ViewerLayoutWidgetUi.State.ImagePreview.Cover
        ),
        action = {},
        scope = CoroutineScope(
            Dispatchers.Main
        )
    )
}

