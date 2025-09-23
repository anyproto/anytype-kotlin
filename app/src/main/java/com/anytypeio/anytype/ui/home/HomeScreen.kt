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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.dv.DefaultDragAndDropModifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.FromIndex
import com.anytypeio.anytype.presentation.widgets.ToIndex
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetActionButton
import com.anytypeio.anytype.ui.widgets.types.AllContentWidgetCard
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.EmptyStateWidgetScreen
import com.anytypeio.anytype.ui.widgets.types.GalleryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreen(
    navPanelState: NavPanelState,
    modifier: Modifier,
    mode: InteractionMode,
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
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
    onNavBarShareButtonClicked: () -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSpaceWidgetClicked: () -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onSpaceWidgetShareIconClicked: (ObjectWrapper.SpaceView) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionClicked: (Id) -> Unit = {}
    ) {

    Box(modifier = modifier.fillMaxSize()) {
        WidgetList(
            widgets = widgets,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetElementClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onBundledWidgetHeaderClicked = onBundledWidgetClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onChangeWidgetView = onChangeWidgetView,
            onEditWidgets = onEditWidgets,
            onSpaceWidgetClicked = onSpaceWidgetClicked,
            onMove = onMove,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onSpaceWidgetShareIconClicked = onSpaceWidgetShareIconClicked,
            onCreateWidget = onCreateWidget,
            onCreateDataViewObject = onCreateDataViewObject,
            onCreateElement = onCreateElement,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onCreateNewTypeClicked = onCreateNewTypeClicked,
            onSectionClicked = onSectionClicked
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
                onSearchClick = onSearchClicked,
                onAddDocClick = onCreateNewObjectClicked,
                onAddDocLongClick = onCreateNewObjectLongClicked,
                onShareButtonClicked = onNavBarShareButtonClicked,
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
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onBundledWidgetHeaderClicked: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    mode: InteractionMode,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onEditWidgets: () -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSpaceWidgetClicked: () -> Unit,
    onSpaceWidgetShareIconClicked: (ObjectWrapper.SpaceView) -> Unit,
    onCreateWidget: () -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionClicked: (Id) -> Unit = {}
) {

    val view = LocalView.current

    val views = remember { mutableStateOf(widgets) }
    views.value = widgets

    val lazyListState = rememberLazyListState()

    val lastFromIndex = remember { mutableStateOf<Int?>(null) }
    val lastToIndex = remember { mutableStateOf<Int?>(null) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        lastFromIndex.value = from.index
        lastToIndex.value = to.index

        views.value = views.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    val onDragStoppedHandler = {
        val from = lastFromIndex.value
        val to = lastToIndex.value
        if (from != null && to != null && from != to) {
            onMove(views.value, from, to)
        }
        // Reset after firing
        lastFromIndex.value = null
        lastToIndex.value = null
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = views.value,
            key = { _, item -> item.id }
        ) { index, item ->
            when (item) {
                is WidgetView.SpaceWidget.View -> {
                    SpaceWidgetCard(
                        onClick = onSpaceWidgetClicked,
                        name = item.space.name.orEmpty(),
                        icon = item.icon,
                        spaceType = item.type,
                        onSpaceShareIconClicked = { onSpaceWidgetShareIconClicked(item.space) },
                        isShared = item.isShared,
                        membersCount = item.membersCount
                    )
                }
                is WidgetView.Tree -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            TreeWidgetItem(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                alpha = alpha.value,
                                item = item,
                                onExpand = onExpand,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onWidgetElementClicked = { obj ->
                                    onWidgetElementClicked(item.id, obj)
                                },
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onWidgetMenuTriggered = onWidgetMenuTriggered,
                                onCreateElement = onCreateElement
                            )
                        }
                    } else {
                        TreeWidgetItem(
                            index = index,
                            mode = mode,
                            alpha = 1.0f,
                            item = item,
                            onExpand = onExpand,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onWidgetElementClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onCreateElement = onCreateElement
                        )
                    }
                }
                is WidgetView.Link -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            LinkWidgetItem(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                alpha = alpha.value,
                                item = item,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onObjectCheckboxClicked = onObjectCheckboxClicked
                            )
                        }
                    } else {
                        LinkWidgetItem(
                            index = index,
                            mode = mode,
                            alpha = 1.0f,
                            item = item,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onObjectCheckboxClicked = onObjectCheckboxClicked
                        )
                    }
                }
                is WidgetView.SetOfObjects -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            SetOfObjectsItem(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                alpha = alpha.value,
                                item = item,
                                onWidgetElementClicked = { obj ->
                                    onWidgetElementClicked(item.id, obj)
                                },
                                onWidgetMenuTriggered = onWidgetMenuTriggered,
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onChangeWidgetView = onChangeWidgetView,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onCreateDataViewObject = onCreateDataViewObject,
                                onCreateElement = onCreateElement
                            )
                        }
                    } else {
                        SetOfObjectsItem(
                            index = index,
                            mode = mode,
                            alpha = 1.0f,
                            item = item,
                            onWidgetElementClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateDataViewObject = onCreateDataViewObject,
                            onCreateElement = onCreateElement
                        )
                    }
                }
                is WidgetView.Gallery -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            GalleryWidgetItem(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                alpha = alpha.value,
                                item = item,
                                onWidgetElementClicked = { obj ->
                                    onWidgetElementClicked(item.id, obj)
                                },
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onChangeWidgetView = onChangeWidgetView,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onWidgetMenuTriggered = onWidgetMenuTriggered
                            )
                        }
                    } else {
                        GalleryWidgetItem(
                            index = index,
                            mode = mode,
                            alpha = 1.0f,
                            item = item,
                            onWidgetElementClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onCreateElement = onCreateElement
                        )
                    }
                }
                is WidgetView.ListOfObjects -> {
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            ListOfObjectsItem(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                alpha = alpha.value,
                                item = item,
                                onWidgetElementClicked = { obj ->
                                    onWidgetElementClicked(item.id, obj)
                                },
                                onWidgetSourceClicked = onWidgetSourceClicked,
                                onWidgetMenuTriggered = onWidgetMenuTriggered,
                                onWidgetMenuAction = onWidgetMenuAction,
                                onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                onCreateElement = onCreateElement
                            )
                        }
                    } else {
                        ListOfObjectsItem(
                            index = index,
                            mode = mode,
                            alpha = 1.0f,
                            item = item,
                            onWidgetElementClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onWidgetMenuAction = onWidgetMenuAction,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateElement = onCreateElement
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
                    if (mode is InteractionMode.Edit) {
                        ReorderableItem(reorderableLazyListState, key = item.id) { isDragged ->
                            val alpha = animateFloatAsState(if (isDragged) 0.8f else 1.0f)
                            AllContentWidgetCard(
                                modifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                                index = index,
                                mode = mode,
                                onWidgetClicked = {
                                    onWidgetSourceClicked(
                                        item.id,
                                        Widget.Source.Bundled.AllObjects
                                    )
                                },
                                onDropDownMenuAction = { action ->
                                    onWidgetMenuAction(item.id, action)
                                },
                                alpha = alpha.value
                            )
                        }
                    } else {
                        AllContentWidgetCard(
                            index = index,
                            mode = mode,
                            onWidgetClicked = {
                                onWidgetSourceClicked(
                                    item.id,
                                    Widget.Source.Bundled.AllObjects
                                )
                            },
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            alpha = 1.0f
                        )
                    }
                }
                is WidgetView.SpaceChat -> {
                    SpaceChatWidgetCard(
                        mode = mode,
                        unReadMentionCount = item.unreadMentionCount,
                        unReadMessageCount = item.unreadMessageCount,
                        isMuted = item.isMuted,
                        onWidgetClicked = { onWidgetSourceClicked(item.id, item.source) },
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        }
                    )
                }
                is WidgetView.Action.EditWidgets -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 20.dp, end = 20.dp)
                            .height(128.dp)
                            .animateItem(
                                placementSpec = spring(
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
                is WidgetView.EmptyState -> {
                    if (mode !is InteractionMode.Edit) {
                        EmptyStateWidgetScreen(
                            modifier = Modifier.fillParentMaxSize(),
                            onAddWidgetClicked = {
                                onCreateWidget()
                            }
                        )
                    }
                }

                WidgetView.Section.ObjectTypes -> {
                    SpaceObjectTypesSectionHeader(
                        onCreateNewTypeClicked = onCreateNewTypeClicked,
                        onSectionClicked = { onSectionClicked(SECTION_OBJECT_TYPE) }
                    )
                }
                WidgetView.Section.Pinned -> {
                    PinnedSectionHeader(
                        onSectionClicked = { onSectionClicked(SECTION_PINNED) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
private fun ListOfObjectsItem(
    modifier: Modifier = Modifier,
    index: Int,
    mode: InteractionMode,
    alpha: Float,
    item: WidgetView.ListOfObjects,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {}
) {
    Box(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        ListWidgetCard(
            item = item,
            mode = mode,
            onWidgetObjectClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateElement = onCreateElement,
            onWidgetMenuTriggered = onWidgetMenuTriggered
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
    modifier: Modifier = Modifier,
    index: Int,
    mode: InteractionMode,
    alpha: Float,
    item: WidgetView.SetOfObjects,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {}
) {
    Box(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        DataViewListWidgetCard(
            item = item,
            onWidgetObjectClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onChangeWidgetView = onChangeWidgetView,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateElement = onCreateElement
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
    modifier: Modifier = Modifier,
    index: Int,
    mode: InteractionMode,
    alpha: Float,
    item: WidgetView.Gallery,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {}
) {
    Box(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        GalleryWidgetCard(
            item = item,
            onWidgetObjectClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onChangeWidgetView = onChangeWidgetView,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onCreateElement = onCreateElement
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
    modifier: Modifier = Modifier,
    index: Int,
    mode: InteractionMode,
    alpha: Float,
    item: WidgetView.Link,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        LinkWidgetCard(
            item = item,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onWidgetSourceClicked = onWidgetSourceClicked,
            isInEditMode = mode is InteractionMode.Edit,
            hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
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
private fun TreeWidgetItem(
    modifier: Modifier = Modifier,
    index: Int,
    mode: InteractionMode,
    alpha: Float,
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onCreateElement: (WidgetView) -> Unit
) {
    Box(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(top = if (index == 0) 6.dp else 0.dp)
            .alpha(alpha)
    ) {
        TreeWidgetCard(
            item = item,
            onExpandElement = onExpand,
            onDropDownMenuAction = { action ->
                onWidgetMenuAction(item.id, action)
            },
            onWidgetElementClicked = onWidgetElementClicked,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            onCreateElement = onCreateElement,
            mode = mode,
            onWidgetMenuClicked = onWidgetMenuTriggered
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

@Composable
private fun SpaceObjectTypesSectionHeader(
    onSectionClicked: () -> Unit,
    onCreateNewTypeClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onSectionClicked() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp),
            text = stringResource(R.string.widgets_section_object_types),
            style = Caption1Medium,
            color = colorResource(id = R.color.control_transparent_secondary)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = "Create new type",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 12.dp)
                .size(18.dp)
                .noRippleClickable { onCreateNewTypeClicked() },
            contentScale = ContentScale.Inside
        )
    }
}

@Composable
private fun PinnedSectionHeader(
    onSectionClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onSectionClicked() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp),
            text = stringResource(R.string.widgets_section_pinned),
            style = Caption1Medium,
            color = colorResource(id = R.color.control_transparent_secondary)
        )
    }
}