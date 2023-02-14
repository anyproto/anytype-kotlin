package com.anytypeio.anytype.ui.types.views

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.settings.fonts
import com.anytypeio.anytype.ui.types.views.TypeEditWidgetDefaults.OffsetX
import com.anytypeio.anytype.ui.types.views.TypeEditWidgetDefaults.PaddingStart


@Composable
fun TypeEditWidget(
    inputValue: MutableState<Id>,
    nameValid: MutableState<Boolean>,
    buttonColor: MutableState<Int>,
    objectIcon: ObjectIcon,
    onLeadingIconClick: () -> Unit,
    imeOptions: ImeOptions = ImeOptions.Default,
    onImeDoneClick: (name: String) -> Unit = {}
) {

    val focusRequester = remember { FocusRequester() }

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
            .padding(start = PaddingStart)
            .offset(OffsetX)
            .onGloballyPositioned {
                focusRequester.requestFocus()
            },
        keyboardOptions = KeyboardOptions(
            imeAction = when (imeOptions) {
                ImeOptions.Default -> ImeAction.Default
                ImeOptions.Done -> ImeAction.Done
            }
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeDoneClick(inputValue.value)
            }
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
            cursorColor = colorResource(id = R.color.toolbar_section_tool)
        ),
        leadingIcon = {
            LeadingIcon(
                icon = objectIcon,
                onClick = onLeadingIconClick
            )
        }
    )

}

enum class ImeOptions {
    Default,
    Done,
}

@Immutable
private object TypeEditWidgetDefaults {
    val OffsetX = (-4).dp
    val PaddingStart = 6.dp
}
