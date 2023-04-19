package com.anytypeio.anytype.ui.relations

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.presentation.relations.RelationEditState
import com.anytypeio.anytype.presentation.relations.RelationEditViewModel
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.ui.relations.TypeEditWidgetDefaults.OffsetX
import com.anytypeio.anytype.ui.relations.TypeEditWidgetDefaults.PaddingStart
import com.anytypeio.anytype.ui.relations.RelationScreenDefaults.PaddingBottom
import com.anytypeio.anytype.ui.relations.RelationScreenDefaults.PaddingTop
import com.anytypeio.anytype.ui.types.views.ImeOptions

@ExperimentalMaterialApi
@Composable
fun RelationEditScreen(vm: RelationEditViewModel, preparedName: String, readOnly: Boolean) {

    val state by vm.uiState.collectAsStateWithLifecycle()
    val inputValue = remember { mutableStateOf(preparedName) }
    val nameValid = remember { mutableStateOf(preparedName.trim().isNotEmpty()) }

    Column(Modifier.padding(top = PaddingTop, bottom = PaddingBottom).height(132.dp)) {
        RelationEditHeader(vm = vm, readOnly = readOnly)
        RelationEditWidget(
            preparedString = inputValue,
            nameValid = nameValid,
            state = state,
            onImeDoneClick = {
                vm.updateRelationDetails(name = inputValue.value.trim())
            },
            imeOptions = ImeOptions.Done,
            shouldMoveCursor = preparedName.trim().isNotEmpty()
        )
    }

}

@Composable
fun RelationEditHeader(
    vm: RelationEditViewModel,
    readOnly: Boolean
) {

    Box(modifier = Modifier.fillMaxWidth()) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(EditHeaderDefaults.Height)
            .padding(EditHeaderDefaults.PaddingValues)
    ) {
        if (!readOnly) {
            Spacer(modifier = Modifier.weight(1f))
        }
        Text(
            text = stringResource(id = R.string.relation_editing_title),
            color = colorResource(id = R.color.text_primary),
            style = Title1,
        )
        if (!readOnly) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.type_editing_uninstall),
                    color = colorResource(id = R.color.palette_system_red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { vm.uninstallRelation() },
                    textAlign = TextAlign.End,
                    style = UXBody
                )
            }
        }
    }

}

@Composable
fun RelationEditWidget(
    preparedString: MutableState<String>,
    nameValid: MutableState<Boolean>,
    state: RelationEditState,
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
            .padding(start = PaddingStart)
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
            if (state is RelationEditState.Data) {
                LeadingRelationIcon(icon = state.objectIcon)
            }
        },
        textStyle = BodyRegular
    )

}

@Composable
fun LeadingRelationIcon(
    @DrawableRes icon: Int,
) {
    Image(modifier = Modifier, painter = painterResource(id = icon), contentDescription = "")
}


@Immutable
private object TypeEditWidgetDefaults {
    val OffsetX = (-4).dp
    val PaddingStart = 6.dp
}

@Immutable
private object EditHeaderDefaults {
    val Height = 54.dp
    val PaddingValues = PaddingValues(start = 12.dp, top = 18.dp, end = 16.dp, bottom = 12.dp)
}

@Immutable
private object RelationScreenDefaults {
    val PaddingTop = 6.dp
    val PaddingBottom = 16.dp
}