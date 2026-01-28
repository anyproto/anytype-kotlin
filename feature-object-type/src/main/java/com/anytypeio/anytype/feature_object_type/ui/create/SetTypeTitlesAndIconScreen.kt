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
import androidx.compose.runtime.LaunchedEffect
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
import com.anytypeio.anytype.core_models.ui.CustomIconColor
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
import com.anytypeio.anytype.core_models.ui.ObjectIcon


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTypeTitlesAndIconScreen(
    uiState: UiTypeSetupTitleAndIconState,
    modifier: Modifier = Modifier,
    onIconClicked: () -> Unit,
    onDismiss: () -> Unit,
    onButtonClicked: (String, String) -> Unit
) {

    if (uiState !is UiTypeSetupTitleAndIconState.Visible) return

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
            onIconClicked = onIconClicked,
            onButtonClicked = onButtonClicked
        )
    }
}

@Composable
private fun ColumnScope.CreateNewTypeScreenContent(
    uiState: UiTypeSetupTitleAndIconState.Visible,
    onIconClicked: () -> Unit,
    onButtonClicked: (String, String) -> Unit
) {

    // Only track the Title non-empty condition.
    var isTitleNotEmpty by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf(uiState.getInitialTitleValue()) }
    // Plural text is maintained separately.
    var pluralText by remember { mutableStateOf(uiState.getInitialPluralValue()) }
    // The button is enabled if the Title is not empty
    val isButtonEnabled = isTitleNotEmpty

    val icon = when(uiState) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> uiState.icon
        is UiTypeSetupTitleAndIconState.Visible.EditType -> uiState.icon
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
            .padding(start = 19.dp, end = 16.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier
                .noRippleThrottledClickable {
                    onIconClicked()
                },
            iconSize = 30.dp,
            icon = icon,
            backgroundColor = R.color.amp_transparent
        )
        Spacer(modifier = Modifier.width(9.dp))
        CreateTypeField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            hint = uiState.getTitleHint(),
            initialValue = uiState.getInitialTitleValue(),
            onTextChanged = { newText ->
                // Check if the plural still matches the current title.
                // If they are equal then we can assume auto-population is still active.
                val shouldAutoUpdate = titleText == pluralText
                titleText = newText
                if (shouldAutoUpdate) {
                    pluralText = newText
                }
            },
            onButtonEnabled = { enable ->
                isTitleNotEmpty = enable
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
        // Note: Here we pass the parent's pluralText as the initialValue so that
        // if it changes, we can update the field (see the LaunchedEffect inside CreateTypeField).
        CreateTypeField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            hint = uiState.getPluralHint(),
            initialValue = pluralText,
            textStyle = BodyRegular,
            onTextChanged = { newPluralText ->
                pluralText = newPluralText
            },
            onButtonEnabled = { }
        )
    }
    Spacer(modifier = Modifier.height(22.dp))

    ButtonPrimary(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = {
            onButtonClicked(titleText, pluralText)
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
    initialValue: String = "",
    textStyle: androidx.compose.ui.text.TextStyle = HeadlineHeading,
    hint: String,
    onButtonEnabled: (Boolean) -> Unit = {},
    onTextChanged: (String) -> Unit
) {

    // Hold the text state internally.
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(initialValue)
        )
    }

    // Store the last value received from the parent.
    var lastInitialValue by remember { mutableStateOf(initialValue) }

    // If initialValue changes, and the field is still auto-populated (i.e. the user didn't change it manually)
    // then update the textFieldValue.
    LaunchedEffect(initialValue) {
        if (textFieldValue.text == lastInitialValue) {
            textFieldValue = TextFieldValue(initialValue)
            onButtonEnabled(initialValue.isNotEmpty())
        }
        lastInitialValue = initialValue
    }

    LaunchedEffect(textFieldValue) {
        onButtonEnabled(textFieldValue.text.isNotEmpty())
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

fun UiTypeSetupTitleAndIconState.Visible.getInitialTitleValue(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> ""
        is UiTypeSetupTitleAndIconState.Visible.EditType -> this.initialTitle ?: ""
    }
}

fun UiTypeSetupTitleAndIconState.Visible.getInitialPluralValue(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> ""
        is UiTypeSetupTitleAndIconState.Visible.EditType -> this.initialPlural ?: ""
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.Visible.getTitleHint(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> stringResource(id = R.string.object_type_create_title_hint)
        is UiTypeSetupTitleAndIconState.Visible.EditType -> stringResource(id = R.string.untitled)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.Visible.getPluralHint(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> stringResource(id = R.string.object_type_create_plural_title_hint)
        is UiTypeSetupTitleAndIconState.Visible.EditType -> stringResource(id = R.string.untitled)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.Visible.getTitle(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> stringResource(id = R.string.object_type_create_new_title)
        is UiTypeSetupTitleAndIconState.Visible.EditType -> stringResource(id = R.string.object_type_rename_title)
    }
}

@Composable
fun UiTypeSetupTitleAndIconState.Visible.getButtonTitle(): String {
    return when (this) {
        is UiTypeSetupTitleAndIconState.Visible.CreateNewType -> stringResource(id = R.string.create)
        is UiTypeSetupTitleAndIconState.Visible.EditType -> stringResource(id = R.string.done)
    }
}

@DefaultPreviews
@Composable
fun CreateNewTypeScreenPreview() {
    Column {
        CreateNewTypeScreenContent(
            uiState = UiTypeSetupTitleAndIconState.Visible.CreateNewType(
                icon = ObjectIcon.TypeIcon.Default(
                    rawValue = "american-football",
                    color = CustomIconColor.Red
                )
            ),
            onIconClicked = { /* no-op */ },
            onButtonClicked = { _, _ -> }
        )
    }
}

@DefaultPreviews
@Composable
fun EditTypeScreenPreview() {
    Column {
        CreateNewTypeScreenContent(
            uiState = UiTypeSetupTitleAndIconState.Visible.EditType(
                icon = ObjectIcon.TypeIcon.Default(
                    rawValue = "american-football",
                    color = CustomIconColor.Red
                ),
                initialTitle = "Page",
                initialPlural = "Pages"
            ),
            onIconClicked = { /* no-op */ },
            onButtonClicked = { _, _ -> }
        )
    }
}