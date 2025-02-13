package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.getPrettyName
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiFieldEditOrNewState
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.feature_object_type.ui.createDummyFieldDraggableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldScreen(
    uiFieldEditOrNewState: UiFieldEditOrNewState,
    fieldEvent: (FieldEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (uiFieldEditOrNewState is UiFieldEditOrNewState.Visible) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            dragHandle = { DragHandle() },
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_primary),
            shape = RoundedCornerShape(16.dp),
            sheetState = bottomSheetState,
            onDismissRequest = {
                fieldEvent(FieldEvent.OnFieldEditScreenDismiss)
            },
        ) {
            EditFieldContainer(
                uiFieldEditOrNewState = uiFieldEditOrNewState,
                fieldEvent = fieldEvent
            )
        }
    }
}

@Composable
private fun ColumnScope.EditFieldContainer(
    uiFieldEditOrNewState: UiFieldEditOrNewState.Visible,
    fieldEvent: (FieldEvent) -> Unit
    ) {

    var innerValue by remember(uiFieldEditOrNewState.item.fieldTitle) {
        mutableStateOf(uiFieldEditOrNewState.item.fieldTitle)
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isEditableField = uiFieldEditOrNewState.item.isEditableField

    val title = when (uiFieldEditOrNewState) {
        is UiFieldEditOrNewState.Visible.Edit -> {
            stringResource(R.string.object_type_fields_edit_field)
        }

        is UiFieldEditOrNewState.Visible.New -> {
            stringResource(R.string.object_type_fields_new_field)
        }

        is UiFieldEditOrNewState.Visible.ViewOnly -> {
            stringResource(R.string.object_type_fields_preview_field)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Title2,
            color = colorResource(R.color.text_primary)
        )
    }
    NameTextField(
        innerValue = innerValue,
        isEditableField = isEditableField,
        focusRequester = focusRequester,
        keyboardController = keyboardController
    ) {
        innerValue = it
    }
    Spacer(modifier = Modifier.height(10.dp))
    Divider()
    FieldType(
        format = uiFieldEditOrNewState.item.format,
        isEditableField = isEditableField,
        fieldEvent = fieldEvent
    )
    Divider()
    if (uiFieldEditOrNewState.item.format == RelationFormat.OBJECT) {
        LimitTypes(
            objTypes = uiFieldEditOrNewState.item.limitObjectTypes,
            isEditableField = isEditableField,
            fieldEvent = fieldEvent
        )
        Divider()
    }
    Spacer(modifier = Modifier.height(14.dp))
    if (isEditableField) {
        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(R.string.object_type_fields_btn_save),
            onClick = {
                val name = innerValue
                val format = uiFieldEditOrNewState.item.format
                val ids = uiFieldEditOrNewState.item.limitObjectTypes.map { it.id }
                fieldEvent(
                    FieldEvent.OnSaveButtonClicked(
                        name = name,
                        format = format,
                        limitObjectTypes = ids
                    )
                )
            },
            size = ButtonSize.Large
        )
    }
}

@Composable
fun ColumnScope.NameTextField(
    innerValue: String,
    isEditableField: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onNameUpdate: (String) -> Unit
) {

    val focusManager = LocalFocusManager.current

    val strokeColorActive = colorResource(id = R.color.widget_edit_view_stroke_color_active)
    val strokeColorInactive = colorResource(id = R.color.widget_edit_view_stroke_color_inactive)

    val strokeColor = remember {
        mutableStateOf(strokeColorInactive)
    }

    val strokeWidthActive = 2.dp
    val strokeWidthInactive = 1.dp

    val strokeWidth = remember {
        mutableStateOf(strokeWidthInactive)
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        text = stringResource(id = R.string.name),
        style = Caption1Regular,
        color = colorResource(id = R.color.text_secondary)
    )

    BasicTextField(
        value = innerValue,
        onValueChange = {
            onNameUpdate(it)
        },
        textStyle = HeadlineHeading.copy(color = colorResource(id = R.color.text_primary)),
        singleLine = true,
        enabled = isEditableField,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 20.dp, top = 6.dp, end = 20.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    strokeColor.value = strokeColorActive
                    strokeWidth.value = strokeWidthActive
                } else {
                    strokeColor.value = strokeColorInactive
                    strokeWidth.value = strokeWidthInactive
                }
            },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            focusManager.clearFocus()
            onNameUpdate(innerValue)
        },
        decorationBox = { innerTextField ->
            if (innerValue.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.untitled),
                    style = HeadlineHeading,
                    color = colorResource(id = R.color.text_tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun ColumnScope.FieldType(
    format: RelationFormat,
    isEditableField: Boolean,
    fieldEvent: (FieldEvent) -> Unit
) {
    val icon = format.simpleIcon()
    Section(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        text = stringResource(id = R.string.type)
    )
    Divider()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable {
                if (isEditableField) {
                    fieldEvent(FieldEvent.OnChangeTypeClick)
                }
            }
    ) {
        if (icon != null) {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart),
                painter = painterResource(id = icon),
                contentDescription = "Relation format icon",
            )
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp)
                .align(Alignment.CenterStart),
            text = stringResource(format.getPrettyName()),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isEditableField) {
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field format icon",
            )
        }
    }
}

@Composable
private fun ColumnScope.LimitTypes(
    objTypes: List<UiFieldObjectItem>,
    isEditableField: Boolean,
    fieldEvent: (FieldEvent) -> Unit
) {
    val size = objTypes.size
    Section(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        text = stringResource(id = R.string.limit_object_types)
    )
    Divider()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable {
                //todo this is not correct
                // - user should be able to see all object types(open types screen in preview mode)
                // but only in case when there are >1 types
                if (isEditableField) {
                    fieldEvent(FieldEvent.OnLimitTypesClick)
                }
            }
    ) {
        if (objTypes.isNotEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                ListWidgetObjectIcon(
                    modifier = Modifier.size(20.dp),
                    icon = objTypes.first().icon,
                    backgroundColor = R.color.transparent_black
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp),
                    text = objTypes.first().title.take(20),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (size > 1) {
                    Text(
                        modifier = Modifier,
                        text = "   +${size - 1}",
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                text = stringResource(R.string.none),
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isEditableField)  {
            Image(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field object types icon",
            )
        }
    }
}

@Composable
private fun Section(modifier: Modifier, text: String) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp, bottom = 8.dp)
                .align(Alignment.BottomStart),
            text = text,
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
        )
    }
}

@DefaultPreviews
@Composable
private fun MyPreview() {
    EditFieldScreen(
        uiFieldEditOrNewState = UiFieldEditOrNewState.Visible.Edit(
            item = createDummyFieldDraggableItem()
        ),
        fieldEvent = {}
    )
}

@DefaultPreviews
@Composable
private fun MyPreviewOnlyPreview() {
    EditFieldScreen(
        uiFieldEditOrNewState = UiFieldEditOrNewState.Visible.ViewOnly(
            item = createDummyFieldDraggableItem(
                isEditableField = false
            )
        ),
        fieldEvent = {}
    )
}