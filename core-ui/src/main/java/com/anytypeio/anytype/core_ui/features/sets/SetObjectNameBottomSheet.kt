package com.anytypeio.anytype.core_ui.features.sets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.EmojiPickerScreen
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.picker.EmojiPickerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetObjectNameBottomSheet(
    isVisible: Boolean,
    icon: ObjectIcon,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onIconClicked: () -> Unit,
    onOpenClicked: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_secondary),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon button
                    ListWidgetObjectIcon(
                        modifier = Modifier
                            .size(20.dp)
                            .noRippleClickable { onIconClicked() },
                        icon = icon,
                        iconSize = 20.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text field
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onTextChanged(newValue.text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = PreviewTitle2Medium.copy(
                            color = colorResource(id = R.color.text_primary)
                        ),
                        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onDismiss()
                            }
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        modifier = Modifier.padding(start = 1.5.dp),
                                        text = stringResource(id = R.string.untitled),
                                        style = PreviewTitle2Medium,
                                        color = colorResource(id = R.color.text_tertiary)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Open button
                    Icon(
                        painter = painterResource(id = R.drawable.ic_open_to_edit),
                        contentDescription = "Open object",
                        modifier = Modifier
                            .size(24.dp)
                            .noRippleClickable { onOpenClicked() },
                        tint = colorResource(id = R.color.control_secondary)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetObjectIconPickerBottomSheet(
    isVisible: Boolean,
    views: List<EmojiPickerView>,
    onEmojiClicked: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Animate hide when isVisible becomes false
    LaunchedEffect(isVisible) {
        if (!isVisible && sheetState.isVisible) {
            sheetState.hide()
        }
    }

    // Only show when visible
    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        },
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        EmojiPickerScreen(
            views = views,
            onEmojiClicked = onEmojiClicked,
            onQueryChanged = onQueryChanged,
            showDragger = false
        )
    }
}

@Composable
@DefaultPreviews
fun PreviewSetObjectNameBottomSheet() {
    SetObjectNameBottomSheet(
        isVisible = true,
        icon = ObjectIcon.TypeIcon.Default.DEFAULT,
        onTextChanged = {},
        onDismiss = {},
        onIconClicked = {},
        onOpenClicked = {}
    )
}

