package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.relations.option.OptionScreenViewState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OptionWidget(
    state: OptionScreenViewState,
    onButtonClicked: () -> Unit,
    onTextChanged: (String) -> Unit,
    onColorChanged: (ThemeColor) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {

        val currentState by rememberUpdatedState(state)
        val keyboardController = LocalSoftwareKeyboardController.current

        OptionWidgetContent(
            state = currentState,
            onButtonClicked = onButtonClicked,
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            onTextChanged = onTextChanged,
            onColorChanged = onColorChanged
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OptionWidgetContent(
    state: OptionScreenViewState,
    onButtonClicked: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onTextChanged: (String) -> Unit,
    onColorChanged: (ThemeColor) -> Unit
) {
    var selectedColor by remember { mutableStateOf(state.color) }
    var editableText by remember { mutableStateOf(state.text) }

    val (title, buttonText) = getTexts(state)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        Header(text = title)
        TextInput(
            initialValue = state.text,
            color = state.color,
            onTextChanged = {
                editableText = it
                onTextChanged(it)
            },
            focusRequester = focusRequester,
            keyboardController = keyboardController
        )
        Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = stringResource(id = R.string.color),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
        Spacer(modifier = Modifier.height(20.dp))
        CirclesContainer(selectedColor = selectedColor) { newColor ->
            selectedColor = newColor
            onColorChanged(newColor)
        }
        Spacer(modifier = Modifier.height(115.dp))
        ButtonPrimary(
            text = buttonText,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            onClick = { onButtonClicked() },
            size = ButtonSize.Large
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CirclesContainer(selectedColor: ThemeColor, action: (ThemeColor) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 5
    ) {
        val itemModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(CircleShape)
            .aspectRatio(1f)
            .align(Alignment.CenterVertically)
        ThemeColor.values().drop(1).forEach { color ->
            Box(
                itemModifier
                    .noRippleClickable { action(color) }
                    .background(
                        color = dark(color = color)
                    )
            ) {
                if (selectedColor == color) {
                    Image(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(id = R.drawable.ic_tick_24),
                        contentDescription = "option selected"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TextInput(
    initialValue: String,
    color: ThemeColor,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onTextChanged: (String) -> Unit
) {
    var innerValue by remember { mutableStateOf(initialValue) }
    val focusManager = LocalFocusManager.current

    if (initialValue.isEmpty()) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value = innerValue,
        onValueChange = {
            innerValue = it
            onTextChanged(it)
        },
        textStyle = Title1.copy(color = dark(color = color)),
        singleLine = true,
        enabled = true,
        cursorBrush = SolidColor(dark(color = color)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 0.dp, top = 2.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            focusManager.clearFocus()
        },
        decorationBox = { innerTextField ->
            if (innerValue.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.hint_enter_name),
                    style = Title1,
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
private fun Header(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 74.dp),
            text = text,
            style = Title1.copy(),
            color = colorResource(R.color.text_primary),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
private fun getTexts(state: OptionScreenViewState): Pair<String, String> {
    return when (state) {
        is OptionScreenViewState.Create -> {
            stringResource(id = R.string.option_widget_create) to stringResource(id = R.string.create)
        }

        is OptionScreenViewState.Edit -> {
            stringResource(id = R.string.option_widget_edit) to stringResource(id = R.string.apply)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = Devices.PIXEL_4_XL)
@Composable
fun PreviewOptionWidget() {
    OptionWidget(
        state = OptionScreenViewState.Create(
            text = "Urgent",
            color = ThemeColor.BLUE,
        ),
        onButtonClicked = {},
        onTextChanged = {},
        onColorChanged = {}
    )
}
