package com.anytypeio.anytype.ui.home

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.dv.DefaultDragAndDropModifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.getWidgetMenuItems
import com.anytypeio.anytype.ui.widgets.types.AllContentWidgetCard
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ChatListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.EmptyStateWidgetScreen
import com.anytypeio.anytype.ui.widgets.types.GalleryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import kotlinx.coroutines.delay
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.renderWidgetSection(
    widgets: List<WidgetView>,
    reorderableState: ReorderableLazyListState,
    view: View,
    mode: InteractionMode,
    sectionType: SectionType,
    isOtherSectionDragging: Boolean = false,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit,
    onCreateWidget: () -> Unit
) {
    itemsIndexed(
        items = widgets,
        key = { _, item -> item.id },
        contentType = { _, item -> sectionType }
    ) { index, item ->
        val animateItemModifier = Modifier.animateItem()
        when (item) {
            is WidgetView.Tree -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(
                    item.id,
                    item.sectionType,
                    item.canCreateObjectOfType,
                    item.source
                ) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    TreeWidgetCard(
                        modifier = modifier,
                        mode = mode,
                        item = item,
                        onExpandElement = onExpand,
                        onWidgetElementClicked = { obj ->
                            onWidgetElementClicked(item.id, obj)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                        onWidgetMenuClicked = onWidgetMenuTriggered,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.Link -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.source) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    LinkWidgetCard(
                        modifier = modifier,
                        item = item,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.SetOfObjects -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    DataViewListWidgetCard(
                        modifier = modifier,
                        item = item,
                        mode = mode,
                        onWidgetObjectClicked = { obj ->
                            onWidgetElementClicked(item.id, obj)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                        onWidgetMenuTriggered = onWidgetMenuTriggered,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.Gallery -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    GalleryWidgetCard(
                        modifier = modifier,
                        item = item,
                        mode = mode,
                        onWidgetObjectClicked = { obj ->
                            onWidgetElementClicked(item.id, obj)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                        onWidgetMenuTriggered = onWidgetMenuTriggered,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.ChatList -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    ChatListWidgetCard(
                        modifier = modifier,
                        item = item,
                        mode = mode,
                        onWidgetObjectClicked = { obj ->
                            onWidgetElementClicked(item.id, obj)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                        onWidgetMenuTriggered = onWidgetMenuTriggered,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.ListOfObjects -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    ListWidgetCard(
                        modifier = modifier,
                        item = item,
                        mode = mode,
                        onWidgetObjectClicked = { obj ->
                            onWidgetElementClicked(item.id, obj)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                        onWidgetMenuTriggered = onWidgetMenuTriggered,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.Bin -> {
                BinWidgetCard(
                    item = item,
                    onDropDownMenuAction = { action ->
                        onWidgetMenuAction(item.id, action)
                    },
                    onWidgetSourceClicked = onWidgetSourceClicked,
                )
            }

            is WidgetView.AllContent -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType) {
                    item.getWidgetMenuItems()
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = animateItemModifier
                ) { isDragged ->
                    val hasStartedDragging = remember { mutableStateOf(false) }

                    if (isReorderEnabled) {
                        LaunchedEffect(isDragged) {
                            if (isDragged) {
                                hasStartedDragging.value = true
                                delay(1000)
                                isCardMenuExpanded.value = false
                            } else if (hasStartedDragging.value) {
                                hasStartedDragging.value = false
                            }
                        }
                    }

                    val modifier = WidgetCardModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        mode = mode,
                        view = view,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onWidgetLongClicked = {
                            isCardMenuExpanded.value = !isCardMenuExpanded.value
                        },
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
                        shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                    )

                    AllContentWidgetCard(
                        modifier = modifier,
                        widgetView = item,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.SpaceChat -> {
                SpaceChatWidgetCard(
                    item = item,
                    mode = mode,
                    unReadMentionCount = item.unreadMentionCount,
                    unReadMessageCount = item.unreadMessageCount,
                    isMuted = item.isMuted,
                    onWidgetClicked = { onWidgetSourceClicked(item.id) },
                    onDropDownMenuAction = { action ->
                        onWidgetMenuAction(item.id, action)
                    }
                )
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
fun SpaceObjectTypesSectionHeader(
    mode: InteractionMode,
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
        if (mode !is InteractionMode.ReadOnly) {
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
}

@Composable
fun PinnedSectionHeader(
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

/**
 * Custom DragGestureDetector that uses touch slop to differentiate between:
 * - Long-press without movement (shows menu)
 * - Long-press with drag movement (starts dragging)
 *
 * Based on approach from https://github.com/Calvin-LL/Reorderable/issues/55
 */
private class LongPressWithSlopDetector(
    private val touchSlop: Float,
    private val onMenuTrigger: () -> Unit,
    private val haptic: HapticFeedback,
    private val onDragStarted: () -> Unit = {},
    private val onDragStopped: () -> Unit = {}
) : DragGestureDetector {
    override suspend fun PointerInputScope.detect(
        onDragStart: (Offset) -> Unit,
        onDragEnd: () -> Unit,
        onDragCancel: () -> Unit,
        onDrag: (PointerInputChange, Offset) -> Unit
    ) {
        awaitEachGesture {
            val down = awaitFirstDown()

            // Wait for a long-press. If it gets cancelled (pointer up or moved too far),
            // we stop handling this gesture.
            val longPress = awaitLongPressOrCancellation(down.id) ?: run {
                onDragCancel()
                return@awaitEachGesture
            }

            var isDragging = false
            val pointerId = longPress.id
            val dragStartOffset = longPress.position

            // After long-press is recognized, watch for movement.
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: continue

                if (!change.pressed) {
                    // Pointer was released; decide whether to trigger menu or end drag.
                    if (isDragging) {
                        onDragEnd()
                        onDragStopped()
                    } else {
                        onMenuTrigger()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    break
                }

                val dragDelta = change.positionChange()
                val verticalDelta = dragDelta.y

                if (!isDragging) {
                    // Check if we've moved far enough from the long-press position to start a drag.
                    val verticalOffset = change.position.y - dragStartOffset.y
                    if (abs(verticalOffset) > touchSlop) {
                        isDragging = true
                        onDragStarted()
                        onDragStart(dragStartOffset)
                        change.consume()
                    }
                }

                if (isDragging && verticalDelta != 0f) {
                    onDrag(change, Offset(0f, verticalDelta))
                    change.consume()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReorderableCollectionItemScope.WidgetCardModifier(
    lazyItemScope: LazyItemScope,
    isMenuExpanded: Boolean,
    mode: InteractionMode,
    view: View,
    onWidgetClicked: () -> Unit,
    onWidgetLongClicked: () -> Unit,
    dragModifier: Modifier? = null,
    shouldEnableLongClick: Boolean = true
): Modifier {
    val haptic = LocalHapticFeedback.current
    val touchSlop = LocalViewConfiguration.current.touchSlop

    var longPressConsumed by remember { mutableStateOf(false) }

    var modifier = Modifier
        .then(
            with(lazyItemScope) {
                Modifier.animateItem(placementSpec = null)
            }
        )
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
        .alpha(if (isMenuExpanded) 0.8f else 1f)
        .background(
            shape = RoundedCornerShape(16.dp),
            color = colorResource(id = R.color.dashboard_card_background)
        )

    // Apply click and drag modifiers based on mode
    modifier = modifier.then(
        if (mode is InteractionMode.ReadOnly) {
            Modifier.noRippleClickable { onWidgetClicked() }
        } else {
            if (shouldEnableLongClick && dragModifier != null) {
                // When drag is enabled, use simple click + custom drag detector
                Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (longPressConsumed) {
                            longPressConsumed = false
                        } else {
                            onWidgetClicked()
                        }
                    }
                    .draggableHandle(
                        dragGestureDetector = LongPressWithSlopDetector(
                            touchSlop = touchSlop,
                            onMenuTrigger = {
                                longPressConsumed = true
                                onWidgetLongClicked()
                            },
                            haptic = haptic,
                            onDragStarted = {
                                ViewCompat.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstantsCompat.GESTURE_START
                                )
                            },
                            onDragStopped = {
                                ViewCompat.performHapticFeedback(
                                    view,
                                    HapticFeedbackConstantsCompat.GESTURE_END
                                )
                            }
                        )
                    )
            } else if (shouldEnableLongClick) {
                // When drag is not enabled, use standard combinedClickable
                Modifier.combinedClickable(
                    onClick = { onWidgetClicked() },
                    onLongClick = {
                        onWidgetLongClicked()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            } else {
                Modifier.noRippleClickable { onWidgetClicked() }
            }
        }
    )

    return modifier
}
