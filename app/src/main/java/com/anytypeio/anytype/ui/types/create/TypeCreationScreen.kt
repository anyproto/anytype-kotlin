package com.anytypeio.anytype.ui.types.create

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.types.TypeCreationViewModel
import com.anytypeio.anytype.ui.types.create.TypeScreenDefaults.PaddingBottom
import com.anytypeio.anytype.ui.types.create.TypeScreenDefaults.PaddingTop
import com.anytypeio.anytype.ui.types.views.TypeCreationHeader
import com.anytypeio.anytype.ui.types.views.TypeEditWidget

@ExperimentalMaterialApi
@Composable
fun TypeCreationScreen(vm: TypeCreationViewModel, preparedName: String) {

    val state by vm.uiState.collectAsStateWithLifecycle()
    val inputValue = remember { mutableStateOf(preparedName) }
    val nameValid = remember { mutableStateOf(preparedName.trim().isNotEmpty()) }

    Column(Modifier.padding(top = PaddingTop, bottom = PaddingBottom)) {
        TypeCreationHeader(
            vm = vm,
            nameValid = nameValid,
            inputValue = inputValue
        )
        TypeEditWidget(
            preparedString = inputValue,
            nameValid = nameValid,
            objectIcon = state.objectIcon,
            onLeadingIconClick = vm::openEmojiPicker,
            shouldMoveCursor = preparedName.trim().isNotEmpty()
        )
    }

}

@Immutable
private object TypeScreenDefaults {
    val PaddingTop = 6.dp
    val PaddingBottom = 16.dp
}