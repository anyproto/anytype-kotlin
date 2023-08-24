package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.DoneMode
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.EditMode
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.Delete
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.Edit
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.Position
import com.anytypeio.anytype.presentation.sets.DVViewsWidgetUiState.Clicks.Dismiss
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DataViewViewsWidget(
    scope: CoroutineScope,
    state: DVViewsWidgetUiState,
    click: (DVViewsWidgetUiState.Clicks) -> Unit
) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart)
    {

        val currentState by rememberUpdatedState(state)
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
                    .noRippleThrottledClickable { click.invoke(Dismiss) }
            )
        }

        if (swipeableState.isAnimationRunning) {
            DisposableEffect(Unit) {
                onDispose {
                    click(Dismiss)
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
                    .shadow(
                        elevation = 40.dp,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x40000000)
                    )
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
                        .padding(bottom = 16.dp)
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
                                    click = { click(DoneMode) }
                                )
                            } else {
                                ActionText(
                                    text = stringResource(id = R.string.edit),
                                    click = { click(EditMode) }
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

                    ViewsList(state = currentState, click = click)
//                    if (currentState.isMoreMenuVisible && itemsScroll.isScrollInProgress) {
//                        onDismiss()
//                    }
                }
            }
        }
    }
}

@Composable
private fun ViewsList(
    state: DVViewsWidgetUiState,
    click: (DVViewsWidgetUiState.Clicks) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        itemsIndexed(state.items) { index, view ->
            ViewsItem(state = state, view = view, click = click)
            if (index != state.items.size - 1) {
                Divider()
            }
        }
    }
}

@Composable
private fun ViewsItem(
    state: DVViewsWidgetUiState,
    view: ViewerView,
    click: (DVViewsWidgetUiState.Clicks) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
    ) {
        val (delete, text, edit, dnd, unsupported) = createRefs()
        Image(
            modifier = Modifier
                .noRippleThrottledClickable {
                    click.invoke(Delete(view.id))
                }
                .constrainAs(delete) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    visibility =
                        if (state.isEditing && !view.isActive) Visibility.Visible else Visibility.Gone
                },
            painter = painterResource(id = R.drawable.ic_relation_delete),
            contentDescription = "Delete view"
        )
        Image(
            modifier = Modifier.constrainAs(dnd) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                visibility = if (state.isEditing) Visibility.Visible else Visibility.Gone

            },
            painter = painterResource(id = R.drawable.ic_dnd),
            contentDescription = "Dnd view"
        )
        Image(
            modifier = Modifier
                .noRippleThrottledClickable {
                    click.invoke(Edit(view.id))
                }
                .constrainAs(edit) {
                    end.linkTo(dnd.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    visibility = if (state.isEditing) Visibility.Visible else Visibility.Gone
                },
            painter = painterResource(id = R.drawable.ic_edit_24),
            contentDescription = "Edit view"
        )
        Text(
            modifier = Modifier
                .constrainAs(unsupported) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(edit.start)
                    visibility = if (!state.isEditing && view.isUnsupported) Visibility.Visible else Visibility.Gone
                },
            text = stringResource(id = R.string.unsupported),
            color = colorResource(id = R.color.text_secondary),
            style = Caption2Regular,
            textAlign = TextAlign.Left
        )
        Text(
            modifier = Modifier
                .noRippleThrottledClickable {
                    //click(Position(view.id)
                }
                .constrainAs(text) {
                    start.linkTo(delete.end, margin = 12.dp, goneMargin = 0.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(unsupported.start, margin = 8.dp, goneMargin = 38.dp)
                    width = Dimension.fillToConstraints
                },
            text = view.name,
            color = colorResource(id = if (view.isActive) R.color.text_primary else R.color.glyph_active),
            style = HeadlineSubheading,
            textAlign = TextAlign.Left,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionText(text: String, click: () -> Unit) {
    Text(
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
                end = 16.dp
            )
            .noRippleThrottledClickable { click() },
        text = text,
        style = BodyCalloutRegular,
        color = colorResource(id = R.color.glyph_active),
        textAlign = TextAlign.Center
    )
}