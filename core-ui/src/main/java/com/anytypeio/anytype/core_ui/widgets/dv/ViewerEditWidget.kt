package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.sets.ViewEditAction
import com.anytypeio.anytype.presentation.sets.ViewerEditWidgetUi
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ViewerEditWidget(
    state: ViewerEditWidgetUi,
    action: (ViewEditAction) -> Unit
) {

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    LaunchedEffect(key1 = state, block = {
        if (state is ViewerEditWidgetUi.Data && state.showWidget) sheetState.show() else sheetState.hide()
    })

    DisposableEffect(
        key1 = (sheetState.targetValue == ModalBottomSheetValue.Hidden
                && sheetState.isVisible)
    ) {
        onDispose {
            if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
                action(ViewEditAction.Dismiss)
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetShape = RoundedCornerShape(16.dp),
        sheetContent = {
            if (state is ViewerEditWidgetUi.Data) {
                ViewerEditWidgetContent(state, action)
            }
        },
        content = {
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .noRippleThrottledClickable { action.invoke(ViewEditAction.Dismiss) }
        }
    )

}

@Composable
fun ViewerEditWidgetContent(
    state: ViewerEditWidgetUi.Data,
    action: (ViewEditAction) -> Unit
) {

    val currentState by rememberUpdatedState(state)

    var currentCoordinates: Rect by remember { mutableStateOf(Rect.Zero) }

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
                .padding(bottom = 16.dp, top = 8.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    val text = if (currentState.isNewMode) {
                        stringResource(R.string.new_view)
                    } else {
                        stringResource(R.string.edit_view)
                    }
                    Text(
                        text = text,
                        style = Title1,
                        color = colorResource(R.color.text_primary)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .onGloballyPositioned { coordinates ->
                            if (coordinates.isAttached) {
                                with(coordinates.boundsInRoot()) {
                                    currentCoordinates = this
                                }
                            } else {
                                currentCoordinates = Rect.Zero
                            }
                        }
                        .noRippleThrottledClickable {
                            action.invoke(ViewEditAction.More)
                        },
                    ) {
                    Image(
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 12.dp
                        ),
                        painter = painterResource(id = R.drawable.ic_style_toolbar_more),
                        contentDescription = null
                    )
                }
            }
            NameTextField(state = currentState, action = action)
            Spacer(modifier = Modifier.height(12.dp))
            if (state.isDefaultObjectTypeEnabled) {
                ColumnItem(
                    title = stringResource(id = R.string.default_object),
                    value = state.defaultObjectType?.name.orEmpty(),
                    isEnable = true
                ) {
                    action(ViewEditAction.DefaultObjectType(id = state.id))
                }
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            } else {
                val templateName = if (state.defaultTemplateId == DEFAULT_TEMPLATE_ID_BLANK) {
                    stringResource(id = R.string.blank)
                } else {
                    state.defaultTemplateName.orEmpty()
                }
                ColumnItem(
                    title = stringResource(id = R.string.default_template),
                    value = templateName,
                    isEnable = true
                ) {
                    action(ViewEditAction.DefaultTemplate(id = state.id))
                }
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            }

            val layoutValue = when (state.layout) {
                DVViewerType.LIST -> stringResource(id = R.string.view_list)
                DVViewerType.GRID -> stringResource(id = R.string.view_grid)
                DVViewerType.GALLERY -> stringResource(id = R.string.view_gallery)
                DVViewerType.BOARD -> stringResource(id = R.string.view_kanban)
                else -> stringResource(id = R.string.none)
            }
            ColumnItem(
                title = stringResource(id = R.string.layout),
                value = layoutValue
            ) { action(ViewEditAction.Layout(id = state.id)) }
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val relationsValue = when (state.relations.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.relations[0]
                else -> stringResource(id = R.string.num_applied, state.relations.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.relations),
                value = relationsValue
            ) { action(ViewEditAction.Relations(id = state.id)) }

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val filtersValue = when (state.filters.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.filters[0]
                else -> stringResource(id = R.string.num_applied, state.filters.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.filter),
                value = filtersValue
            ) { action(ViewEditAction.Filters(id = state.id)) }

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            val sortsValue = when (state.sorts.size) {
                0 -> stringResource(id = R.string.none)
                1 -> state.sorts[0]
                else -> stringResource(id = R.string.num_applied, state.sorts.size)
            }
            ColumnItem(
                title = stringResource(id = R.string.sort),
                value = sortsValue
            ) { action(ViewEditAction.Sorts(id = state.id)) }
        }
        ViewerEditMoreMenu(
            show = currentState.showMore,
            currentState = currentState,
            action = action,
            coordinates = currentCoordinates
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NameTextField(
    state: ViewerEditWidgetUi.Data,
    action: (ViewEditAction) -> Unit
) {
    var innerValue by remember(state.name) { mutableStateOf(state.name) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                width = 1.dp,
                color = Color(0xFFE3E3E3),
                shape = RoundedCornerShape(size = 10.dp)
            )
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.name),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_secondary)
        )
        BasicTextField(
            value = innerValue,
            onValueChange = { innerValue = it },
            textStyle = Title1,
            singleLine = true,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 0.dp, top = 2.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
                action.invoke(
                    ViewEditAction.UpdateName(
                        id = state.id,
                        name = innerValue
                    )
                )
            }
        )
    }
}

@Composable
fun ColumnItem(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    title: String,
    isEnable: Boolean = true,
    value: String,
    arrow : Painter = painterResource(id = R.drawable.ic_arrow_forward),
    onClick: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleThrottledClickable(onClick = onClick)
            .alpha(if (isEnable) 1f else 0.2f)
    ) {
        val (titleRef, valueRef, iconRef) = createRefs()
        val rightGuideline = createGuidelineFromStart(0.5f)
        Text(
            modifier = Modifier
                .wrapContentWidth()
                .constrainAs(titleRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            text = title,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
        )
        Image(
            modifier = imageModifier
                .constrainAs(iconRef) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            painter = arrow,
            contentDescription = "Arrow icon",
        )
        Text(
            modifier = Modifier
                .constrainAs(valueRef) {
                    start.linkTo(rightGuideline)
                    end.linkTo(iconRef.start, margin = 6.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            text = value,
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewViewerEditWidget() {
    val state = ViewerEditWidgetUi.Data(
        showWidget = true,
        showMore = false,
        isNewMode = true,
        name = "Artist",
        defaultObjectType = ObjectWrapper.Type(buildMap { put("name", "Name") }),
        filters = listOf(),
        sorts = emptyList(),
        layout = DVViewerType.LIST,
        relations = listOf(),
        id = "1",
        defaultTemplateId = "1",
        isDefaultObjectTypeEnabled = false,
        defaultTemplateName = null
    )
    ViewerEditWidget(state = state, action = {})
}