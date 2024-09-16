package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewerLayoutWidgetUi.State.ImagePreview
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ViewerLayoutCoverWidget(
    uiState: ViewerLayoutWidgetUi,
    action: (ViewerLayoutWidgetUi.Action) -> Unit,
    scope: CoroutineScope
) {

    val swipeableState = rememberSwipeableState(DragStates.VISIBLE)
    val sizePx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    val lazyListState = rememberLazyListState()

    if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
        DisposableEffect(Unit) {
            onDispose {
                action(ViewerLayoutWidgetUi.Action.DismissCoverMenu)
            }
        }
    }

    if (!uiState.showCoverMenu) {
        DisposableEffect(Unit) {
            onDispose {
                scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
            }
        }
    }

    AnimatedVisibility(
        visible = uiState.showCoverMenu,
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
        val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = shape
                )
                .clip(shape)
        ) {
            WidgetHeader(title = stringResource(R.string.view_layout_cover_widget_title))
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, bottom = 250.dp)
            ) {
                items(
                    count = uiState.imagePreviewItems.size,
                    key = { index -> uiState.imagePreviewItems[index].relationKey.key }
                ) { idx ->
                    val item = uiState.imagePreviewItems[idx]
                    val title = item.getTitle()
                    val iconDrawableRes = when (item) {
                        is ImagePreview.None -> null
                        is ImagePreview.PageCover -> null
                        is ImagePreview.Custom -> R.drawable.ic_relation_attachment_24
                    }
                    CoverItem(
                        text = title,
                        checked = item.isChecked,
                        iconDrawableRes = iconDrawableRes
                    ) {
                        action(ViewerLayoutWidgetUi.Action.ImagePreviewUpdate(item))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverItem(
    text: String,
    checked: Boolean,
    @DrawableRes iconDrawableRes: Int? = null,
    action: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .height(58.dp)
            .noRippleThrottledClickable { action() }
    ) {
        if (iconDrawableRes != null) {
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterStart),
                painter = painterResource(id = iconDrawableRes),
                contentDescription = "File relation icon"
            )
            Text(
                modifier = Modifier
                    .padding(start = 34.dp)
                    .align(Alignment.CenterStart),
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterStart),
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        }
        if (checked) {
            Image(
                modifier = Modifier.align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_option_checked_black),
                contentDescription = "Checked"
            )
        }
    }
    Divider()
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewLayoutCoverWidget() {
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
            showCardSize = true,
            viewer = "",
            showCoverMenu = true,
            imagePreviewItems = listOf(
                ImagePreview.None(isChecked = false),
                ImagePreview.PageCover(isChecked = true),
                ImagePreview.Custom(
                    relationKey = RelationKey(Relations.IDENTITY),
                    isChecked = false,
                    name = "Some File Relation"
                )
            )
        ),
        action = {},
        scope = CoroutineScope(
            Dispatchers.Main
        )
    )
}