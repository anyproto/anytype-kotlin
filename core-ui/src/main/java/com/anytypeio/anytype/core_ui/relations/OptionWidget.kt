package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.anytypeio.anytype.core_models.ThemeColor
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

}