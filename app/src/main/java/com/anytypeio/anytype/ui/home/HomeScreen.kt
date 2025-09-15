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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.dv.DefaultDragAndDropModifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.home.SystemTypeView
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
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
    systemTypes: List<SystemTypeView>,
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
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit,
    onCreateObjectInsideWidget: (Id) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onSystemTypeClicked: (SystemTypeView) -> Unit,
    onCreateNewTypeClicked: () -> Unit,
    onCreateNewObjectOfTypeClicked: (SystemTypeView) -> Unit,
    onDeleteSystemTypeClicked: (SystemTypeView) -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {
        WidgetList(
            widgets = widgets,
            systemTypes = systemTypes,
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
            onSeeAllObjectsClicked = onSeeAllObjectsClicked,
            onCreateWidget = onCreateWidget,
            onCreateObjectInsideWidget = onCreateObjectInsideWidget,
            onCreateDataViewObject = onCreateDataViewObject,
            onCreateElement = onCreateElement,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onSystemTypeClicked = onSystemTypeClicked,
            onCreateNewTypeClicked = onCreateNewTypeClicked,
            onCreateNewObjectOfTypeClicked = onCreateNewObjectOfTypeClicked,
            onDeleteSystemTypeClicked = onDeleteSystemTypeClicked
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
    systemTypes: List<SystemTypeView>,
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
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit,
    onCreateWidget: () -> Unit,
    onCreateObjectInsideWidget: (Id) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onSystemTypeClicked: (SystemTypeView) -> Unit,
    onCreateNewTypeClicked: () -> Unit,
    onCreateNewObjectOfTypeClicked: (SystemTypeView) -> Unit,
    onDeleteSystemTypeClicked: (SystemTypeView) -> Unit
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
        // Object Types Section
        item {
            PinnedSectionHeader()
        }

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
                                onCreateObjectInsideWidget = onCreateObjectInsideWidget,
                                onWidgetMenuTriggered = onWidgetMenuTriggered
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
                            onCreateObjectInsideWidget = onCreateObjectInsideWidget,
                            onWidgetMenuTriggered = onWidgetMenuTriggered
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
                                onWidgetMenuTriggered = onWidgetMenuTriggered
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
                            onWidgetMenuTriggered = onWidgetMenuTriggered
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
                                onSeeAllObjectsClicked = onSeeAllObjectsClicked,
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
                            onSeeAllObjectsClicked = onSeeAllObjectsClicked,
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
            }
        }
        
        // Object Types Section
        item {
            SystemTypesSectionHeader(
                onCreateNewTypeClicked = onCreateNewTypeClicked
            )
        }

        // Individual system type items
        itemsIndexed(
            items = systemTypes,
            key = { _, systemType -> "systemType_${systemType.id}" }
        ) { index, systemType ->
            SystemTypeItem(
                systemType = systemType,
                onClicked = { onSystemTypeClicked(systemType) },
                onNewObjectClicked = onCreateNewObjectOfTypeClicked,
                onDeleteTypeClicked = onDeleteSystemTypeClicked,
                isCreateObjectAllowed = systemType.isCreateObjectAllowed
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
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
            onCreateDataViewObject = onCreateDataViewObject,
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
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit,
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
            onSeeAllObjectsClicked = onSeeAllObjectsClicked,
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
    onWidgetMenuTriggered: (WidgetId) -> Unit,
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
    onCreateObjectInsideWidget: (Id) -> Unit
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
            mode = mode,
            onCreateObjectInsideWidget = onCreateObjectInsideWidget,
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
private fun SystemTypesSectionHeader(
    onCreateNewTypeClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
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
private fun PinnedSectionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.SystemTypeItem(
    systemType: SystemTypeView,
    onClicked: () -> Unit,
    onNewObjectClicked: (SystemTypeView) -> Unit,
    onDeleteTypeClicked: (SystemTypeView) -> Unit,
    isCreateObjectAllowed: Boolean
) {
    // Choose rendering based on widget layout, reusing existing widget cards
    when (systemType.widgetLayout) {
        Block.Content.Widget.Layout.TREE -> {
            val widgetView = systemType.toTreeWidgetView()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp)
                    .animateItem()
            ) {
                TreeWidgetCard(
                    item = widgetView,
                    mode = InteractionMode.Default,
                    onExpandElement = { /* No-op for system types */ },
                    onWidgetElementClicked = { onClicked() },
                    onWidgetSourceClicked = { _, _ -> onClicked() },
                    onWidgetMenuClicked = { /* Handled by custom menu */ },
                    onDropDownMenuAction = { /* No-op */ },
                    onToggleExpandedWidgetState = { /* No-op */ },
                    onObjectCheckboxClicked = { _, _ -> /* No-op */ },
                    onCreateObjectInsideWidget = { onNewObjectClicked(systemType) }
                )

                // Custom menu for system type
                SystemTypeOverlayMenu(
                    systemType = systemType,
                    onNewObjectClicked = onNewObjectClicked,
                    onDeleteTypeClicked = onDeleteTypeClicked,
                    isCreateObjectAllowed = isCreateObjectAllowed
                )
            }
        }
        Block.Content.Widget.Layout.LIST -> {
            val widgetView = systemType.toListWidgetView(isCompact = false)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp)
                    .animateItem()
            ) {
                ListWidgetCard(
                    item = widgetView,
                    mode = InteractionMode.Default,
                    onWidgetObjectClicked = { onClicked() },
                    onWidgetSourceClicked = { _, _ -> onClicked() },
                    onWidgetMenuTriggered = { /* Handled by custom menu */ },
                    onDropDownMenuAction = { /* No-op */ },
                    onToggleExpandedWidgetState = { /* No-op */ },
                    onObjectCheckboxClicked = { _, _ -> /* No-op */ },
                    onCreateElement = { onNewObjectClicked(systemType) }
                )

                // Custom menu for system type
                SystemTypeOverlayMenu(
                    systemType = systemType,
                    onNewObjectClicked = onNewObjectClicked,
                    onDeleteTypeClicked = onDeleteTypeClicked,
                    isCreateObjectAllowed = isCreateObjectAllowed
                )
            }
        }
        Block.Content.Widget.Layout.COMPACT_LIST -> {
            val widgetView = systemType.toListWidgetView(isCompact = true)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp)
                    .animateItem()
            ) {
                ListWidgetCard(
                    item = widgetView,
                    mode = InteractionMode.Default,
                    onWidgetObjectClicked = { onClicked() },
                    onWidgetSourceClicked = { _, _ -> onClicked() },
                    onWidgetMenuTriggered = { /* Handled by custom menu */ },
                    onDropDownMenuAction = { /* No-op */ },
                    onToggleExpandedWidgetState = { /* No-op */ },
                    onObjectCheckboxClicked = { _, _ -> /* No-op */ },
                    onCreateElement = { onNewObjectClicked(systemType) }
                )

                // Custom menu for system type
                SystemTypeOverlayMenu(
                    systemType = systemType,
                    onNewObjectClicked = onNewObjectClicked,
                    onDeleteTypeClicked = onDeleteTypeClicked,
                    isCreateObjectAllowed = isCreateObjectAllowed
                )
            }
        }
        Block.Content.Widget.Layout.VIEW -> {
            val widgetView = systemType.toSetOfObjectsWidgetView()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp)
                    .animateItem()
            ) {
                DataViewListWidgetCard(
                    item = widgetView,
                    onWidgetObjectClicked = { onClicked() },
                    onWidgetSourceClicked = { _, _ -> onClicked() },
                    onWidgetMenuTriggered = { /* Handled by custom menu */ },
                    onDropDownMenuAction = { /* No-op */ },
                    onChangeWidgetView = { _, _ -> /* No-op */ },
                    onToggleExpandedWidgetState = { /* No-op */ },
                    mode = InteractionMode.Default,
                    onObjectCheckboxClicked = { _, _ -> /* No-op */ },
                    onCreateDataViewObject = { _, _ -> onNewObjectClicked(systemType) },
                    onCreateElement = { onNewObjectClicked(systemType) }
                )

                // Custom menu for system type
                SystemTypeOverlayMenu(
                    systemType = systemType,
                    onNewObjectClicked = onNewObjectClicked,
                    onDeleteTypeClicked = onDeleteTypeClicked,
                    isCreateObjectAllowed = isCreateObjectAllowed
                )
            }
        }
        Block.Content.Widget.Layout.LINK,
        null -> {
            val widgetView = systemType.toLinkWidgetView()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp)
                    .animateItem()
            ) {
                LinkWidgetCard(
                    item = widgetView,
                    onDropDownMenuAction = { /* No-op */ },
                    onWidgetSourceClicked = { _, _ -> onClicked() },
                    isInEditMode = false,
                    hasReadOnlyAccess = false,
                    onWidgetMenuTriggered = { /* Handled by custom menu */ }
                )

                // Custom menu for system type
                SystemTypeOverlayMenu(
                    systemType = systemType,
                    onNewObjectClicked = onNewObjectClicked,
                    onDeleteTypeClicked = onDeleteTypeClicked,
                    isCreateObjectAllowed = isCreateObjectAllowed
                )
            }
        }
    }
}

// Extension functions to convert SystemTypeView to WidgetView types
private fun SystemTypeView.toTreeWidgetView(): WidgetView.Tree {
    return WidgetView.Tree(
        id = id,
        isLoading = false,
        name = WidgetView.Name.Default(name),
        source = toWidgetSource(),
        elements = emptyList(), // System types don't have tree elements
        isExpanded = false,
        isEditable = false
    )
}

private fun SystemTypeView.toListWidgetView(isCompact: Boolean): WidgetView.ListOfObjects {
    return WidgetView.ListOfObjects(
        id = id,
        isLoading = false,
        source = toWidgetSource(),
        type = WidgetView.ListOfObjects.Type.Favorites, // Default type for system types
        elements = emptyList(), // System types don't have list elements
        isExpanded = true,
        isCompact = isCompact
    )
}

private fun SystemTypeView.toSetOfObjectsWidgetView(): WidgetView.SetOfObjects {
    return WidgetView.SetOfObjects(
        id = id,
        isLoading = false,
        source = toWidgetSource(),
        tabs = emptyList(), // System types don't have tabs
        elements = emptyList(), // System types don't have elements
        isExpanded = true,
        name = WidgetView.Name.Default(name)
    )
}

private fun SystemTypeView.toLinkWidgetView(): WidgetView.Link {
    return WidgetView.Link(
        id = id,
        isLoading = false,
        name = WidgetView.Name.Default(name),
        source = toWidgetSource()
    )
}

private fun SystemTypeView.toWidgetSource(): Widget.Source {
    return Widget.Source.Default(
        obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to id,
                Relations.NAME to name,
                Relations.TYPE to listOf(id), // System type references itself
                Relations.LAYOUT to 0.0 // Basic layout
            )
        )
    )
}

@Composable
private fun SystemTypeOverlayMenu(
    systemType: SystemTypeView,
    onNewObjectClicked: (SystemTypeView) -> Unit,
    onDeleteTypeClicked: (SystemTypeView) -> Unit,
    isCreateObjectAllowed: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Transparent overlay for capturing long press
    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleCombinedClickable(
                onClick = { /* Click handled by widget card */ },
                onLongClicked = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                }
            )
    )

    // Context menu
    SystemTypeItemMenu(
        expanded = showMenu,
        onDismiss = { showMenu = false },
        onNewObjectClicked = {
            onNewObjectClicked(systemType)
            showMenu = false
        },
        onDeleteTypeClicked = {
            onDeleteTypeClicked(systemType)
            showMenu = false
        },
        isCreateObjectAllowed = isCreateObjectAllowed,
        isDeletable = systemType.isDeletable
    )
}

@Composable
private fun SystemTypeItemMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNewObjectClicked: () -> Unit,
    onDeleteTypeClicked: () -> Unit,
    isCreateObjectAllowed: Boolean,
    isDeletable: Boolean
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = (isCreateObjectAllowed || isDeletable) && expanded,
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        // New Object menu item - only show if creation is allowed
        if (isCreateObjectAllowed) {
            DropdownMenuItem(
                onClick = onNewObjectClicked,
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            text = stringResource(R.string.widgets_menu_new_object_type)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_item_create),
                            contentDescription = "New object icon",
                            modifier = Modifier
                                .wrapContentSize(),
                            colorFilter = ColorFilter.tint(
                                colorResource(id = R.color.text_primary)
                            )
                        )
                    }
                }
            )
        }
        
        // Delete Type menu item - only show if deletable (user-created types)
        if (isDeletable) {
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp, height = 8.dp)
            DropdownMenuItem(
                onClick = onDeleteTypeClicked,
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            style = BodyRegular,
                            color = colorResource(id = R.color.palette_system_red),
                            text = stringResource(R.string.widgets_menu_delete_object_type)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_menu_item_delete_type),
                            contentDescription = "Delete type icon",
                            modifier = Modifier.wrapContentSize(),
                            colorFilter = ColorFilter.tint(
                                colorResource(id = R.color.palette_system_red)
                            )
                        )
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemTypesSectionHeaderPreview() {
    SystemTypesSectionHeader(
        onCreateNewTypeClicked = { }
    )
}

@Preview(showBackground = true)
@Composable 
private fun SystemTypeItemPreview() {
    val sampleSystemType = SystemTypeView(
        id = "sample-id",
        name = "Note",
        icon = ObjectIcon.TypeIcon.Emoji(
            unicode = "📝",
            rawValue = "document",
            color = CustomIconColor.DEFAULT
        )
    )
    
    LazyColumn {
        item {
            SystemTypeItem(
                systemType = sampleSystemType,
                onClicked = { },
                onNewObjectClicked = { },
                onDeleteTypeClicked = { },
                isCreateObjectAllowed = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemTypeItemWithFallbackIconPreview() {
    val sampleSystemType = SystemTypeView(
        id = "sample-id-2", 
        name = "Task",
        icon = ObjectIcon.TypeIcon.Fallback("task")
    )
    
    LazyColumn {
        item {
            SystemTypeItem(
                systemType = sampleSystemType,
                onClicked = { },
                onNewObjectClicked = { },
                onDeleteTypeClicked = { },
                isCreateObjectAllowed = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SystemTypesSectionPreview() {
    val sampleTypes = listOf(
        SystemTypeView(
            id = "note-id",
            name = "Note", 
            icon = ObjectIcon.TypeIcon.Emoji(
                unicode = "📝",
                rawValue = "document",
                color = CustomIconColor.DEFAULT
            )
        ),
        SystemTypeView(
            id = "task-id",
            name = "Task",
            icon = ObjectIcon.TypeIcon.Fallback("task")
        ),
        SystemTypeView(
            id = "book-id",
            name = "Book",
            icon = ObjectIcon.TypeIcon.Emoji(
                unicode = "📚", 
                rawValue = "book",
                color = CustomIconColor.Blue
            )
        )
    )
    
    LazyColumn {
        item {
            SystemTypesSectionHeader(
                onCreateNewTypeClicked = { }
            )
        }
        
        itemsIndexed(
            items = sampleTypes,
            key = { _, systemType -> "systemType_${systemType.id}" }
        ) { _, systemType ->
            SystemTypeItem(
                systemType = systemType,
                onClicked = { },
                onNewObjectClicked = { },
                onDeleteTypeClicked = { },
                isCreateObjectAllowed = true
            )
        }
    }
}