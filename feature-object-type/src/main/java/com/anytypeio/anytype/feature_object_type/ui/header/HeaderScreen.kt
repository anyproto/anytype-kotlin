package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.models.UiIconState
import com.anytypeio.anytype.feature_object_type.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.ObjectIcon


@Composable
fun IconAndTitleWidget(
    modifier: Modifier,
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Row(modifier = modifier) {
        ListWidgetObjectIcon(
            modifier = Modifier.size(32.dp)
                .noRippleThrottledClickable{
                    if (uiIconState.isEditable) {
                        onTypeEvent.invoke(TypeEvent.OnObjectTypeIconClick)
                    }
                },
            icon = uiIconState.icon,
            backgroundColor = R.color.amp_transparent
        )
        NameField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            uiTitleState = uiTitleState,
            onTypeEvent = onTypeEvent,
        )
    }
}

@Composable
fun NameField(
    modifier: Modifier,
    uiTitleState: UiTitleState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    var innerValue by remember(uiTitleState.title) { mutableStateOf(uiTitleState.title) }
    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = innerValue,
        onValueChange = { innerValue = it },
        textStyle = HeadlineTitle.copy(color = colorResource(id = R.color.text_primary)),
        singleLine = false,
        enabled = uiTitleState.isEditable,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        modifier = modifier
            .padding(start = 12.dp, end = 20.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            focusManager.clearFocus()
            onTypeEvent.invoke(
                TypeEvent.OnObjectTypeTitleUpdate(
                    title = innerValue
                )
            )
        },
        decorationBox = { innerTextField ->
//            if (innerValue.isEmpty()) {
//                Text(
//                    text = stringResource(id = R.string.new_view),
//                    style = Title1,
//                    color = colorResource(id = R.color.text_tertiary),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .wrapContentHeight()
//                )
//            }
            innerTextField()
        }
    )
}

@DefaultPreviews
@Composable
fun IconAndTitleWidgetPreview() {
    IconAndTitleWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onTypeEvent = {},
        uiIconState = UiIconState(icon = ObjectIcon.Task(isChecked = false), isEditable = true),
        uiTitleState = UiTitleState(title = "I understand that contributing to this repository will require me to agree with the", isEditable = true)
    )
}