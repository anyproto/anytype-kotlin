package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchField(
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val focusManager = LocalFocusManager.current
        val focusRequester = FocusRequester()
        val input = remember { mutableStateOf(String()) }
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = colorResource(id = R.color.shape_transparent))
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_search_18),
                contentDescription = "Search icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
            if (input.value.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clear_18),
                    contentDescription = "Search icon",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .noRippleClickable {
                            input.value = ""
                            onQueryChanged("")
                        }
                )
            }
            BasicTextField(
                value = input.value,
                onValueChange = {
                    input.value = it
                    onQueryChanged(it)
                },
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp)
                    .align(Alignment.CenterStart)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused)
                            onFocused()
                    },
                maxLines = 1,
                singleLine = true,
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                cursorBrush = SolidColor(
                    colorResource(id = R.color.cursor_color)
                ),
                decorationBox = @Composable { innerTextField ->
                    TextFieldDefaults.OutlinedTextFieldDecorationBox(
                        value = input.value,
                        innerTextField = innerTextField,
                        singleLine = true,
                        enabled = true,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_hint),
                                style = BodyRegular
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = colorResource(id = R.color.text_primary),
                            backgroundColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            placeholderColor = colorResource(id = R.color.text_tertiary)
                        ),
                        interactionSource = remember { MutableInteractionSource() },
                        visualTransformation = VisualTransformation.None,
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 0.dp
                        ),
                        border = {},
                    )
                }
            )
        }
    }
}