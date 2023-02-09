package com.anytypeio.anytype.ui.types.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.ui.settings.fonts


@Composable
fun TypeNameInput(
    inputValue: MutableState<Id>,
    nameValid: MutableState<Boolean>,
    buttonColor: MutableState<Int>
) {

    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {

        OutlinedTextField(
            value = inputValue.value,
            onValueChange = {
                inputValue.value = it
                nameValid.value = it.trim().isNotEmpty()
                buttonColor.value = if (nameValid.value) {
                    R.color.text_primary
                } else {
                    R.color.text_secondary
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    focusRequester.requestFocus()
                }
                .padding(start = 54.dp, end = 16.dp) // for debug, will delete later
            ,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.type_creation_placeholder),
                    color = colorResource(id = R.color.text_tertiary),
                    style = TextStyle(
                        fontFamily = fonts,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp
                    )
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = colorResource(id = R.color.text_primary),
                backgroundColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                placeholderColor = colorResource(id = R.color.text_tertiary),
                cursorColor = colorResource(id = R.color.toolbar_section_tool) // need to check color
            )
        )
    }

}

