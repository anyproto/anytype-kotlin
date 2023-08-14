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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption2Semibold
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.templates.TemplateMenuClick
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.widgets.TemplatesWidgetUiState
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ObjectTypeTemplatesWidget(
    state: TemplatesWidgetUiState,
    onDismiss: () -> Unit,
    itemClick: (TemplateView) -> Unit,
    moreClick: (TemplateView.Template) -> Unit,
    editClick: () -> Unit,
    doneClick: () -> Unit,
    scope: CoroutineScope,
    menuClick: (TemplateMenuClick) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
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

        var currentClickedMoreButtonCoordinates: IntOffset by remember {
            mutableStateOf(IntOffset(0, 0))
        }

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
                                Text(
                                    modifier = Modifier
                                        .padding(
                                            start = 15.dp,
                                            top = 12.dp,
                                            bottom = 12.dp,
                                            end = 16.dp
                                        )
                                        .clickable { doneClick() },
                                    text = stringResource(id = R.string.done),
                                    style = BodyCalloutRegular,
                                    color = colorResource(id = R.color.glyph_active)
                                )
                            } else {
                                Text(
                                    modifier = Modifier
                                        .padding(
                                            start = 15.dp,
                                            top = 12.dp,
                                            bottom = 12.dp,
                                            end = 16.dp
                                        )
                                        .clickable { editClick() },
                                    text = stringResource(id = R.string.edit),
                                    style = BodyCalloutRegular,
                                    color = colorResource(id = R.color.glyph_active)
                                )
                            }
                        }
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
                    val itemsScroll = rememberLazyListState()
                    TemplatesList(
                        state = currentState,
                        moreClick = { template, intOffset ->
                            currentClickedMoreButtonCoordinates = intOffset
                            moreClick(template)
                        },
                        itemClick = {
                            itemClick(it)
                        },
                        scrollState = itemsScroll
                    )
                    if (currentState.isMoreMenuVisible && itemsScroll.isScrollInProgress) {
                        onDismiss()
                    }
                }
            }
        }
        if (currentState.isMoreMenuVisible && currentState.moreMenuTemplate != null) {
            MoreMenu(
                templateView = currentState.moreMenuTemplate!!,
                currentState = currentState,
                currentCoordinates = currentClickedMoreButtonCoordinates,
                menuClick = menuClick
            )
        }
    }
}

@Composable
private fun MoreMenu(
    templateView: TemplateView.Template,
    currentState: TemplatesWidgetUiState,
    currentCoordinates: IntOffset,
    menuClick: (TemplateMenuClick) -> Unit
) {
    val offsetX = if (currentState.isMoreMenuVisible) {
        val moreButtonXCoordinatesDp =
            with(LocalDensity.current) { currentCoordinates.x.toDp() }
        if (moreButtonXCoordinatesDp > 244.dp) {
            moreButtonXCoordinatesDp - 244.dp
        } else {
            0.dp
        }
    } else {
        0.dp
    }
    Column(
        modifier = Modifier
            .size(244.dp, 176.dp)
            .offset(x = offsetX, y = -260.dp)
            .shadow(
                elevation = 40.dp,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(size = 10.dp)
            )
    ) {
        MenuItem(
            click = { menuClick(TemplateMenuClick.Default(templateView)) },
            text = stringResource(id = R.string.templates_menu_default_for_view)
        )
        Divider()
        MenuItem(
            click = { menuClick(TemplateMenuClick.Edit(templateView)) },
            text = stringResource(id = R.string.templates_menu_edit)
        )
        Divider()
        MenuItem(
            click = { menuClick(TemplateMenuClick.Duplicate(templateView)) },
            text = stringResource(id = R.string.templates_menu_duplicate)
        )
        Divider()
        MenuItem(
            click = { menuClick(TemplateMenuClick.Delete(templateView)) },
            text = stringResource(id = R.string.templates_menu_delete)
        )
    }
}

@Composable
private fun MenuItem(click: () -> Unit, text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 11.dp, bottom = 11.dp)
            .clickable { click() },
        text = text,
        style = BodyCalloutRegular,
        color = colorResource(id = R.color.text_primary),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Divider() {
    Spacer(
        modifier = Modifier
            .height(0.5.dp)
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.shape_primary))
    )
}

@Composable
private fun TemplatesList(
    scrollState: LazyListState,
    state: TemplatesWidgetUiState,
    itemClick: (TemplateView) -> Unit,
    moreClick: (TemplateView.Template, IntOffset) -> Unit
) {
    LazyRow(
        state = scrollState,
        modifier = Modifier
            .padding(top = 8.dp)
            .height(224.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    )
    {
        itemsIndexed(
            items = state.items,
            itemContent = { index, item ->
                Box(
                    modifier =
                    Modifier
                        .height(231.dp)
                        .width(127.dp)
                ) {
                    val (borderWidth, borderColor) = if (item.isDefault) {
                        Pair(2.dp, colorResource(id = R.color.palette_system_amber_50))
                    } else {
                        Pair(1.dp, colorResource(id = R.color.shape_primary))
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp, end = 7.dp)
                            .border(
                                width = borderWidth,
                                color = borderColor,
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
                    if (item is TemplateView.Template) {
                        AnimatedVisibility(
                            visible = state.isEditing,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                        ) {
                            var currentCoordinates: IntOffset by remember {
                                mutableStateOf(IntOffset(0, 0))
                            }
                            Image(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(28.dp)
                                    .clickable { moreClick(item, currentCoordinates) }
                                    .onGloballyPositioned { coordinates ->
                                        if (coordinates.isAttached) {
                                            with(coordinates.positionInRoot()) {
                                                currentCoordinates = IntOffset(x.toInt(), y.toInt())
                                            }
                                        } else {
                                            currentCoordinates = IntOffset(0, 0)
                                        }
                                    },
                                painter = painterResource(id = R.drawable.ic_edit_temlate),
                                contentDescription = "Edit template button"
                            )
                        }
                    }
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
                    TemplateItemCoverAndIcon(item)
                    if (item.layout == ObjectType.Layout.TODO) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TemplateItemTodoTitle(text = item.name)
                    } else {
                        if (!item.isImageOrEmojiPresent()) {
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (item.layout == ObjectType.Layout.PROFILE) {
                            TemplateItemTitle(
                                text = item.name,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            TemplateItemTitle(text = item.name)
                        }
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
                                Spacer(modifier = Modifier.height(6.dp))
                                TemplateItemTitle(
                                    text = item.name,
                                    textAlign = TextAlign.Center
                                )
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
        Image(
            painter = rememberAsyncImagePainter(
                model = it,
                error = painterResource(id = R.drawable.ic_home_widget_space)
            ),
            contentDescription = "Custom image template's icon",
            modifier = modifier,
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
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(82.dp)
                        .padding(top = 50.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                    TemplateItemIconOrImage(item = item, modifier = modifier)
                }
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
private fun TemplateItemTitle(text: String, textAlign: TextAlign = TextAlign.Start) {
    Text(
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp
        ),
        text = text.ifBlank { stringResource(id = R.string.untitled) },
        style = Caption2Semibold.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        maxLines = 2,
        textAlign = textAlign
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
    val state = TemplatesWidgetUiState(
        items = items,
        showWidget = true,
        isEditing = true,
        isMoreMenuVisible = true,
        moreMenuTemplate = null
    )
    ObjectTypeTemplatesWidget(
        state = state,
        onDismiss = {},
        itemClick = {},
        editClick = {},
        doneClick = {},
        moreClick = {},
        scope = CoroutineScope(
            Dispatchers.Main
        ),
        menuClick = {}
    )
}