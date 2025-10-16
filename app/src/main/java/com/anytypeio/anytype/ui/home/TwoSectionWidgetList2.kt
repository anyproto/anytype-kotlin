package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.WidgetView
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber


@Composable
fun WidgetsScreen(
    viewModel: HomeScreenViewModel,
    paddingValues: PaddingValues
) {

    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val hapticFeedback = rememberReorderHapticFeedback()

    val mode = viewModel.mode.collectAsState().value
    val pinnedWidgets = viewModel.pinnedViews.collectAsState().value
    val typeWidgets = viewModel.typeViews.collectAsState().value

    // UI-local lists for immediate mutation inside onMove (required by the library)
    val pinnedUi = remember { mutableStateListOf<WidgetView>() }
    val typesUi = remember { mutableStateListOf<WidgetView>() }

    // Drag state tracking for debounced commit (since library has no onDragEnd)
    val isDragging = remember { mutableStateOf(false) }
    val lastMoveTick = remember { mutableStateOf(0L) }

    // Keep UI lists in sync with VM when not dragging
    LaunchedEffect(pinnedWidgets) {
        if (!isDragging.value) {
            pinnedUi.clear()
            pinnedUi.addAll(pinnedWidgets)
        }
    }
    LaunchedEffect(typeWidgets) {
        if (!isDragging.value) {
            typesUi.clear()
            typesUi.addAll(typeWidgets)
        }
    }

    // Debounce commit: if there are no moves for 200ms, treat as drag end and persist via VM
    LaunchedEffect(lastMoveTick.value) {
        if (lastMoveTick.value == 0L) return@LaunchedEffect
        delay(200)
        isDragging.value = false
//        viewModel.onPinnedDragEnd()
//        viewModel.onTypeDragEnd()
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromType = from.contentType as? SectionType
            val toType = to.contentType as? SectionType
            if (fromType == null || toType == null || fromType != toType) {
                Timber.d("Ignoring cross-section move: ${from.contentType} -> ${to.contentType}")
            }

            val fromId = from.key as? Id
            val toId = to.key as? Id
            if (fromId == null || toId == null) return@rememberReorderableLazyListState

            isDragging.value = true
            lastMoveTick.value = System.currentTimeMillis()

            when (fromType) {
                SectionType.PINNED -> {
                    val f = pinnedUi.indexOfFirst { it.id == fromId }
                    val t = pinnedUi.indexOfFirst { it.id == toId }
                    if (f != -1 && t != -1 && f != t) {
                        val item = pinnedUi.removeAt(f)
                        pinnedUi.add(t, item)
                        //viewModel.onMovePinned(fromId, toId) // VM tracks pending order / debounced persistence
                    }
                }
                SectionType.TYPES -> {
                    val f = typesUi.indexOfFirst { it.id == fromId }
                    val t = typesUi.indexOfFirst { it.id == toId }
                    if (f != -1 && t != -1 && f != t) {
                        val item = typesUi.removeAt(f)
                        typesUi.add(t, item)
                        //viewModel.onMoveTypes(fromId, toId) // VM tracks pending order / debounced persistence
                    }
                }
                else -> Unit
            }

        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {

            item {
                ReorderableItem(
                    enabled = false,
                    state = reorderableState,
                    key = SECTION_PINNED,
                ) {
                    PinnedSectionHeader(
                        onSectionClicked = viewModel::onSectionPinnedClicked
                    )
                }
            }
            // Pinned widgets section
            renderWidgetSection(
                widgets = pinnedUi,
                reorderableState = reorderableState,
                view = view,
                mode = mode,
                sectionType = SectionType.PINNED,
                onExpand = viewModel::onExpand,
                onWidgetMenuAction = { widget: Id, action: DropDownMenuAction ->
                    viewModel.onDropDownMenuAction(widget, action)
                },
                onWidgetElementClicked = viewModel::onWidgetElementClicked,
                onWidgetSourceClicked = viewModel::onWidgetSourceClicked,
                onWidgetMenuTriggered = viewModel::onWidgetMenuTriggered,
                onToggleExpandedWidgetState = viewModel::onToggleWidgetExpandedState,
                onChangeWidgetView = viewModel::onChangeCurrentWidgetView,
                onObjectCheckboxClicked = viewModel::onObjectCheckboxClicked,
                onCreateElement = viewModel::onCreateWidgetElementClicked,
                onCreateWidget = viewModel::onCreateWidgetClicked
            )

            item {
                ReorderableItem(
                    enabled = false,
                    state = reorderableState,
                    key = SECTION_OBJECT_TYPE,
                ) {
                    SpaceObjectTypesSectionHeader(
                        mode = mode,
                        onCreateNewTypeClicked = viewModel::onCreateNewTypeClicked,
                        onSectionClicked = viewModel::onSectionTypesClicked
                    )
                }
            }

            // Type widgets section
            renderWidgetSection(
                widgets = typesUi,
                reorderableState = reorderableState,
                view = view,
                mode = mode,
                sectionType = SectionType.TYPES,
                onExpand = viewModel::onExpand,
                onWidgetMenuAction = { widget: Id, action: DropDownMenuAction ->
                    viewModel.onDropDownMenuAction(widget, action)
                },
                onWidgetElementClicked = viewModel::onWidgetElementClicked,
                onWidgetSourceClicked = viewModel::onWidgetSourceClicked,
                onWidgetMenuTriggered = viewModel::onWidgetMenuTriggered,
                onToggleExpandedWidgetState = viewModel::onToggleWidgetExpandedState,
                onChangeWidgetView = viewModel::onChangeCurrentWidgetView,
                onObjectCheckboxClicked = viewModel::onObjectCheckboxClicked,
                onCreateElement = viewModel::onCreateWidgetElementClicked,
                onCreateWidget = viewModel::onCreateWidgetClicked
            )

            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp)
                )
            }
        }
    }
}