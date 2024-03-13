package com.anytypeio.anytype.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.peyments.R
import com.anytypeio.anytype.viewmodel.PaymentsCodeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreen(
    state: PaymentsCodeState,
    actionResend: () -> Unit,
    actionCode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colorResource(id = R.color.background_primary),
        content = { ModalCodeContent(state = state, actionCode = actionCode) }
    )
}

@Composable
private fun ModalCodeContent(state: PaymentsCodeState, actionCode: (String) -> Unit) {
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val enteredDigits = remember { mutableStateListOf<Char>() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = enteredDigits.size) {
        if (enteredDigits.size == 4) {
            actionCode(enteredDigits.joinToString(""))
        }
    }

    LaunchedEffect(key1 = state) {
        if (state is PaymentsCodeState.Loading) {
            focusManager.clearFocus(true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                focusRequesters.forEachIndexed { index, focusRequester ->
                    CodeNumber(
                        isEnabled = state !is PaymentsCodeState.Loading,
                        modifier = modifier,
                        focusRequester = focusRequester,
                        onDigitEntered = { digit ->
                            if (enteredDigits.size < 4) {
                                enteredDigits.add(digit)
                            }
                            if (index < 3) focusRequesters[index + 1].requestFocus()
                        },
                        onBackspace = {
                            if (enteredDigits.isNotEmpty()) enteredDigits.removeLast()
                            if (index > 0) focusRequesters[index - 1].requestFocus()
                        }
                    )
                    if (index < 3) Spacer(modifier = Modifier.width(8.dp))
                }
            }
            if (state is PaymentsCodeState.Error) {
                Text(
                    text = state.message,
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 7.dp),
                    textAlign = TextAlign.Center
                )
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
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = state is PaymentsCodeState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = colorResource(R.color.shape_secondary),
                trackColor = colorResource(R.color.shape_primary)
            )
        }
    }
}

@Composable
private fun CodeNumber(
    isEnabled: Boolean,
    focusRequester: FocusRequester,
    onDigitEntered: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier
) {
    val (text, setText) = remember { mutableStateOf("") }

    val borderColor = colorResource(id = R.color.shape_primary)
    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            when {
                newValue.length == 1 && newValue[0].isDigit() && text.isEmpty() -> {
                    setText(newValue)
                    onDigitEntered(newValue[0])
                }

                newValue.isEmpty() -> {
                    if (text.isNotEmpty()) {
                        setText("")
                        onBackspace()
                    }
                }
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace && text.isEmpty()) {
                    onBackspace()
                    true
                } else false
            },
        singleLine = true,
        enabled = isEnabled,
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
        textStyle = HeadlineTitle.copy(color = colorResource(id = R.color.text_primary))
    )
}

@Preview
@Composable
fun EnterCodeModalPreview() {
    ModalCodeContent(
        state = PaymentsCodeState.Loading,
        actionCode = {}
    )
}