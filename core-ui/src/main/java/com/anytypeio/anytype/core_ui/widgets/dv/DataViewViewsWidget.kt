package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.foundation.layout.Box
import androidx.annotation.ColorRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DataViewViewsWidget(
    scope: CoroutineScope,
    state: DVViewsWidgetUiState,
    onDismiss: () -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart)
    {

        val currentState by rememberUpdatedState(state)
        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)

        if (swipeableState.isAnimationRunning) {
            DisposableEffect(Unit) {
                onDispose {
                    onDismiss()
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

        AnimatedVisibility(
            visible = currentState.showWidget,
            enter = slideInVertically { it },
            exit = slideOutVertically(tween(200)) { it },
            modifier = Modifier
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE,
                        sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) }
                )
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp, bottom = 31.dp)
                    .background(
                        color = colorResource(id = R.color.background_secondary),
                        shape = RoundedCornerShape(size = 16.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart),
                        ) {
                            if (currentState.isEditing) {
                                ActionText(
                                    text = stringResource(id = R.string.done),
                                    click = {}
                                )
                            } else {
                                ActionText(
                                    text = stringResource(id = R.string.edit),
                                    click = {}
                                )
                            }
                        }
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.views),
                                style = Title1,
                                color = colorResource(R.color.text_primary)
                            )
                        }
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            Image(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 12.dp,
                                    bottom = 12.dp,
                                    end = 16.dp
                                ),
                                painter = painterResource(id = R.drawable.ic_default_plus),
                                contentDescription = null
                            )
                        }
                    }
                    val itemsScroll = rememberLazyListState()

//                    if (currentState.isMoreMenuVisible && itemsScroll.isScrollInProgress) {
//                        onDismiss()
//                    }
                }
            }
        }
    }
}

@Composable
private fun ViewsList(scrollState: ScrollState: DVViewsWidgetUiState) {
    LazyColumn(state = state.items) {
    }
}

@Composable
private fun ActionText(text: String, click: () -> Unit) {
    Text(
        modifier = Modifier
            .padding(
                start = 15.dp,
                top = 12.dp,
                bottom = 12.dp,
                end = 16.dp
            )
            .noRippleClickable { click() },
        text = text,
        style = BodyCalloutRegular,
        color = colorResource(id = R.color.glyph_active)
    )
}