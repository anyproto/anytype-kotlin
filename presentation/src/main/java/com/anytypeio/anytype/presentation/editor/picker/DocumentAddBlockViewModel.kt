package com.anytypeio.anytype.presentation.editor.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.picker.AddBlockView.Companion.itemsExperimental
import com.anytypeio.anytype.presentation.editor.picker.AddBlockView.Companion.itemsStable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DocumentAddBlockViewModel(
    private val getObjectTypes: GetObjectTypes,
    private val getFlavourConfig: GetFlavourConfig
) : ViewModel() {

    val views = MutableStateFlow<List<AddBlockView>>(emptyList())
    val commands = MutableSharedFlow<Commands>(0)
    var objectTypes : List<ObjectType> = emptyList()

    fun onStart() {
        if (getFlavourConfig.isDataViewEnabled()) {
            views.value = listOf(AddBlockView.AddBlockHeader) + itemsExperimental()
            viewModelScope.launch {
                val params = GetObjectTypes.Params(filterArchivedObjects = true)
                getObjectTypes.invoke(params).proceed(
                    failure = { Timber.e("Error getting object type list:${it.message}") },
                    success = { addObjectTypes(it) }
                )
            }
        } else {
            views.value = listOf(AddBlockView.AddBlockHeader) + itemsStable()
        }
    }

    fun onObjectTypeClicked(objectView: AddBlockView.ObjectView) {
        viewModelScope.launch {
            commands.emit(Commands.NotifyOnObjectTypeClicked(objectView.url, objectView.layout))
        }
    }

    private fun addObjectTypes(list: List<ObjectType>) {
        this.objectTypes = list
        val objectTypes = list.map { objectType ->
            AddBlockView.ObjectView(
                url = objectType.url,
                name = objectType.name,
                emoji = objectType.emoji,
                description = objectType.description,
                layout = objectType.layout
            )
        }
        val position = views.value.lastIndexOf(AddBlockView.Item(type = UiBlock.LINK_TO_OBJECT))
        views.value = views.value.toMutableList().apply {
            addAll(position + 1, objectTypes)
        }
    }

    sealed class Commands{
        data class NotifyOnObjectTypeClicked(val url: String, val layout: ObjectType.Layout) :
            Commands()
    }
}

class DocumentAddBlockViewModelFactory(
    private val getObjectTypes: GetObjectTypes,
    private val getFlavourConfig: GetFlavourConfig
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DocumentAddBlockViewModel(
            getObjectTypes,
            getFlavourConfig
        ) as T
    }
}