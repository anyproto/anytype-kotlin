package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Delete
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Dismiss
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.DoneMode
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Edit
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.EditMode
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ViewersWidget(
    state: ViewersWidgetUi,
    action: (ViewersWidgetUi.Action) -> Unit,
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (state.showWidget) {
        ModalBottomSheet(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.ime)
                .padding(start = 8.dp, end = 8.dp, bottom = 30.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { action(Dismiss) },
            sheetState = bottomSheetState,
            dragHandle = { DragHandle() },
            content = {
                ViewersWidgetContent(
                    modifier = Modifier.padding(bottom = 168.dp),
                    state = state,
                    action = action
                )
            }
        )
    }
}

@Composable
fun DragHandle() {
    Column {
        Spacer(modifier = Modifier.height(6.dp))
        Dragger()
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun ViewersWidgetContent(
    modifier: Modifier,
    state: ViewersWidgetUi,
    action: (ViewersWidgetUi.Action) -> Unit
) {

    val view = LocalView.current
    val lastFromIndex = remember { mutableStateOf<Int?>(null) }
    val lastToIndex = remember { mutableStateOf<Int?>(null) }

    val lazyListState = rememberLazyListState()

    val views = remember { mutableStateListOf<ViewerView>() }
    views.swapList(state.items)

    val onDragStoppedHandler = {
        val from = lastFromIndex.value
        val to = lastToIndex.value
        if (from != null && to != null && from != to) {
            action(
                ViewersWidgetUi.Action.OnMove(
                    currentViews = views,
                    from = from,
                    to = to
                )
            )
        }
        // Reset after firing
        lastFromIndex.value = null
        lastToIndex.value = null
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        lastFromIndex.value = from.index
        lastToIndex.value = to.index

        val newList = views.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        views.swapList(newList)

        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Header(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            isEditingMode = state.isEditing,
            isReadOnlyState = state.isReadOnly,
            action = action
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            items(
                count = views.size,
                key = { index -> views[index].id },
            ) { index ->
                ReorderableItem(reorderableLazyListState, key = views[index].id) { isDragging ->
                    val currentItem = LocalView.current
                    if (isDragging) {
                        currentItem.isHapticFeedbackEnabled = true
                        currentItem.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    Item(
                        dndModifier = DefaultDragAndDropModifier(view, onDragStoppedHandler),
                        modifier = Modifier,
                        isDragging = isDragging,
                        isEditing = state.isEditing,
                        action = action,
                        view = views[index]
                    )
                }
                if (index != views.size - 1) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun Item(
    dndModifier: Modifier = Modifier,
    modifier: Modifier,
    isDragging: Boolean,
    isEditing: Boolean,
    action: (ViewersWidgetUi.Action) -> Unit,
    view: ViewerView
) {
    val alpha = animateFloatAsState(if (isDragging) 0.8f else 1.0f, label = "")
    ConstraintLayout(
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp)
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow
                )
            )
            .alpha(alpha.value)
    ) {
        val (delete, text, edit, dnd, unsupported) = createRefs()
        Image(
            modifier = Modifier
                .noRippleThrottledClickable {
                    action.invoke(Delete(view.id))
                }
                .constrainAs(delete) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    visibility =
                        if (isEditing && !view.isActive) Visibility.Visible else Visibility.Gone
                },
            painter = painterResource(id = R.drawable.ic_relation_delete),
            contentDescription = "Delete view"
        )
        Image(
            modifier = dndModifier
                .constrainAs(dnd) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    visibility =
                        if (isEditing) Visibility.Visible else Visibility.Gone

                },
            painter = painterResource(id = R.drawable.ic_dnd),
            contentDescription = "Dnd view"
        )
        Image(
            modifier = Modifier
                .noRippleThrottledClickable {
                    action.invoke(Edit(id = view.id))
                }
                .constrainAs(edit) {
                    end.linkTo(dnd.start, margin = 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    visibility =
                        if (isEditing) Visibility.Visible else Visibility.Gone
                },
            painter = painterResource(id = R.drawable.ic_edit_24),
            contentDescription = "Edit view"
        )
        Text(
            modifier = Modifier
                .constrainAs(unsupported) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(edit.start)
                    visibility =
                        if (!isEditing && view.isUnsupported) Visibility.Visible else Visibility.Gone
                },
            text = stringResource(id = R.string.unsupported),
            color = colorResource(id = R.color.text_secondary),
            style = Caption2Regular,
            textAlign = TextAlign.Left
        )
        Text(
            modifier = Modifier
                .noRippleThrottledClickable {
                    if (!isEditing) {

                        action.invoke(
                            ViewersWidgetUi.Action.SetActive(
                                id = view.id, type = view.type
                            )
                        )
                    }
                }
                .constrainAs(text) {
                    start.linkTo(
                        delete.end, margin = 12.dp, goneMargin = 0.dp
                    )
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(
                        unsupported.start, margin = 8.dp, goneMargin = 38.dp
                    )
                    width = Dimension.fillToConstraints
                },
            text = view.name.ifBlank { stringResource(id = R.string.untitled) },
            color = colorResource(id = if (view.isActive) R.color.text_primary else R.color.glyph_active),
            style = HeadlineSubheading,
            textAlign = TextAlign.Left,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier,
    isReadOnlyState: Boolean,
    isEditingMode: Boolean,
    action: (ViewersWidgetUi.Action) -> Unit
) {
    Box(modifier = modifier) {
        if (!isReadOnlyState) {
            ActionButtons(
                modifier = Modifier.align(Alignment.CenterStart),
                isEditingMode = isEditingMode,
                action = action
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.views),
            style = Title1,
            color = colorResource(R.color.text_primary)
        )
        if (!isReadOnlyState) {
            PlusButton(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .noRippleThrottledClickable {
                        action.invoke(ViewersWidgetUi.Action.Plus)
                    }
            )
        }
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier,
    isEditingMode: Boolean,
    action: (ViewersWidgetUi.Action) -> Unit
) {
    if (isEditingMode) {
        ActionText(
            modifier = modifier,
            text = stringResource(id = R.string.done),
            click = { action(DoneMode) }
        )
    } else {
        ActionText(
            modifier = modifier,
            text = stringResource(id = R.string.edit),
            click = { action(EditMode) }
        )
    }
}

@Composable
private fun BoxScope.PlusButton(
    modifier: Modifier
) {
    Image(
        modifier = modifier.padding(
            start = 16.dp,
            top = 12.dp,
            bottom = 12.dp,
            end = 16.dp
        ),
        painter = painterResource(id = R.drawable.ic_default_plus),
        contentDescription = null
    )
}

@Composable
private fun ActionText(modifier: Modifier, text: String, click: () -> Unit) {
    Text(
        modifier = modifier
            .padding(
                start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp
            )
            .noRippleThrottledClickable { click() },
        text = text,
        style = BodyRegular,
        color = colorResource(id = R.color.glyph_active),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ReorderableCollectionItemScope.DefaultDragAndDropModifier(
    view: View,
    onDragStopped: () -> Unit
): Modifier {
    return Modifier.longPressDraggableHandle(
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
            onDragStopped()
        }
    )
}
