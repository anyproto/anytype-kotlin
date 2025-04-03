package com.anytypeio.anytype.feature_object_type.ui.create

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

sealed class UiTypeSetupTitleAndIconState {

    abstract val icon: ObjectIcon.TypeIcon.Default

    data class CreateNewType(
        override val icon: ObjectIcon.TypeIcon.Default,
        val initialTitle: String = "",
        val initialPlural: String = ""
    ) : UiTypeSetupTitleAndIconState()

    data class EditType(
        override val icon: ObjectIcon.TypeIcon.Default,
        val initialTitle: String = "",
        val initialPlural: String = ""
    ) : UiTypeSetupTitleAndIconState()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTypeTitlesAndIconScreen(
    uiState: UiTypeSetupTitleAndIconState,
    modifier: Modifier = Modifier,
    onTitleChanged: (String) -> Unit,
    onPluralChanged: (String) -> Unit,
    onIconClicked: () -> Unit,
    onDismiss: () -> Unit,
    onButtonClicked: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(11.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onDismiss()
        }
    ) {
        CreateNewTypeScreenContent(
            uiState = uiState,
            onTitleChanged = onTitleChanged,
            onPluralChanged = onPluralChanged,
            onIconClicked = onIconClicked,
            onButtonClicked = onButtonClicked
        )
    }
}

@Composable
private fun ColumnScope.CreateNewTypeScreenContent(
    uiState: UiTypeSetupTitleAndIconState,
    onTitleChanged: (String) -> Unit,
    onPluralChanged: (String) -> Unit,
    onIconClicked: () -> Unit,
    onButtonClicked: () -> Unit
) {

    var isButtonEnabled by remember {
        mutableStateOf(false)
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        textAlign = TextAlign.Center,
        style = Title1,
        color = colorResource(id = R.color.text_primary),
        text = uiState.getTitle()
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp)
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier
                .noRippleThrottledClickable {
                    onIconClicked()
                },
            iconSize = 48.dp,
            icon = uiState.icon,
            backgroundColor = R.color.amp_transparent
        )
        Spacer(modifier = Modifier.width(0.dp))
        CreateTypeField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 11.5.dp)
                .wrapContentHeight(),
            hint = uiState.getTitleHint(),
            onTextChanged = {
                onTitleChanged(it)
                isButtonEnabled = it.isNotEmpty()
            }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary),
            text = stringResource(id = R.string.object_type_create_plural_title)
        )
        Spacer(modifier = Modifier.height(4.dp))
        CreateTypeField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            hint = uiState.getPluralHint(),
            textStyle = BodyRegular,
            onTextChanged = onPluralChanged
        )
    }
    Spacer(modifier = Modifier.height(22.dp))

    ButtonPrimary(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = {
            onButtonClicked()
        },
        enabled = isButtonEnabled,
        text = uiState.getButtonTitle(),
        size = ButtonSize.Large,
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun CreateTypeField(
    modifier: Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = HeadlineHeading,
    hint: String,
    onTextChanged: (String) -> Unit
) {

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onTextChanged(textFieldValue.text)
        },
        textStyle = textStyle.copy(color = colorResource(id = R.color.text_primary)),
        singleLine = false,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        modifier = modifier
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            focusManager.clearFocus()
            onTextChanged(textFieldValue.text)
        },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = hint,
                        style = textStyle,
                        color = colorResource(id = R.color.text_tertiary),
                    )
                }
            }
            innerTextField()
        }
    )
}

@Composable
fun UiTypeSetupTitleAndIconState.getTitleHint(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.CreateNewType -> stringResource(id = R.string.object_type_create_title_hint)
        is UiTypeSetupTitleAndIconState.EditType -> stringResource(id = R.string.untitled)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.getPluralHint(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.CreateNewType -> stringResource(id = R.string.object_type_create_plural_title_hint)
        is UiTypeSetupTitleAndIconState.EditType -> stringResource(id = R.string.untitled)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.getTitle(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.CreateNewType -> stringResource(id = R.string.object_type_create_new_title)
        is UiTypeSetupTitleAndIconState.EditType -> stringResource(id = R.string.object_type_rename_title)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.getButtonTitle(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.CreateNewType -> stringResource(id = R.string.create)
        is UiTypeSetupTitleAndIconState.EditType -> stringResource(id = R.string.done)
    }
}

@DefaultPreviews
@Composable
fun CreateNewTypeScreenPreview() {
    Column {
        CreateNewTypeScreenContent(
            uiState = UiTypeSetupTitleAndIconState.CreateNewType(
                icon = ObjectIcon.TypeIcon.Default(
                    rawValue = "american-football",
                    color = CustomIconColor.Red
                )
            ),
            onTitleChanged = {},
            onPluralChanged = {},
            onIconClicked = { /* no-op */ },
            onButtonClicked = { /* no-op */ }
        )
    }
}

@DefaultPreviews
@Composable
fun EditTypeScreenPreview() {
    Column {
        CreateNewTypeScreenContent(
            uiState = UiTypeSetupTitleAndIconState.EditType(
                icon = ObjectIcon.TypeIcon.Default(
                    rawValue = "american-football",
                    color = CustomIconColor.Red
                )
            ),
            onTitleChanged = {},
            onPluralChanged = {},
            onIconClicked = { /* no-op */ },
            onButtonClicked = { /* no-op */ }
        )
    }
}