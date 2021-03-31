package com.anytypeio.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.page.picker.AddBlockView.Companion.items
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DocumentAddBlockViewModel(
    private val getObjectTypes: GetObjectTypes
) : ViewModel() {

    val views = MutableStateFlow<List<AddBlockView>>(emptyList())
    val commands = MutableSharedFlow<Commands>(0)
    var objectTypes : List<ObjectType> = emptyList()

    fun onStart() {
        views.value = listOf(AddBlockView.AddBlockHeader) + items()
    }

    fun onObjectTypeClicked(objectView: AddBlockView.ObjectView) {
        viewModelScope.launch {
            commands.emit(Commands.NotifyOnObjectTypeClicked(objectView.url, objectView.layout))
        }
    }

    sealed class Commands{
        data class NotifyOnObjectTypeClicked(val url: String, val layout: ObjectType.Layout) :
            Commands()
    }
}

class DocumentAddBlockViewModelFactory(
    private val getObjectTypes: GetObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocumentAddBlockViewModel(
            getObjectTypes
        ) as T
    }
}