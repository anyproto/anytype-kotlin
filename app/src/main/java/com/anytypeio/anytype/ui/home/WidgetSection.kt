package com.anytypeio.anytype.ui.home

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.gestures.ReorderableItemModifier
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.dv.DefaultDragAndDropModifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.compositeKey
import com.anytypeio.anytype.ui.widgets.menu.getWidgetMenuItems
import com.anytypeio.anytype.ui.widgets.types.AllContentWidgetCard
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ChatListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.CompactListWidgetList
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.EmptyStateWidgetScreen
import com.anytypeio.anytype.ui.widgets.types.GalleryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ObjectTypesGroupWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import com.anytypeio.anytype.ui.widgets.types.getPrettyName
import kotlinx.coroutines.delay
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
    hideCounters: Boolean = false,
    canToggleChannelPin: Boolean = true,
    favoriteTargets: Set<Id> = emptySet(),
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onSeeAllClicked: (WidgetId, ViewId?) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit,
    onCreateWidget: () -> Unit
) {
    itemsIndexed(
        items = widgets,
        key = { _, item -> item.compositeKey() },
        contentType = { _, item -> sectionType }
    ) { index, item ->
        val animateItemModifier = Modifier.animateItem()
        when (item) {
            is WidgetView.UnreadChatList -> {
                // Unread chat list widgets are rendered separately in WidgetsScreen
                // They should not be part of the reorderable widget sections
            }
            is WidgetView.RecentlyEdited -> {
                // Recently edited widgets are rendered separately in WidgetsScreen
                // They should not be part of the reorderable widget sections
            }
            is WidgetView.Tree -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(
                    item.id,
                    item.sectionType,
                    item.canCreateObjectOfType,
                    item.source,
                    canToggleChannelPin,
                    favoriteTargets
                ) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                val menuItems = remember(item.id, item.sectionType, item.source, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                        isCardMenuExpanded = isCardMenuExpanded,
                        hideCounters = hideCounters
                    )
                }
            }

            is WidgetView.SetOfObjects -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                        onSeeAllClicked = onSeeAllClicked,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded,
                        hideCounters = hideCounters
                    )
                }
            }

            is WidgetView.Gallery -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                        onSeeAllClicked = onSeeAllClicked,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded
                    )
                }
            }

            is WidgetView.ChatList -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                        onSeeAllClicked = onSeeAllClicked,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded,
                        hideCounters = hideCounters
                    )
                }
            }

            is WidgetView.ListOfObjects -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = remember(item.id, item.sectionType, item.canCreateObjectOfType, item.source, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        onCreateElement = onCreateElement,
                        menuItems = menuItems,
                        isCardMenuExpanded = isCardMenuExpanded,
                        hideCounters = hideCounters
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
                val menuItems = remember(item.id, item.sectionType, canToggleChannelPin, favoriteTargets) {
                    item.getWidgetMenuItems(canToggleChannelPin, favoriteTargets)
                }
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.compositeKey(),
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

                    val modifier = ReorderableItemModifier(
                        lazyItemScope = this@itemsIndexed,
                        isMenuExpanded = isCardMenuExpanded.value,
                        isReadOnly = mode is InteractionMode.ReadOnly,
                        view = view,
                        onItemClicked = { onWidgetSourceClicked(item.id) },
                        onItemLongClicked = {
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
            is WidgetView.ObjectTypesGroup -> {
                // ObjectTypesGroup is rendered directly in WidgetsScreen.kt
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
            is WidgetView.InviteMembers -> {
                // Rendered directly in WidgetsScreen.kt
            }
            is WidgetView.Home -> {
                // Rendered directly in WidgetsScreen.kt
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
    onSectionClicked: () -> Unit
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
            style = Title2,
            color = colorResource(id = R.color.text_transparent_secondary)
        )
    }
}

@Composable
fun UnreadSectionHeader(
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
            text = stringResource(R.string.widgets_section_unread),
            style = Title2,
            color = colorResource(id = R.color.text_transparent_secondary)
        )
    }
}

@Composable
fun MyFavoritesSectionHeader(
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
            text = stringResource(R.string.widgets_section_my_favorites),
            style = Title2,
            color = colorResource(id = R.color.text_transparent_secondary)
        )
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
            style = Title2,
            color = colorResource(id = R.color.text_transparent_secondary)
        )
    }
}

/**
 * Displays the unread chat list widget content without tabs.
 * Based on ChatListWidgetCard but simplified for the unread section.
 */
@Composable
fun UnreadChatListWidget(
    item: WidgetView.UnreadChatList,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
) {
    // Wrap in card with rounded background (same as other widgets)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // No header - section header is rendered separately
            // No tabs - unread section doesn't have tabs
            if (item.elements.isNotEmpty()) {
                // Use compact list layout (same as ChatListWidgetCard in compact mode)
                CompactListWidgetList(
                    mode = mode,
                    elements = item.elements,
                    onWidgetElementClicked = onWidgetObjectClicked,
                    onObjectCheckboxClicked = onObjectCheckboxClicked
                )
            }
        }
    }
}

/**
 * Displays the My Favorites widget content — Unread-style compact rows with
 * long-press-to-drag reordering (DROID-4397).
 *
 * Each row wraps the standard compact icon+name layout in a
 * [sh.calvin.reorderable.ReorderableColumn] row. [onReordered] fires on drag
 * settle with the full ordered list of object IDs to persist — the caller
 * should forward this to [com.anytypeio.anytype.presentation.home.HomeScreenViewModel.onMyFavoritesReordered].
 *
 * Renders nothing when [item] has no elements; WidgetsScreen already gates on
 * emptiness, but double-check here too for flicker safety.
 */
@Composable
fun MyFavoritesWidget(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onReordered: (orderedObjectIds: List<Id>) -> Unit,
    /**
     * DROID-4397: strictly-increasing counter that bumps whenever a
     * reorder RPC fails in the VM. Included in the [remember] key below
     * so the optimistic local row order is discarded and re-seeded from
     * the authoritative [item.elements] on failure. A successful RPC
     * causes [item.elements] itself to change (new subscription emission),
     * so no signal is needed for the success path.
     */
    reorderFailedSignal: Int = 0
) {
    if (item.elements.isEmpty()) return
    val view = LocalView.current

    // Local mutable copy so drag visuals happen immediately; the list is
    // re-synced whenever the upstream [item.elements] changes (recomposition
    // on new subscription value) OR when [reorderFailedSignal] ticks.
    val currentElements = remember(item.elements, reorderFailedSignal) {
        mutableStateListOf<WidgetView.SetOfObjects.Element>().apply {
            addAll(item.elements)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        sh.calvin.reorderable.ReorderableColumn(
            modifier = Modifier.fillMaxWidth(),
            list = currentElements,
            onSettle = { fromIndex, toIndex ->
                if (fromIndex == toIndex) return@ReorderableColumn
                val moved = currentElements.removeAt(fromIndex)
                currentElements.add(toIndex, moved)
                onReordered(currentElements.map { it.obj.id })
            },
            onMove = {
                androidx.core.view.ViewCompat.performHapticFeedback(
                    view,
                    androidx.core.view.HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                )
            }
        ) { index, element, _ ->
            MyFavoriteRow(
                element = element,
                mode = mode,
                view = view,
                onClick = { onWidgetObjectClicked(element.obj) },
                onCheckboxClicked = { isChecked ->
                    onObjectCheckboxClicked(element.obj.id, isChecked)
                },
                showDivider = index < currentElements.lastIndex
            )
        }
    }
}

@Composable
private fun sh.calvin.reorderable.ReorderableScope.MyFavoriteRow(
    element: WidgetView.SetOfObjects.Element,
    mode: InteractionMode,
    view: android.view.View,
    onClick: () -> Unit,
    onCheckboxClicked: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .longPressDraggableHandle(
                    onDragStarted = {
                        androidx.core.view.ViewCompat.performHapticFeedback(
                            view,
                            androidx.core.view.HapticFeedbackConstantsCompat.GESTURE_START
                        )
                    },
                    onDragStopped = {
                        androidx.core.view.ViewCompat.performHapticFeedback(
                            view,
                            androidx.core.view.HapticFeedbackConstantsCompat.GESTURE_END
                        )
                    }
                )
                .then(
                    if (mode !is InteractionMode.Edit)
                        Modifier.noRippleClickable(onClick = onClick)
                    else Modifier
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon(
                iconSize = 18.dp,
                icon = element.objectIcon,
                modifier = Modifier.padding(end = 12.dp),
                onTaskIconClicked = onCheckboxClicked,
                iconWithoutBackgroundMaxSize = 200.dp
            )
            val name = when (val n = element.name) {
                is WidgetView.Name.Default -> n.prettyPrintName
                is WidgetView.Name.Bundled -> ""
                WidgetView.Name.Empty -> ""
            }
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.weight(1f)
            )
        }
        if (showDivider) {
            Divider(
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.widget_divider)
            )
        }
    }
}

@Composable
fun RecentlyEditedSectionHeader(
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
            text = stringResource(R.string.widgets_section_recently_edited),
            style = Title2,
            color = colorResource(id = R.color.text_transparent_secondary)
        )
    }
}

/**
 * Displays the recently edited widget content.
 * Similar to UnreadChatListWidget - simple flat list of objects.
 */
@Composable
fun RecentlyEditedWidget(
    item: WidgetView.RecentlyEdited,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
) {
    // Wrap in card with rounded background (same as other widgets)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .background(
                shape = RoundedCornerShape(24.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (item.elements.isNotEmpty()) {
                // Use compact list layout (same as UnreadChatListWidget)
                CompactListWidgetList(
                    mode = mode,
                    elements = item.elements,
                    onWidgetElementClicked = onWidgetObjectClicked,
                    onObjectCheckboxClicked = onObjectCheckboxClicked
                )
            }
        }
    }
}
