package com.anytypeio.anytype.ui.types

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.types.TypeCreationViewModel
import com.anytypeio.anytype.ui.types.TypeScreenDefaults.PaddingTop
import com.anytypeio.anytype.ui.types.views.TypeCreationHeader
import com.anytypeio.anytype.ui.types.views.TypeNameInput

@ExperimentalMaterialApi
@Composable
fun TypeCreationScreen(vm: TypeCreationViewModel, preparedName: Id) {

    val inputValue = remember { mutableStateOf(preparedName) }
    val nameValid = remember { mutableStateOf(preparedName.trim().isNotEmpty()) }
    val buttonColor = remember {
        mutableStateOf(
            if (nameValid.value) {
                R.color.text_primary
            } else {
                R.color.text_secondary
            }
        )
    }

    Column(Modifier.padding(top = PaddingTop)) {
        TypeCreationHeader(
            vm = vm,
            nameValid = nameValid,
            buttonColor = buttonColor,
            inputValue = inputValue
        )
        TypeNameInput(
            inputValue = inputValue,
            nameValid = nameValid,
            buttonColor = buttonColor
        )
    }

}

@Immutable
private object TypeScreenDefaults {
    val PaddingTop = 6.dp
}