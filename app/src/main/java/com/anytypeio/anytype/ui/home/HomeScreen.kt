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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.FromIndex
import com.anytypeio.anytype.presentation.widgets.ToIndex
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetActionButton
import com.anytypeio.anytype.ui.widgets.types.AllContentWidgetCard
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.GalleryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun HomeScreen(
    navPanelState: NavPanelState,
    modifier: Modifier,
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
    onHomeButtonClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onCreateNewObjectLongClicked: () -> Unit,
    onShareButtonClicked: () -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSpaceWidgetClicked: () -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onSpaceShareIconClicked: (ObjectWrapper.SpaceView) -> Unit,
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit,
    onCreateObjectInsideWidget: (Id) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {
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
            onSpaceWidgetClicked = onSpaceWidgetClicked,
            onMove = onMove,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onSpaceShareIconClicked = onSpaceShareIconClicked,
            onSeeAllObjectsClicked = onSeeAllObjectsClicked,
            onCreateWidget = onCreateWidget,
            onCreateObjectInsideWidget = onCreateObjectInsideWidget,
            onCreateDataViewObject = onCreateDataViewObject
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
                WidgetEditModeButton(
                    text = stringResource(id = R.string.add),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onCreateWidget)
                )
                Spacer(modifier = Modifier.width(10.dp))
                WidgetEditModeButton(
                    text = stringResource(id = R.string.done),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onExitEditMode)
                )
            }
        }
        AnimatedVisibility(
            visible = mode !is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            BottomNavigationMenu(
                state = navPanelState,
                modifier = Modifier,
                searchClick = onSearchClicked,
                addDocClick = onCreateNewObjectClicked,
                addDocLongClick = onCreateNewObjectLongClicked,
                onShareButtonClicked = onShareButtonClicked,
                onHomeButtonClicked = onHomeButtonClicked
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
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSpaceWidgetClicked: () -> Unit,
    onSpaceShareIconClicked: (ObjectWrapper.SpaceView) -> Unit,
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit,
    onCreateWidget: () -> Unit,
    onCreateObjectInsideWidget: (Id) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit
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
            key = { _, item -> "home-widget-${item.id}" }
        ) { index, item ->
            when (item) {
                is WidgetView.SpaceWidget.View -> {
                    SpaceWidgetCard(
                        onClick = onSpaceWidgetClicked,
                        name = item.space.name.orEmpty(),
                        icon = item.icon,
                        spaceType = item.type,
                        onSpaceShareIconClicked = { onSpaceShareIconClicked(item.space) },
                        isShared = item.isShared,
                        membersCount = item.membersCount
                    )
                }
                is WidgetView.Tree -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(lazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            TreeWidgetItem(
                                index = index,
                                mode = mode,
                                lazyListState = lazyListState,
                                alpha = alpha.value,
                                item = item,
                                onExpand = onExpand,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onCreateObjectInsideWidget = onCreateObjectInsideWidget
                            )
                        }
                    } else {
                        TreeWidgetItem(
                            index = index,
                            mode = mode,
                            lazyListState = lazyListState,
                            alpha = 1.0f,
                            item = item,
                            onExpand = onExpand,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onCreateObjectInsideWidget = onCreateObjectInsideWidget
                        )
                    }
                }
                is WidgetView.Link -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(lazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            LinkWidgetItem(
                                index = index,
                                mode = mode,
                                lazyListState = lazyListState,
                                alpha = alpha.value,
                                item = item,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onWidgetSourceClicked = onWidgetSourceClicked
                            )
                        }
                    } else {
                        LinkWidgetItem(
                            index = index,
                            mode = mode,
                            lazyListState = lazyListState,
                            alpha = 1.0f,
                            item = item,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onWidgetSourceClicked = onWidgetSourceClicked
                        )
                    }
                }
                is WidgetView.SetOfObjects -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(
                            lazyListState, key = item.id
                        ) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            SetOfObjectsItem(
                                index = index,
                                mode = mode,
                                lazyListState = lazyListState,
                                alpha = alpha.value,
                                item = item,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onChangeWidgetView = onChangeWidgetView,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onCreateDataViewObject = onCreateDataViewObject
                            )
                        }
                    } else {
                        SetOfObjectsItem(
                            index = index,
                            mode = mode,
                            lazyListState = lazyListState,
                            alpha = 1.0f,
                            item = item,
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateDataViewObject = onCreateDataViewObject
                        )
                    }
                }
                is WidgetView.Gallery -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(
                            lazyListState, key = item.id
                        ) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            GalleryWidgetItem(
                                index = index,
                                mode = mode,
                                lazyListState = lazyListState,
                                alpha = alpha.value,
                                item = item,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onChangeWidgetView = onChangeWidgetView,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onSeeAllObjectsClicked = onSeeAllObjectsClicked
                            )
                        }
                    } else {
                        GalleryWidgetItem(
                            index = index,
                            mode = mode,
                            lazyListState = lazyListState,
                            alpha = 1.0f,
                            item = item,
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onSeeAllObjectsClicked = onSeeAllObjectsClicked
                        )
                    }
                }
                is WidgetView.ListOfObjects -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(
                            lazyListState, key = item.id
                        ) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            ListOfObjectsItem(
                                index = index,
                                mode = mode,
                                lazyListState = lazyListState,
                                alpha = alpha.value,
                                item = item,
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked
                            )
                        }
                    } else {
                        ListOfObjectsItem(
                            index = index,
                            mode = mode,
                            lazyListState = lazyListState,
                            alpha = 1.0f,
                            item = item,
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked
                        )
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
                is WidgetView.AllContent -> {
                    AllContentWidgetCard(
                        mode = mode,
                        onWidgetClicked = { onBundledWidgetHeaderClicked(item.id) }
                    )
                }
                is WidgetView.SpaceChat -> {
                    SpaceChatWidgetCard(
                        mode = mode,
                        onWidgetClicked = { onBundledWidgetHeaderClicked(item.id) }
                    )
                }
                is WidgetView.Action.EditWidgets -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 20.dp, end = 20.dp)
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
                            Row {
                                WidgetActionButton(
                                    label = stringResource(R.string.add_widget),
                                    onClick = throttledClick(
                                        onClick = {
                                            onCreateWidget()
                                        }
                                    ),
                                    modifier = Modifier.weight(1.0f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                WidgetActionButton(
                                    label = stringResource(R.string.edit_widgets),
                                    onClick = onEditWidgets,
                                    modifier = Modifier.weight(1.0f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListOfObjectsItem(
    index: Int,
    mode: InteractionMode,
    lazyListState: ReorderableLazyListState,
    alpha: Float,
    item: WidgetView.ListOfObjects,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .then(
                if (mode is InteractionMode.Edit)
                    Modifier.detectReorderAfterLongPress(lazyListState)
                else
                    Modifier
            )
            .alpha(alpha)
    ) {
        ListWidgetCard(
            item = item,
            mode = mode,
            onWidgetObjectClicked = onWidgetObjectClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            onObjectCheckboxClicked = onObjectCheckboxClicked
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp),
            enter = fadeIn() + slideInHorizontally { it / 4 },
            exit = fadeOut() + slideOutHorizontally { it / 4 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_remove_widget),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp)
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

@Composable
private fun SetOfObjectsItem(
    index: Int,
    mode: InteractionMode,
    lazyListState: ReorderableLazyListState,
    alpha: Float,
    item: WidgetView.SetOfObjects,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit
) {
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .then(
                if (mode is InteractionMode.Edit)
                    Modifier.detectReorderAfterLongPress(lazyListState)
                else
                    Modifier
            )
            .alpha(alpha)
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
            mode = mode,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateDataViewObject = onCreateDataViewObject
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp),
            enter = fadeIn() + slideInHorizontally { it / 4 },
            exit = fadeOut() + slideOutHorizontally { it / 4 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_remove_widget),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp)
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

@Composable
private fun GalleryWidgetItem(
    index: Int,
    mode: InteractionMode,
    lazyListState: ReorderableLazyListState,
    alpha: Float,
    item: WidgetView.Gallery,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit
) {
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .then(
                if (mode is InteractionMode.Edit)
                    Modifier.detectReorderAfterLongPress(lazyListState)
                else
                    Modifier
            )
            .alpha(alpha)
    ) {
        GalleryWidgetCard(
            item = item,
            onWidgetObjectClicked = onWidgetObjectClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onChangeWidgetView = onChangeWidgetView,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onSeeAllObjectsClicked = onSeeAllObjectsClicked
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp),
            enter = fadeIn() + slideInHorizontally { it / 4 },
            exit = fadeOut() + slideOutHorizontally { it / 4 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_remove_widget),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp)
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

@Composable
private fun LinkWidgetItem(
    index: Int,
    mode: InteractionMode,
    lazyListState: ReorderableLazyListState,
    alpha: Float,
    item: WidgetView.Link,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit
) {
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
            .alpha(alpha)
    ) {
        LinkWidgetCard(
            item = item,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onWidgetSourceClicked = onWidgetSourceClicked,
            isInEditMode = mode is InteractionMode.Edit,
            hasReadOnlyAccess = mode is InteractionMode.ReadOnly
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp),
            enter = fadeIn() + slideInHorizontally { it / 4 },
            exit = fadeOut() + slideOutHorizontally { it / 4 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_remove_widget),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp)
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

@Composable
private fun TreeWidgetItem(
    index: Int,
    mode: InteractionMode,
    lazyListState: ReorderableLazyListState,
    alpha: Float,
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onCreateObjectInsideWidget: (Id) -> Unit
) {
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .then(
                if (mode is InteractionMode.Edit)
                    Modifier.detectReorderAfterLongPress(lazyListState)
                else
                    Modifier
            )
            .alpha(alpha)
    ) {
        TreeWidgetCard(
            item = item,
            onExpandElement = onExpand,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onWidgetObjectClicked = onWidgetObjectClicked,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onCreateObjectInsideWidget = onCreateObjectInsideWidget
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp),
            enter = fadeIn() + slideInHorizontally { it / 4 },
            exit = fadeOut() + slideOutHorizontally { it / 4 }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_remove_widget),
                modifier = Modifier
                    .height(24.dp)
                    .width(24.dp)
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

@Composable
fun WidgetEditModeButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(
                color = colorResource(id = R.color.widgets_edit_mode_button),
                shape = RoundedCornerShape(14.dp)
            )
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = UXBody,
            color = colorResource(id = R.color.text_white)
        )
    }
}