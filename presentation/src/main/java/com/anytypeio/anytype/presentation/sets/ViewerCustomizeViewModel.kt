package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class ViewerCustomizeViewState {
    object Init : ViewerCustomizeViewState()
    data class InitGrid(
        val filterSize: String,
        val sortsSize: String,
        val isShowFilterSize: Boolean,
        val isShowSortsSize: Boolean
    ) : ViewerCustomizeViewState()
}

class ViewerCustomizeViewModel(private val state: StateFlow<ObjectSet>) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewerCustomizeViewState>(
        ViewerCustomizeViewState.Init
    )
    val viewState: StateFlow<ViewerCustomizeViewState> = _viewState

    fun onViewCreated(viewerId: Id) {
        val set = state.value
        val block = set.blocks.first { it.content is DV }
        val dv = block.content as DV
        val viewer = dv.viewers.first { it.id == viewerId }
        _viewState.value = when (val type = viewer.type) {
            Block.Content.DataView.Viewer.Type.GRID -> ViewerCustomizeViewState.InitGrid(
                isShowFilterSize = viewer.filters.isNotEmpty(),
                isShowSortsSize = viewer.sorts.isNotEmpty(),
                filterSize = viewer.filters.size.toString(),
                sortsSize = viewer.sorts.size.toString()
            )
            else -> throw IllegalStateException("Unexpected view type: $type")
        }
    }

    class Factory(
        private val state: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewerCustomizeViewModel(state) as T
        }
    }
}