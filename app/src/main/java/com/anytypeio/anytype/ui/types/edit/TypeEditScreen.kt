package com.anytypeio.anytype.ui.types.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.types.TypeEditState
import com.anytypeio.anytype.presentation.types.TypeEditViewModel
import com.anytypeio.anytype.ui.types.edit.TypeScreenDefaults.PaddingBottom
import com.anytypeio.anytype.ui.types.edit.TypeScreenDefaults.PaddingTop
import com.anytypeio.anytype.ui.types.views.ImeOptions
import com.anytypeio.anytype.ui.types.views.TypeEditHeader
import com.anytypeio.anytype.ui.types.views.TypeEditWidget

@ExperimentalMaterialApi
@Composable
fun TypeEditScreen(vm: TypeEditViewModel, preparedName: String, readOnly: Boolean) {

    val state by vm.uiState.collectAsStateWithLifecycle()
    val inputValue = remember { mutableStateOf(preparedName) }
    val nameValid = remember { mutableStateOf(preparedName.trim().isNotEmpty()) }

    Column(Modifier.padding(top = PaddingTop, bottom = PaddingBottom).height(120.dp)) {
        TypeEditHeader(vm = vm, readOnly)
        TypeEditWidget(
            preparedString = inputValue,
            nameValid = nameValid,
            objectIcon = (state as? TypeEditState.Data)?.objectIcon ?: ObjectIcon.None,
            onLeadingIconClick = vm::openEmojiPicker,
            onImeDoneClick = vm::updateObjectDetails,
            imeOptions = ImeOptions.Done,
            shouldMoveCursor = preparedName.trim().isNotEmpty()
        )
    }

}

@Immutable
private object TypeScreenDefaults {
    val PaddingTop = 6.dp
    val PaddingBottom = 16.dp
}