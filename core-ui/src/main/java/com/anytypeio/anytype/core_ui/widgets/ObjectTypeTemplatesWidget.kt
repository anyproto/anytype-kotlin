package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption2Semibold
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.widgets.TemplatesWidgetUiState
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ObjectTypeTemplatesWidget(
    state: TemplatesWidgetUiState,
    onDismiss: () -> Unit,
    itemClick: (TemplateView) -> Unit,
    scope: CoroutineScope
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {

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
                    .noRippleClickable { onDismiss() }
            )
        }

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
                    .height(312.dp)
                    .padding(start = 8.dp, end = 8.dp, bottom = 31.dp)
                    .background(
                        color = colorResource(id = R.color.background_primary),
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
//                        Box(
//                            modifier = Modifier
//                                .align(Alignment.CenterStart),
//                        ) {
//                            Text(
//                                modifier = Modifier.padding(
//                                    start = 16.dp,
//                                    top = 12.dp,
//                                    bottom = 12.dp,
//                                    end = 16.dp
//                                ),
//                                text = stringResource(id = R.string.edit),
//                                style = BodyCalloutRegular
//                            )
//                        }
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Text(
                                text = stringResource(R.string.type_templates_widget_title),
                                style = Title1,
                                color = colorResource(R.color.text_primary)
                            )
                        }
//                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
//                            Image(
//                                modifier = Modifier.padding(
//                                    start = 16.dp,
//                                    top = 12.dp,
//                                    bottom = 12.dp,
//                                    end = 16.dp
//                                ),
//                                painter = painterResource(id = R.drawable.ic_default_plus),
//                                contentDescription = null
//                            )
//                        }
                    }
                    TemplatesList(currentState.items) {
                        scope.launch {
                            onDismiss()
                            delay(200L)
                            itemClick(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatesList(
    items: List<TemplateView>,
    itemClick: (TemplateView) -> Unit
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
                        .clickable {
                            itemClick(item)
                        }
                ) {
                    TemplateItemContent(item)
                }
            }
        )
    }
}

@Composable
private fun TemplateItemContent(item: TemplateView) {
    Column {
        when (item) {
            is TemplateView.Blank -> {
                Spacer(modifier = Modifier.height(28.dp))
                TemplateItemTitle(text = stringResource(id = R.string.blank))
            }

            is TemplateView.Template -> {
                if (item.isCoverPresent()) {
                    TemplateItemCoverAndIcon(item = item)
                    if (!item.isImageOrEmojiPresent()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                } else {
                    if (item.layout == ObjectType.Layout.TODO) {
                        Spacer(modifier = Modifier.height(28.dp))
                        TemplateItemTodoTitle(text = item.name)
                    } else {
                        if (item.isImageOrEmojiPresent()) {
                            if (item.layout == ObjectType.Layout.PROFILE) {
                                Box(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(60.dp)
                                        .padding(top = 28.dp)
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    val modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                    TemplateItemIconOrImage(item = item, modifier = modifier)
                                }
                                TemplateItemTitle(text = item.name, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                val modifier = Modifier
                                    .width(48.dp)
                                    .height(60.dp)
                                    .padding(start = 16.dp, top = 28.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                TemplateItemIconOrImage(item = item, modifier = modifier)
                                TemplateItemTitle(text = item.name)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
//
//
//

                }
                Spacer(modifier = Modifier.height(12.dp))
                TemplateItemRectangles()
            }
        }
    }
}

@Composable
private fun TemplateItemIconOrImage(
    item: TemplateView.Template,
    modifier: Modifier = Modifier
) {
    item.image?.let {
//        val isProfile = item.layout == ObjectType.Layout.PROFILE
//        val modifier1 = if (isProfile) {
//            modifier
//                .width(32.dp)
//                .height(32.dp)
//                .padding(0.dp)
//                .clip(CircleShape)
//        } else {
//            modifier.clip(RoundedCornerShape(3.dp))
//        }
        Image(
            painter = rememberAsyncImagePainter(
                model = it,
                error = painterResource(id = R.drawable.ic_home_widget_space)
            ),
            contentDescription = "Custom image template's icon",
            modifier = modifier
                .border(
                    width = 2.dp,
                    color = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(3.dp)
                ),
            contentScale = ContentScale.Crop
        )
    }
    item.emoji?.let {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color = colorResource(id = R.color.shape_tertiary)
                )
                .border(
                    width = 2.dp,
                    color = colorResource(id = R.color.background_primary),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = Emojifier.safeUri(it),
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Emoji template's icon",
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun TemplateItemCoverAndIcon(item: TemplateView.Template) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        TemplateItemCoverColor(item = item)
        TemplateItemCoverImage(item = item)
        TemplateItemCoverGradient(item = item)
        when (item.layout) {
            ObjectType.Layout.TODO -> {}
            ObjectType.Layout.PROFILE -> {
                val modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 50.dp)
                TemplateItemIconOrImage(item = item, modifier = modifier)
            }

            else -> {
                val modifier = Modifier
                    .width(48.dp)
                    .height(82.dp)
                    .padding(start = 16.dp, top = 50.dp)
                TemplateItemIconOrImage(item = item, modifier = modifier)
            }
        }
    }
}

@Composable
private fun TemplateItemCoverColor(item: TemplateView.Template) {
    item.coverColor?.let {
        Box(
            modifier = Modifier
                .height(74.dp)
                .fillMaxWidth()
                .background(
                    color = Color(it.color),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                ),
        )
    }
}

@Composable
private fun TemplateItemCoverImage(item: TemplateView.Template) {
    item.coverImage?.let {
        Image(
            painter = rememberAsyncImagePainter(
                model = it,
                error = painterResource(id = R.drawable.ic_home_widget_space)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentDescription = stringResource(id = R.string.content_description_document_cover),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun TemplateItemCoverGradient(item: TemplateView.Template) {
    item.coverGradient?.let { it ->
        val resourceId = when (it) {
            CoverGradient.YELLOW -> R.drawable.cover_gradient_yellow
            CoverGradient.RED -> R.drawable.cover_gradient_red
            CoverGradient.BLUE -> R.drawable.cover_gradient_blue
            CoverGradient.TEAL -> R.drawable.cover_gradient_teal
            CoverGradient.PINK_ORANGE -> R.drawable.wallpaper_gradient_1
            CoverGradient.BLUE_PINK -> R.drawable.wallpaper_gradient_2
            CoverGradient.GREEN_ORANGE -> R.drawable.wallpaper_gradient_3
            CoverGradient.SKY -> R.drawable.wallpaper_gradient_4
            else -> {
                Timber.e("Unknown cover gradient: $it")
                0
            }
        }
        val drawable = LocalContext.current.getDrawable(resourceId)
        Box(
            modifier = Modifier
                .height(74.dp)
                .fillMaxWidth()
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                )
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        drawable?.let { d ->
                            d.setBounds(
                                0,
                                0,
                                size.width.roundToInt(),
                                size.height.roundToInt()
                            )
                            d.draw(canvas.nativeCanvas)
                        }
                    }
                }
        )
    }
}

@Composable
private fun TemplateItemTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp
        ),
        text = text.ifBlank { stringResource(id = R.string.untitled) },
        style = Caption2Semibold.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        maxLines = 2
    )
}

@Composable
private fun TemplateItemTodoTitle(text: String) {
    Row {
        Image(
            painter = painterResource(id = R.drawable.ic_todo_title_checkbox),
            contentDescription = "Todo icon",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(14.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            modifier = Modifier.padding(
                start = 4.dp,
                end = 16.dp
            ),
            text = text.ifBlank { stringResource(id = R.string.untitled) },
            style = Caption2Semibold.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            maxLines = 1
        )
    }
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

private enum class DragStates {
    VISIBLE,
    DISMISSED
}


@Preview
@Composable
fun ComposablePreview() {
    val items = listOf(
        TemplateView.Blank(
            typeId = "page",
            typeName = "Page",
            layout = ObjectType.Layout.BASIC.code
        ),
        TemplateView.Template(
            id = "1",
            name = "Template 1",
            typeId = "page",
            layout = ObjectType.Layout.BASIC,
            image = null,
            emoji = null,
            coverColor = null,
            coverGradient = null,
            coverImage = null,
        ),
    )
    val state = TemplatesWidgetUiState(items = items, showWidget = true)
    ObjectTypeTemplatesWidget(state = state, onDismiss = {}, itemClick = {}, scope = CoroutineScope(
        Dispatchers.Main
    )
    )
}