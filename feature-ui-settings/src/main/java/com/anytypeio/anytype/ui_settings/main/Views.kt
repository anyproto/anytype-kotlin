package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.ui_settings.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter

@OptIn(FlowPreview::class)
@Composable
fun SpaceNameBlock(
    modifier: Modifier = Modifier,
    name: String,
    onNameSet: (String) -> Unit,
    isEditEnabled: Boolean
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(nameValue.value) {
        snapshotFlow { nameValue.value }
            .debounce(SPACE_NAME_CHANGE_DELAY)
            .dropWhile { input -> input == name }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
                onNameSet(query)
            }
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.space_name),
            style = BodyCalloutRegular.copy(color = colorResource(id = R.color.text_primary)),
            color = colorResource(id = R.color.text_secondary)
        )
        SettingsTextField(
            value = nameValue.value,
            textStyle = BodyBold.copy(color = colorResource(id = R.color.text_primary)),
            onValueChange = {
                nameValue.value = it
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            placeholderText = stringResource(id = R.string.space_name),
            isEditEnabled = isEditEnabled
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun SpaceDescriptionBlock(
    modifier: Modifier = Modifier,
    description: String,
    onDescriptionSet: (String) -> Unit,
    isEditEnabled: Boolean
) {

    val descriptionValue = remember { mutableStateOf(description) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(descriptionValue.value) {
        snapshotFlow { descriptionValue.value }
            .debounce(SPACE_NAME_CHANGE_DELAY)
            .dropWhile { input -> input == description }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
                onDescriptionSet(query)
            }
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.space_description),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary)
        )
        SettingsTextField(
            textStyle = BodyRegular.copy(color = colorResource(id = R.color.text_primary)),
            value = descriptionValue.value,
            onValueChange = {
                descriptionValue.value = it
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            placeholderText = stringResource(id = R.string.space_description),
            isEditEnabled = isEditEnabled
        )
    }
}

@Composable
fun SpaceNameBlock() {
    Text(
        text = stringResource(id = R.string.space_settings),
        style = Title1,
        color = colorResource(id = R.color.text_primary)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = BodyBold,
    placeholderText: String,
    isEditEnabled: Boolean
) {
    BasicTextField(
        value = value,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        onValueChange = onValueChange,
        enabled = isEditEnabled,
        readOnly = !isEditEnabled,
        textStyle = textStyle,
        cursorBrush = SolidColor(colorResource(id = R.color.orange)),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = keyboardActions,
        interactionSource = remember { MutableInteractionSource() },
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                label = null,
                leadingIcon = null,
                trailingIcon = null,
                singleLine = true,
                enabled = true,
                isError = false,
                placeholder = {
                    Text(
                        style = textStyle,
                        text = placeholderText,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                },
                interactionSource = remember { MutableInteractionSource() },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = colorResource(id = R.color.text_primary),
                    backgroundColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    placeholderColor = colorResource(id = R.color.text_tertiary),
                    cursorColor = colorResource(id = R.color.orange)
                ),
                contentPadding = PaddingValues(),
                border = {}
            )
        }
    )
}

private const val SPACE_NAME_CHANGE_DELAY = 300L