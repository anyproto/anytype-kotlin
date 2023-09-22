package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Delete
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Dismiss
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.DoneMode
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.Edit
import com.anytypeio.anytype.presentation.sets.ViewersWidgetUi.Action.EditMode
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ViewersWidget(
    state: ViewersWidgetUi,
    action: (ViewersWidgetUi.Action) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    LaunchedEffect(key1 = state, block = {
        if (state.showWidget) sheetState.show() else sheetState.hide()
    })

    DisposableEffect(
        key1 = (sheetState.targetValue == ModalBottomSheetValue.Hidden
                && sheetState.isVisible)
    ) {
        onDispose {
            if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
                action(Dismiss)
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetShape = RoundedCornerShape(16.dp),
        sheetContent = {
            ViewersWidgetContent(state, action)
        },
        content = {
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .noRippleThrottledClickable { action.invoke(Dismiss) }
        }
    )
}

@Composable
private fun ViewersWidgetContent(
    state: ViewersWidgetUi,
    action: (ViewersWidgetUi.Action) -> Unit
) {
    val currentState by rememberUpdatedState(state)

    val views = remember { mutableStateOf(currentState.items) }
    views.value = currentState.items

    val isEditing = remember { mutableStateOf(currentState.isEditing) }
    isEditing.value = currentState.isEditing
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 40.dp,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
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
                .padding(bottom = 16.dp)
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
                        ActionText(
                            text = stringResource(id = R.string.done),
                            click = { action(DoneMode) }
                        )
                    } else {
                        ActionText(
                            text = stringResource(id = R.string.edit),
                            click = { action(EditMode) }
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        text = stringResource(R.string.views),
                        style = Title1,
                        color = colorResource(R.color.text_primary)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .noRippleThrottledClickable {
                            action.invoke(ViewersWidgetUi.Action.Plus)
                        }
                ) {
                    Image(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 12.dp,
                            bottom = 12.dp,
                            end = 16.dp
                        ),
                        painter = painterResource(id = R.drawable.ic_default_plus),
                        contentDescription = null
                    )
                }
            }

            val lazyListState = rememberReorderableLazyListState(
                onMove = { from, to ->
                    views.value = views.value.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                },
                onDragEnd = { from, to ->
                    action(
                        ViewersWidgetUi.Action.OnMove(
                            currentViews = views.value,
                            from = from,
                            to = to
                        )
                    )
                }
            )

            LazyColumn(
                state = lazyListState.listState,
                modifier = Modifier
                    .reorderable(lazyListState)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                itemsIndexed(
                    items = views.value,
                    key = { _, item -> item.id }) { index, view ->
                    ReorderableItem(
                        reorderableState = lazyListState,
                        key = view.id
                    ) { isDragging ->
                        val currentItem = LocalView.current
                        if (isDragging) {
                            currentItem.isHapticFeedbackEnabled = true
                            currentItem.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                        val alpha =
                            animateFloatAsState(if (isDragging) 0.8f else 1.0f, label = "")
                        ConstraintLayout(
                            modifier = Modifier
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
                                            if (isEditing.value && !view.isActive) Visibility.Visible else Visibility.Gone
                                    },
                                painter = painterResource(id = R.drawable.ic_relation_delete),
                                contentDescription = "Delete view"
                            )
                            Image(
                                modifier = Modifier
                                    .detectReorder(lazyListState)
                                    .constrainAs(dnd) {
                                        end.linkTo(parent.end)
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        visibility =
                                            if (isEditing.value) Visibility.Visible else Visibility.Gone

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
                                            if (isEditing.value) Visibility.Visible else Visibility.Gone
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
                                            if (!isEditing.value && view.isUnsupported) Visibility.Visible else Visibility.Gone
                                    },
                                text = stringResource(id = R.string.unsupported),
                                color = colorResource(id = R.color.text_secondary),
                                style = Caption2Regular,
                                textAlign = TextAlign.Left
                            )
                            Text(
                                modifier = Modifier
                                    .noRippleThrottledClickable {
                                        if (!isEditing.value) {
                                            action.invoke(
                                                ViewersWidgetUi.Action.SetActive(
                                                    view.id
                                                )
                                            )
                                        }
                                    }
                                    .constrainAs(text) {
                                        start.linkTo(
                                            delete.end,
                                            margin = 12.dp,
                                            goneMargin = 0.dp
                                        )
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        end.linkTo(
                                            unsupported.start,
                                            margin = 8.dp,
                                            goneMargin = 38.dp
                                        )
                                        width = Dimension.fillToConstraints
                                    },
                                text = view.name,
                                color = colorResource(id = if (view.isActive) R.color.text_primary else R.color.glyph_active),
                                style = HeadlineSubheading,
                                textAlign = TextAlign.Left,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (index != views.value.size - 1) {
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionText(text: String, click: () -> Unit) {
    Text(
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 12.dp,
                bottom = 12.dp,
                end = 16.dp
            )
            .noRippleThrottledClickable { click() },
        text = text,
        style = BodyCalloutRegular,
        color = colorResource(id = R.color.glyph_active),
        textAlign = TextAlign.Center
    )
}