package com.anytypeio.anytype.feature_object_type.ui.fields

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
import com.anytypeio.anytype.feature_object_type.models.UiFieldEditOrNewState
import com.anytypeio.anytype.feature_object_type.models.UiFieldObjectItem
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldScreen(
    modifier: Modifier,
    uiFieldEditOrNewState: UiFieldEditOrNewState.Visible,
    fieldEvent: (FieldEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        dragHandle = {
            DragHandle()
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_primary),
        shape = RoundedCornerShape(16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {},
    ) {
        EditFieldContainer(
            uiFieldEditOrNewState = uiFieldEditOrNewState,
            fieldEvent = fieldEvent
        )
    }
}

@Composable
private fun ColumnScope.EditFieldContainer(
    uiFieldEditOrNewState: UiFieldEditOrNewState.Visible,
    fieldEvent: (FieldEvent) -> Unit
    ) {

    var innerValue by remember(uiFieldEditOrNewState.title) { mutableStateOf(uiFieldEditOrNewState.title) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val title = when (uiFieldEditOrNewState) {
        is UiFieldEditOrNewState.Visible.Edit -> {
            stringResource(R.string.object_type_fields_edit_field)
        }

        is UiFieldEditOrNewState.Visible.New -> {
            stringResource(R.string.object_type_fields_new_field)
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
        focusRequester = focusRequester,
        keyboardController = keyboardController
    ) {
        innerValue = it
    }
    Spacer(modifier = Modifier.height(10.dp))
    Divider()
    FieldType(
        format = uiFieldEditOrNewState.format,
        fieldTitle = title,
        fieldEvent = fieldEvent
    )
    Divider()
    if (uiFieldEditOrNewState.format == RelationFormat.OBJECT) {
        LimitTypes(
            objTypes = uiFieldEditOrNewState.limitObjectTypes,
            fieldEvent = fieldEvent
        )
        Divider()
    }
    Spacer(modifier = Modifier.height(14.dp))
    ButtonPrimary(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        text = stringResource(R.string.object_type_fields_btn_save),
        onClick = {
            val name = innerValue
            val format = uiFieldEditOrNewState.format
            val ids = uiFieldEditOrNewState.limitObjectTypes.map { it.id }
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

@Composable
fun ColumnScope.NameTextField(
    innerValue: String,
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
        enabled = true,
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
    fieldTitle: String,
    format: RelationFormat,
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
                fieldEvent(FieldEvent.OnChangeTypeClick)
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
            text = fieldTitle,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterEnd),
            painter = painterResource(id = R.drawable.ic_arrow_forward_24),
            contentDescription = "Relation format icon",
        )
    }
}

@Composable
private fun ColumnScope.LimitTypes(
    objTypes: List<UiFieldObjectItem>,
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
                fieldEvent(FieldEvent.OnLimitTypesClick)
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
        Image(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterEnd),
            painter = painterResource(id = R.drawable.ic_arrow_forward_24),
            contentDescription = "Relation format icon",
        )
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
        modifier = Modifier,
        uiFieldEditOrNewState = UiFieldEditOrNewState.Visible.Edit(
            title = "Tag",
            format = RelationFormat.OBJECT,
            limitObjectTypes = listOf(
                UiFieldObjectItem(
                    id = "1",
                    key = "1",
                    title = "Page",
                    icon = ObjectIcon.Empty.ObjectType
                ),
                UiFieldObjectItem(
                    id = "2",
                    key = "2",
                    title = "Note",
                    icon = ObjectIcon.Empty.ObjectType
                )
            )
        ),
        fieldEvent = {}
    )
}