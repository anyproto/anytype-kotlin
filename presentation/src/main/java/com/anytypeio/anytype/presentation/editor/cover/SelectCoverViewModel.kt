package com.anytypeio.anytype.presentation.editor.cover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SelectCoverViewModel(
    private val getCoverGradientCollection: GetCoverGradientCollection
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    val views = MutableStateFlow<List<DocCoverGalleryView>>(emptyList())
    val isDismissed = MutableSharedFlow<Boolean>()

    init {
        render()
    }

    private fun render() {
        val result = mutableListOf<DocCoverGalleryView>()
        result.add(DocCoverGalleryView.Section.Color)
        result.addAll(
            CoverColor.values().map {
                DocCoverGalleryView.Color(it)
            }
        )
        result.add(DocCoverGalleryView.Section.Gradient)
        result.addAll(
            getCoverGradientCollection.provide().map {
                DocCoverGalleryView.Gradient(it)
            }
        )
        views.value = result.toList()
    }

    fun onSolidColorSelected(color: CoverColor) {
        viewModelScope.launch {
            commands.emit(Command.OnColorSelected(color))
        }
    }

    fun onGradientColorSelected(gradient: String) {
        viewModelScope.launch {
            commands.emit(Command.OnGradientSelected(gradient))
        }
    }

    fun onImageSelected(hash: String) {
        viewModelScope.launch {
            commands.emit(Command.OnImageSelected(hash))
        }
    }

    sealed class Command {
        data class OnColorSelected(val color: CoverColor) : Command()
        data class OnGradientSelected(val gradient: String) : Command()
        data class OnImageSelected(val hash: String) : Command()
    }

    class Factory(
        private val getCoverGradientCollection: GetCoverGradientCollection
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectCoverViewModel(getCoverGradientCollection = getCoverGradientCollection) as T
        }
    }
}