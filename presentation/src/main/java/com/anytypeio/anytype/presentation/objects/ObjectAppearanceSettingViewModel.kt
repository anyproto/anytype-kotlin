package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ObjectAppearanceSettingViewModel(
    private val orchestrator: Orchestrator
) : ViewModel() {

    val objectPreviewState = MutableSharedFlow<State>(replay = 0)

    fun onStart(blockId: Id, targetId: Id) {
        val block = orchestrator.stores.views.current().firstOrNull { it.id == blockId }
        if (block != null && block is BlockView.LinkToObject.Default) {
            with(block.appearanceParams) {
                val views = listOf(
                    ObjectAppearanceSettingView.Settings.PreviewLayout(style = style),
                    ObjectAppearanceSettingView.Settings.Icon(size = iconSize, withIcon = withIcon),
                    ObjectAppearanceSettingView.Settings.Cover(withCover = withCover),
                    ObjectAppearanceSettingView.Section.FeaturedRelations,
                    ObjectAppearanceSettingView.Relation.Name(withName = withName),
                    ObjectAppearanceSettingView.Relation.Description(withDescription = withDescription)
                )
                viewModelScope.launch {
                    objectPreviewState.emit(State.Success(views))
                }
            }
        }
    }

    //region STATE
    sealed class State {
        data class Success(val data: List<ObjectAppearanceSettingView>) : State()
        data class Error(val msg: String) : State()
    }
    //endregion

    //region FACTORY
    class Factory(
        private val orchestrator: Orchestrator
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceSettingViewModel(
                orchestrator = orchestrator
            ) as T
        }
    }
    //endregion
}