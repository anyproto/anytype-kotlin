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
import androidx.compose.foundation.layout.size
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
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.header.NameField
import com.anytypeio.anytype.presentation.objects.ObjectIcon

data class UiCreateTypeState(
    val icon: ObjectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
    val initialTitle: String,
) {
    companion object {
        val Empty = UiCreateTypeState(
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            initialTitle = ""
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewTypeScreen(
    uiState: UiCreateTypeState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
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
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ColumnScope.CreateNewTypeScreenContent(
    uiState: UiCreateTypeState,
    onDismiss: () -> Unit,
) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        textAlign = TextAlign.Center,
        style = Title1,
        color = colorResource(id = R.color.text_primary),
        text = stringResource(id = R.string.object_type_create_new_title)
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
            .padding(vertical = 20.dp)
    ) {
        ListWidgetObjectIcon(
            modifier = Modifier
                .size(30.dp)
                .noRippleThrottledClickable {

                },
            icon = uiState.icon,
            backgroundColor = R.color.amp_transparent
        )
        Spacer(modifier = Modifier.width(12.dp))
        CreateTypeField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.5.dp)
                .wrapContentHeight(),
            initialName = uiState.initialTitle,
            onTypeEvent = {},
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
            initialName = "",
        ) { }

    }
    Spacer(modifier = Modifier.height(22.dp))

    ButtonPrimary(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        onClick = {
            onDismiss()
        },
        text = stringResource(id = R.string.create),
        size = ButtonSize.Large,
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun CreateTypeField(
    modifier: Modifier,
    initialName: String,
    onTypeEvent: (TypeEvent) -> Unit
) {

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(initialName)
        )
    }

    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onTypeEvent.invoke(
                TypeEvent.OnObjectTypeTitleUpdate(
                    title = textFieldValue.text
                )
            )
        },
        textStyle = HeadlineTitle.copy(color = colorResource(id = R.color.text_primary)),
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
            onTypeEvent.invoke(
                TypeEvent.OnObjectTypeTitleUpdate(
                    title = textFieldValue.text
                )
            )
        },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = stringResource(id = R.string.untitled),
                        style = HeadlineTitle,
                        color = colorResource(id = R.color.text_tertiary),
                    )
                }
            }
            innerTextField()
        }
    )
}

@DefaultPreviews
@Composable
fun CreateNewTypeScreenPreview() {
    Column {
        CreateNewTypeScreenContent(
            uiState = UiCreateTypeState(
                icon = ObjectIcon.TypeIcon.Default.DEFAULT,
                initialTitle = "To add a dependency on Emoji2, you must add the Google Maven repository to your project. Read Google's Maven repository for more information"
            ),
            onDismiss = {}
        )
    }
}