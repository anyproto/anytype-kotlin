package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
    modifier: Modifier,
    uiFieldEditOrNewState: UiFieldEditOrNewState,
    fieldEvent: (FieldEvent) -> Unit
) {
    if (uiFieldEditOrNewState is UiFieldEditOrNewState.Visible) {
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            modifier = modifier,
            dragHandle = { DragHandle() },
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_primary),
            shape = RoundedCornerShape(16.dp),
            sheetState = bottomSheetState,
            onDismissRequest = { fieldEvent(FieldEvent.OnFieldEditScreenDismiss) },
        ) {
            EditFieldContent(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiFieldEditOrNewState,
                fieldEvent = fieldEvent
            )
        }
    }
}

@Composable
private fun EditFieldContent(
    modifier: Modifier,
    uiState: UiFieldEditOrNewState.Visible,
    fieldEvent: (FieldEvent) -> Unit
) {

    val field = uiState.item
    var innerValue by remember(field.fieldTitle) { mutableStateOf(field.fieldTitle) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isEditable = field.isEditableField

    val title = when (uiState) {
        is UiFieldEditOrNewState.Visible.Edit -> stringResource(R.string.object_type_fields_edit_field)
        is UiFieldEditOrNewState.Visible.New -> stringResource(R.string.object_type_fields_new_field)
        is UiFieldEditOrNewState.Visible.ViewOnly -> stringResource(R.string.object_type_fields_preview_field)
    }

    Column(modifier = modifier) {
        // Header title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = Title2,
                color = colorResource(id = R.color.text_primary)
            )
        }

        // Name text field
        NameTextField(
            modifier = Modifier.fillMaxWidth(),
            value = innerValue,
            isEditable = isEditable,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            onValueChange = { innerValue = it }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Divider()

        // Field type section
        FieldTypeSection(
            format = field.format,
            isEditable = isEditable,
            onTypeClick = { fieldEvent(FieldEvent.OnChangeTypeClick) }
        )
        Divider()

        // Limit object types (only for OBJECT format)
        if (field.format == RelationFormat.OBJECT) {
            LimitTypesSection(
                objTypes = field.limitObjectTypes,
                isEditable = isEditable,
                onLimitTypesClick = { fieldEvent(FieldEvent.OnLimitTypesClick) }
            )
            Divider()
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isEditable) {
            ButtonPrimary(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.object_type_fields_btn_save),
                onClick = {
                    fieldEvent(
                        FieldEvent.OnSaveButtonClicked(
                            name = innerValue,
                            format = field.format,
                            limitObjectTypes = field.limitObjectTypes.map { it.id }
                        )
                    )
                },
                size = ButtonSize.Large
            )
        }
    }
}

@Composable
private fun NameTextField(
    modifier: Modifier,
    value: String,
    isEditable: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.name),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = HeadlineHeading.copy(color = colorResource(id = R.color.text_primary)),
            singleLine = true,
            enabled = isEditable,
            cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 20.dp, top = 6.dp, end = 20.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { /* You can handle focus changes here if needed */ },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
                onValueChange(value)
            },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.untitled),
                        style = HeadlineHeading,
                        color = colorResource(id = R.color.text_tertiary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun FieldTypeSection(
    format: RelationFormat,
    isEditable: Boolean,
    onTypeClick: () -> Unit
) {
    val icon = format.simpleIcon()
    SectionItem(
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
            .noRippleThrottledClickable { if (isEditable) onTypeClick() }
    ) {
        if (icon != null) {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart),
                painter = painterResource(id = icon),
                contentDescription = "Relation format icon"
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
        if (isEditable) {
            Image(
                modifier = Modifier.align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field format icon"
            )
        }
    }
}

@Composable
private fun LimitTypesSection(
    objTypes: List<UiFieldObjectItem>,
    isEditable: Boolean,
    onLimitTypesClick: () -> Unit
) {
    val size = objTypes.size
    SectionItem(
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
            .noRippleThrottledClickable { if (isEditable) onLimitTypesClick() }
    ) {
        if (objTypes.isNotEmpty()) {
            Row(modifier = Modifier.align(Alignment.CenterStart)) {
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
                        text = "   +${size - 1}",
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
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
        if (isEditable) {
            Image(
                modifier = Modifier.align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field object types icon"
            )
        }
    }
}

@Composable
private fun SectionItem(modifier: Modifier, text: String) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp, bottom = 8.dp)
                .align(Alignment.BottomStart),
            text = text,
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@DefaultPreviews
@Composable
private fun MyPreview() {
    EditFieldScreen(
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(),
        uiFieldEditOrNewState = UiFieldEditOrNewState.Visible.ViewOnly(
            item = createDummyFieldDraggableItem(
                isEditableField = false
            )
        ),
        fieldEvent = {}
    )
}