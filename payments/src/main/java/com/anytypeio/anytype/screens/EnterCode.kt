package com.anytypeio.anytype.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.peyments.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterCodeModal(actionResend: () -> Unit, actionCode: (String) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss() },
        containerColor = colorResource(id = R.color.background_primary),
        content = {
            ModalCodeContent()
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ModalCodeContent(error: String? = null, onCodeEntered: (String) -> Unit = {}) {
    val (item1, item2, item3, item4) = FocusRequester.createRefs()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.padding(118.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            text = stringResource(id = com.anytypeio.anytype.localization.R.string.payments_code_title),
            style = BodyBold,
            color = colorResource(
                id = R.color.text_primary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(44.dp))
        val modifier = Modifier
            .width(48.dp)
            .height(64.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center
        ) {
            CodeNumber(
                modifier = modifier
                    .focusRequester(item1)
                    .focusProperties {
                        next = item2
                        previous = item1
                    },
                onNumberChanged = { onCodeEntered(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            CodeNumber(modifier = modifier
                .focusRequester(item2)
                .focusProperties {
                    next = item3
                    previous = item1
                },
                onNumberChanged = { onCodeEntered(it) })
            Spacer(modifier = Modifier.width(8.dp))
            CodeNumber(modifier = modifier
                .focusRequester(item3)
                .focusProperties {
                    next = item4
                    previous = item2
                },
                onNumberChanged = { onCodeEntered(it) })
            Spacer(modifier = Modifier.width(8.dp))
            CodeNumber(modifier = modifier
                .focusRequester(item4)
                .focusProperties {
                    next = item4
                    previous = item3
                },
                onNumberChanged = { onCodeEntered(it) })
        }

        Spacer(modifier = Modifier.height(149.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = com.anytypeio.anytype.localization.R.string.payments_code_resend),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_tertiary),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CodeNumber(modifier: Modifier = Modifier, onNumberChanged: (String) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }
    val maxChar = 1
    val focusManager = LocalFocusManager.current

    LaunchedEffect(
        key1 = text,
    ) {
        if (text.isNotEmpty()) {
            focusManager.moveFocus(
                focusDirection = FocusDirection.Next,
            )
        }
    }
    val borderColor = colorResource(id = R.color.shape_primary)
    BasicTextField(
        value = text,
        onValueChange = { newText: String ->
            if (newText.length <= maxChar && (newText.isEmpty() || newText.isDigitsOnly())) {
                setText(newText)
                onNumberChanged(newText)
            }
        },
        modifier = modifier
            .onKeyEvent { event ->
                if (event.key == Key.Tab) {
                    focusManager.moveFocus(FocusDirection.Next)
                    true
                } else {
                    if (text.isEmpty() && event.key == Key.Backspace) {
                        focusManager.moveFocus(FocusDirection.Previous)
                    }
                    false
                }
            },
        singleLine = true,
        enabled = true,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        },
        textStyle = HeadlineTitle
    )
}

@Preview
@Composable
fun EnterCodeModalPreview() {
    ModalCodeContent()
}