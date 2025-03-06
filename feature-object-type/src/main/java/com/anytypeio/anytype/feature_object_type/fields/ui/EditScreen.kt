package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiFieldEditOrNewState
import com.anytypeio.anytype.feature_object_type.fields.UiFieldObjectItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.ui.createDummyFieldDraggableItem
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem

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

    Column(modifier = modifier) {

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            PropertyIcon(
                modifier = Modifier,
                item = field
            )
            when (uiState) {
                is UiFieldEditOrNewState.Visible.Edit -> {
                    NameTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 13.dp, top = 7.dp)
                            .weight(1.0f),
                        value = innerValue,
                        isEditable = true,
                        focusRequester = focusRequester,
                        keyboardController = keyboardController,
                        isNewState = false,
                        onValueChange = { innerValue = it }
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Box(
                        modifier = Modifier
                            .padding(end = 21.dp)
                            .size(40.dp)
                            .noRippleThrottledClickable {

                            }
                    ) {
                        Image(
                            modifier = Modifier
                                //.padding(end = 20.dp)
                                .wrapContentSize()
                                .align(Alignment.Center),
                            painter = painterResource(id = R.drawable.ic_widget_three_dots),
                            contentDescription = "Property menu icon",
                            contentScale = androidx.compose.ui.layout.ContentScale.None,
                        )
                    }
                }

                is UiFieldEditOrNewState.Visible.New -> {
                    NameTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 13.dp, top = 7.dp)
                            .weight(1.0f),
                        value = innerValue,
                        isEditable = true,
                        focusRequester = focusRequester,
                        keyboardController = keyboardController,
                        isNewState = true,
                        onValueChange = { innerValue = it }
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                }

                is UiFieldEditOrNewState.Visible.ViewOnly -> {
                    NameTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 13.dp, top = 7.dp)
                            .weight(1.0f),
                        value = innerValue,
                        isEditable = false,
                        focusRequester = focusRequester,
                        keyboardController = keyboardController,
                        isNewState = false,
                        onValueChange = { innerValue = it }
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Field type section
        FieldTypeSection(
            format = field.format,
            isEditable = uiState is UiFieldEditOrNewState.Visible.Edit || uiState is UiFieldEditOrNewState.Visible.New,
            onTypeClick = { fieldEvent(FieldEvent.OnChangeTypeClick) }
        )
        Divider()

        // Limit object types (only for OBJECT format)
        if (field.format == RelationFormat.OBJECT) {
            when (uiState) {
                is UiFieldEditOrNewState.Visible.Edit, is UiFieldEditOrNewState.Visible.New -> {
                    LimitTypesSectionEditState(
                        objTypes = field.limitObjectTypes,
                        onLimitTypesClick = { fieldEvent(FieldEvent.OnLimitTypesClick) }
                    )
                }

                is UiFieldEditOrNewState.Visible.ViewOnly -> {
                    LimitTypesSectionPreviewState(
                        objTypes = field.limitObjectTypes,
                        onLimitTypesClick = { fieldEvent(FieldEvent.OnLimitTypesClick) }
                    )
                }
            }
            Divider()
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState is UiFieldEditOrNewState.Visible.Edit || uiState is UiFieldEditOrNewState.Visible.New) {
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
private fun RowScope.PropertyIcon(
    modifier: Modifier,
    item: UiFieldsListItem.Item
) {
    val format = item.format
    format.simpleIcon()?.let {
        Image(
            painter = painterResource(id = it),
            contentDescription = "Property icon",
            contentScale = androidx.compose.ui.layout.ContentScale.None,
            modifier = modifier
                .padding(start = 20.dp)
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.shape_primary),
                    shape = RoundedCornerShape(5.dp)
                )
        )
    }
}

@Composable
private fun NameTextField(
    modifier: Modifier,
    value: String,
    isEditable: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    isNewState: Boolean,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val hint = if (isNewState) {
        stringResource(id = R.string.new_property_hint)
    } else {
        stringResource(id = R.string.untitled)
    }

    Column(modifier = modifier) {

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = HeadlineHeading.copy(color = colorResource(id = R.color.text_primary)),
            singleLine = false,
            enabled = isEditable,
            maxLines = 10,

            cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
            modifier = Modifier
                .fillMaxWidth()
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
                        text = hint,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { if (isEditable) onTypeClick() }
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(id = R.string.format),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        if (isEditable) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(format.getPrettyName()),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_secondary)
                )
                Image(
                    modifier = Modifier,
                    painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                    contentDescription = "Change field format icon"
                )
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = stringResource(format.getPrettyName()),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Composable
private fun LimitTypesSectionEditState(
    objTypes: List<UiFieldObjectItem>,
    onLimitTypesClick: () -> Unit
) {
    val size = objTypes.size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { onLimitTypesClick() }
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(end = 16.dp),
                text = stringResource(id = R.string.limit_object_types),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_primary)
            )
            val text = if (size == 0) {
                stringResource(id = R.string.add)
            } else {
                "$size"
            }
            Text(
                modifier = Modifier,
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
            Image(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field format icon"
            )
        }
    }
}

@Composable
private fun LimitTypesSectionPreviewState(
    objTypes: List<UiFieldObjectItem>,
    onLimitTypesClick: () -> Unit
) {
    val size = objTypes.size
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable { onLimitTypesClick() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(),
            text = stringResource(id = R.string.limit_object_types),
            style = BodyRegular,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary)
        )
        if (size > 0) {
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = "$size",
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = stringResource(id = R.string.none),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun MyPreviewEdit() {
    EditFieldContent(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiFieldEditOrNewState.Visible.Edit(
            item = createDummyFieldDraggableItem()
        ),
        fieldEvent = {}
    )
}

@DefaultPreviews
@Composable
private fun MyPreviewNew() {
    EditFieldContent(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiFieldEditOrNewState.Visible.New(
            item = createDummyFieldDraggableItem()
        ),
        fieldEvent = {}
    )
}

@DefaultPreviews
@Composable
private fun MyPreviewViewOnly() {
    EditFieldContent(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiFieldEditOrNewState.Visible.ViewOnly(
            item = createDummyFieldDraggableItem()
        ),
        fieldEvent = {}
    )
}