package com.anytypeio.anytype.ui.types

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.types.TypeCreationViewModel
import com.anytypeio.anytype.ui.types.TypeScreenDefaults.PaddingBottom
import com.anytypeio.anytype.ui.types.TypeScreenDefaults.PaddingTop
import com.anytypeio.anytype.ui.types.views.TypeCreationHeader
import com.anytypeio.anytype.ui.types.views.TypeEditWidget

@ExperimentalLifecycleComposeApi
@ExperimentalMaterialApi
@Composable
fun TypeCreationScreen(vm: TypeCreationViewModel, preparedName: Id) {

    val state by vm.uiState.collectAsStateWithLifecycle()
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

    Column(Modifier.padding(top = PaddingTop, bottom = PaddingBottom)) {
        TypeCreationHeader(
            vm = vm,
            nameValid = nameValid,
            buttonColor = buttonColor,
            inputValue = inputValue
        )
        TypeEditWidget(
            inputValue = inputValue,
            nameValid = nameValid,
            buttonColor = buttonColor,
            objectIcon = state.objectIcon,
            onLeadingIconClick = vm::openEmojiPicker
        )
    }

}

@Immutable
private object TypeScreenDefaults {
    val PaddingTop = 6.dp
    val PaddingBottom = 16.dp
}