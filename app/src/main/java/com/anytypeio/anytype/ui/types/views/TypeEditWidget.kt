package com.anytypeio.anytype.ui.types.views

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui.settings.fonts
import com.anytypeio.anytype.ui.types.views.TypeEditWidgetDefaults.OffsetX


@Composable
fun TypeEditWidget(
    preparedString: MutableState<String>,
    nameValid: MutableState<Boolean>,
    objectIcon: ObjectIcon,
    onLeadingIconClick: () -> Unit,
    imeOptions: ImeOptions = ImeOptions.Default,
    onImeDoneClick: (name: String) -> Unit = {},
    shouldMoveCursor: Boolean
) {

    val focusRequester = remember { FocusRequester() }
    val innerValue = remember {
        mutableStateOf(
            TextFieldValue(
                text = preparedString.value,
            )
        )
    }

    val cursorMoved = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = innerValue.value,
        onValueChange = {
            innerValue.value = it
            with(it.text.trim()) {
                preparedString.value = this
                nameValid.value = this.isNotEmpty()
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .offset(OffsetX)
            .onGloballyPositioned {
                focusRequester.requestFocus()
                if (shouldMoveCursor && cursorMoved.value.not()) {
                    innerValue.value = TextFieldValue(
                        text = preparedString.value,
                        selection = TextRange(preparedString.value.length)
                    )
                    cursorMoved.value = true
                }
            },
        keyboardOptions = KeyboardOptions(
            imeAction = when (imeOptions) {
                ImeOptions.Default -> ImeAction.Default
                ImeOptions.Done -> ImeAction.Done
            }
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeDoneClick(innerValue.value.text)
            }
        ),
        singleLine = true,
        placeholder = {
            Text(
                text = stringResource(id = R.string.type_creation_placeholder),
                color = colorResource(id = R.color.text_tertiary),
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
}
