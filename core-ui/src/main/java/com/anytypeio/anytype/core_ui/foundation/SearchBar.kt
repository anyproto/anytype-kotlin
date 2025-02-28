package com.anytypeio.anytype.core_ui.foundation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DefaultSearchBar(
    modifier: Modifier = Modifier,
    onQueryChanged: (String) -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val focus = LocalFocusManager.current
    val focusRequester = FocusRequester()

    val selectionColors = TextSelectionColors(
        backgroundColor = colorResource(id = R.color.cursor_color).copy(
            alpha = 0.2f
        ),
        handleColor = colorResource(id = R.color.cursor_color),
    )

    var query by remember { mutableStateOf(TextFieldValue()) }

    Row(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.shape_transparent),
                shape = RoundedCornerShape(10.dp)
            )
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search_18),
            contentDescription = "Search icon",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(
                    start = 11.dp
                )
        )
        CompositionLocalProvider(value = LocalTextSelectionColors provides selectionColors) {

            BasicTextField(
                value = query,
                modifier = Modifier
                    .weight(1.0f)
                    .padding(start = 6.dp)
                    .align(Alignment.CenterVertically)
                    .focusRequester(focusRequester),
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                onValueChange = { input ->
                    query = input.also {
                        onQueryChanged(input.text)
                    }
                },
                singleLine = true,
                maxLines = 1,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focus.clearFocus(true)
                    }
                ),
                decorationBox = @Composable { innerTextField ->
                    TextFieldDefaults.OutlinedTextFieldDecorationBox(
                        value = query.text,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.search),
                                style = BodyRegular.copy(
                                    color = colorResource(id = R.color.text_tertiary)
                                )
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            cursorColor = colorResource(id = R.color.cursor_color),
                        ),
                        border = {},
                        contentPadding = PaddingValues()
                    )
                },
                cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
            )
        }
        Spacer(Modifier.width(9.dp))
        AnimatedVisibility(
            visible = query.text.isNotEmpty(),
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(100))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_clear_18),
                contentDescription = "Clear icon",
                modifier = Modifier
                    .padding(end = 9.dp)
                    .noRippleClickable {
                        query = TextFieldValue().also {
                            onQueryChanged("")
                        }
                    }
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun AllContentSearchBarPreview() {
    DefaultSearchBar {}
}