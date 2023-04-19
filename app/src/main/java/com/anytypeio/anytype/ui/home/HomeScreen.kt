package com.anytypeio.anytype.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.FromIndex
import com.anytypeio.anytype.presentation.widgets.ToIndex
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetActionButton
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LibraryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import timber.log.Timber

@Composable
fun HomeScreen(
    spaceIconView: SpaceIconView,
    mode: InteractionMode,
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onBundledWidgetClicked: (WidgetId) -> Unit,
    onCreateWidget: () -> Unit,
    onEditWidgets: () -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onExitEditMode: () -> Unit,
    onSearchClicked: () -> Unit,
    onLibraryClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onSpaceClicked: () -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        WidgetList(
            widgets = widgets,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetObjectClicked = onWidgetObjectClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onBundledWidgetHeaderClicked = onBundledWidgetClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onChangeWidgetView = onChangeWidgetView,
            onEditWidgets = onEditWidgets,
            onLibraryClicked = onLibraryClicked,
            onMove = onMove
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 26.dp, vertical = 20.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                HomeScreenButton(
                    text = stringResource(id = R.string.add),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onCreateWidget)
                )
                Spacer(modifier = Modifier.width(10.dp))
                HomeScreenButton(
                    text = stringResource(id = R.string.done),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onExitEditMode)
                )
            }
        }
        AnimatedVisibility(
            visible = mode is InteractionMode.Default,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            HomeScreenBottomToolbar(
                spaceIconView = spaceIconView,
                onSearchClicked = throttledClick(onSearchClicked),
                onCreateNewObjectClicked = throttledClick(onCreateNewObjectClicked),
                onSpaceClicked = throttledClick(onSpaceClicked),
                modifier = Modifier
            )
        }
    }

    BackHandler(enabled = mode is InteractionMode.Edit) { onExitEditMode() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetList(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onBundledWidgetHeaderClicked: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    mode: InteractionMode,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onEditWidgets: () -> Unit,
    onLibraryClicked: () -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit
) {
    val views = remember { mutableStateOf(widgets) }
    views.value = widgets
    val lazyListState = rememberReorderableLazyListState(
        onMove = { from, to ->
            views.value = views.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { from, to ->
            if (from != to) {
                onMove(views.value, from, to)
            }
        },
        canDragOver = { draggedOver, _ ->
            val curr = views.value
            val targetView = curr[draggedOver.index]
            (targetView is WidgetView.Draggable)
        }
    )
    LazyColumn(
        state = lazyListState.listState,
        modifier = Modifier
            .reorderable(lazyListState)
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = views.value,
            key = { _, item -> item.id }
        ) { index, item ->
            when (item) {
                is WidgetView.Tree -> {
                    ReorderableItem(lazyListState, key = item.id) { isDragged ->
                        val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (index == 0) 6.dp else 0.dp)
                                .animateContentSize(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .then(
                                    if (mode is InteractionMode.Edit)
                                        Modifier.detectReorderAfterLongPress(lazyListState)
                                    else
                                        Modifier
                                )
                                .alpha(alpha.value)
                        ) {
                            TreeWidgetCard(
                                item = item,
                                onExpandElement = onExpand,
                                onDropDownMenuAction = { action ->
                                    onWidgetMenuAction(item.id, action)
                                },
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                mode = mode
                            )
                            AnimatedVisibility(
                                visible = mode is InteractionMode.Edit,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp),
                                enter = fadeIn() + slideInHorizontally { it / 4 },
                                exit = fadeOut() + slideOutHorizontally { it / 4 }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_remove_widget),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                        .background(
                                            shape = CircleShape,
                                            color = Color.Gray
                                        )
                                        .noRippleClickable {
                                            onWidgetMenuAction(
                                                item.id, DropDownMenuAction.RemoveWidget
                                            )
                                        },
                                    contentDescription = "Remove widget icon"
                                )
                            }
                        }
                    }
                }
                is WidgetView.Link -> {
                    ReorderableItem(lazyListState, key = item.id) { isDragged ->
                        val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (index == 0) 6.dp else 0.dp)
                                .then(
                                    if (mode is InteractionMode.Edit)
                                        Modifier.detectReorderAfterLongPress(lazyListState)
                                    else
                                        Modifier
                                )
                                .alpha(alpha.value)
                        ) {
                            LinkWidgetCard(
                                item = item,
                                onDropDownMenuAction = { action ->
                                    onWidgetMenuAction(item.id, action)
                                },
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                isInEditMode = mode is InteractionMode.Edit
                            )
                            AnimatedVisibility(
                                visible = mode is InteractionMode.Edit,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp),
                                enter = fadeIn() + slideInHorizontally { it / 4 },
                                exit = fadeOut() + slideOutHorizontally { it / 4 }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_remove_widget),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                        .background(
                                            shape = CircleShape,
                                            color = Color.Gray
                                        )
                                        .noRippleClickable {
                                            onWidgetMenuAction(
                                                item.id, DropDownMenuAction.RemoveWidget
                                            )
                                        },
                                    contentDescription = "Remove widget icon"
                                )
                            }
                        }
                    }
                }
                is WidgetView.SetOfObjects -> {
                    ReorderableItem(
                        lazyListState, key = item.id
                    ) { isDragged ->
                        val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (index == 0) 6.dp else 0.dp)
                                .animateContentSize(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .then(
                                    if (mode is InteractionMode.Edit)
                                        Modifier.detectReorderAfterLongPress(lazyListState)
                                    else
                                        Modifier
                                )
                                .alpha(alpha.value)
                        ) {
                            DataViewListWidgetCard(
                                item = item,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onDropDownMenuAction = { action ->
                                    onWidgetMenuAction(item.id, action)
                                },
                                onChangeWidgetView = onChangeWidgetView,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                mode = mode
                            )
                            AnimatedVisibility(
                                visible = mode is InteractionMode.Edit,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp),
                                enter = fadeIn() + slideInHorizontally { it / 4 },
                                exit = fadeOut() + slideOutHorizontally { it / 4 }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_remove_widget),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                        .background(
                                            shape = CircleShape,
                                            color = Color.Gray
                                        )
                                        .noRippleClickable {
                                            onWidgetMenuAction(
                                                item.id, DropDownMenuAction.RemoveWidget
                                            )
                                        },
                                    contentDescription = "Remove widget icon"
                                )
                            }
                        }
                    }
                }
                is WidgetView.ListOfObjects -> {
                    ReorderableItem(
                        lazyListState, key = item.id
                    ) { isDragged ->
                        val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (index == 0) 6.dp else 0.dp)
                                .animateContentSize(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .then(
                                    if (mode is InteractionMode.Edit)
                                        Modifier.detectReorderAfterLongPress(lazyListState)
                                    else
                                        Modifier
                                )
                                .alpha(alpha.value)
                        ) {
                            ListWidgetCard(
                                item = item,
                                mode = mode,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onDropDownMenuAction = { action ->
                                    onWidgetMenuAction(item.id, action)
                                },
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState
                            )
                            AnimatedVisibility(
                                visible = mode is InteractionMode.Edit,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp),
                                enter = fadeIn() + slideInHorizontally { it / 4 },
                                exit = fadeOut() + slideOutHorizontally { it / 4 }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_remove_widget),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(24.dp)
                                        .background(
                                            shape = CircleShape,
                                            color = Color.Gray
                                        )
                                        .noRippleClickable {
                                            onWidgetMenuAction(
                                                item.id, DropDownMenuAction.RemoveWidget
                                            )
                                        },
                                    contentDescription = "Remove widget icon"
                                )
                            }
                        }
                    }
                }
                is WidgetView.Bin -> {
                    BinWidgetCard(
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onClick = { onBundledWidgetHeaderClicked(item.id) },
                        mode = mode
                    )
                }
                is WidgetView.Library -> {
                    LibraryWidgetCard(
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onClick = onLibraryClicked,
                        mode = mode
                    )
                }
                is WidgetView.Action.EditWidgets -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(128.dp)
                            .animateItemPlacement(
                                spring(
                                    stiffness = Spring.StiffnessHigh,
                                    visibilityThreshold = IntOffset.Zero
                                )
                            )
                    ) {
                        AnimatedVisibility(
                            visible = mode is InteractionMode.Default,
                            modifier = Modifier.align(Alignment.TopCenter),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            WidgetActionButton(
                                label = stringResource(R.string.edit_widgets),
                                onClick = onEditWidgets,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(
                color = colorResource(id = R.color.home_screen_button),
                shape = RoundedCornerShape(14.dp)
            )
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = UXBody,
            color = colorResource(id = R.color.text_button_label)
        )
    }
}

@Composable
fun HomeScreenBottomToolbar(
    spaceIconView: SpaceIconView,
    modifier: Modifier,
    onSearchClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onSpaceClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .width(216.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_button)
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onSearchClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_search),
                contentDescription = "Search icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onCreateNewObjectClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_plus),
                contentDescription = "Plus icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onSpaceClicked() }
        ) {
            Timber.d("Binding icon: $spaceIconView")
            when(spaceIconView) {
                is SpaceIconView.Emoji -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = Emojifier.uri(spaceIconView.unicode),
                            error = painterResource(id = R.drawable.ic_home_widget_space)
                        ),
                        contentDescription = "Emoji space icon",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
                is SpaceIconView.Image -> {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = spaceIconView.url,
                            error = painterResource(id = R.drawable.ic_home_widget_space)
                        ),
                        contentDescription = "Custom image space icon",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_home_widget_space),
                        contentDescription = "Placeholder space icon",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}