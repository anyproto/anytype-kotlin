package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption2Semibold
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.widgets.TemplateView
import com.anytypeio.anytype.presentation.widgets.TemplatesWidgetUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ObjectTypeTemplatesWidget(
    state: TemplatesWidgetUiState,
    onShadowClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = state.showWidget,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { onShadowClick() }
            )
        }
        val swipeableState = rememberSwipeableState(0)
        val sizePx = with(LocalDensity.current) { 312.dp.toPx() }

        AnimatedVisibility(
            visible = state.showWidget,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to 0,
                        sizePx to 1
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) }
                )
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(312.dp)
                    .padding(start = 8.dp, end = 8.dp, bottom = 31.dp)
                    .background(
                        color = colorResource(id = R.color.background_primary),
                        shape = RoundedCornerShape(size = 16.dp)
                    )
                ,
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
                                .align(Alignment.CenterStart)
                        ) {
                            Text(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 12.dp,
                                    bottom = 12.dp,
                                    end = 16.dp
                                ),
                                text = stringResource(id = R.string.edit),
                                style = BodyCalloutRegular
                            )
                        }
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.type_templates_widget_title),
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
                    TemplatesList(state.items)
                }
            }
        }
    }
}

@Composable
private fun TemplatesList(
    items: List<TemplateView>
) {
    LazyRow(
        modifier = Modifier
            .padding(top = 8.dp)
            .height(224.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    )
    {
        itemsIndexed(
            items = items,
            itemContent = { index, item ->
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.shape_primary),
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .height(224.dp)
                        .width(120.dp)
                ) {
                    Column {
                        TemplateItemContent(item)
                    }
                }
            }
        )
    }
}

@Composable
private fun TemplateItemContent(item: TemplateView) {
    when (item) {
        TemplateView.Blank -> {
            Spacer(modifier = Modifier.height(28.dp))
            TemplateItemTitle(text = stringResource(id = R.string.blank))
        }

        is TemplateView.NoIcon -> {
            Spacer(modifier = Modifier.height(28.dp))
            TemplateItemTitle(text = item.title)
            Spacer(modifier = Modifier.height(12.dp))
            TemplateItemRectangles()
        }

        is TemplateView.Cover -> TODO()
        is TemplateView.CoverWithIcon -> TODO()
        is TemplateView.Icon -> TODO()
        is TemplateView.Image -> TODO()
        is TemplateView.Profile -> TODO()
        is TemplateView.Task -> TODO()
    }
}

@Composable
private fun TemplateItemTitle(text: String) {
    Text(
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp
        ),
        text = text,
        style = Caption2Semibold.copy(
            color = colorResource(id = R.color.text_primary)
        ),
    )
}

@Composable
private fun TemplateItemRectangles() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(start = 16.dp, end = 16.dp)
                .background(
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(size = 1.dp)
                )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(start = 16.dp, end = 16.dp)
                .background(
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(size = 1.dp)
                )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(start = 16.dp, end = 40.dp)
                .background(
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(size = 1.dp)
                )
        )
    }
}


//@Preview
//@Composable
//fun ComposablePreview() {
//    val items = listOf(TemplateView.Blank, TemplateView.NoIcon("Title1983"))
//    ObjectTypeTemplatesWidget(items = items, show = true)
//}