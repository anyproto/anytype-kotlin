package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_LARGE
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_MEDIUM
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_SMALL
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView.Icon
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ObjectAppearanceIconViewModel(
    private val orchestrator: Orchestrator
) : ViewModel() {

    val state = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id) {
        jobs += viewModelScope.launch {
            val block = orchestrator.stores.views.current().firstOrNull { it.id == blockId }
            if (block != null && block is BlockView.LinkToObject.Default) {
                val params = block.appearanceParams
                val isIconVisible = params.withIcon == true
                val isSmallSize = isIconVisible && params.iconSize == LINK_ICON_SIZE_SMALL
                val isMediumSize = isIconVisible && params.iconSize == LINK_ICON_SIZE_MEDIUM
                val isLargeSize = isIconVisible && params.iconSize == LINK_ICON_SIZE_LARGE
                state.emit(
                    State.Success(
                        items = listOf(
                            Icon.None(isSelected = !isIconVisible),
                            Icon.Small(isSelected = isSmallSize),
                            Icon.Medium(isSelected = isMediumSize),
                            Icon.Large(isSelected = isLargeSize)
                        )
                    )
                )
            }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    fun onItemClicked(item: ObjectAppearanceSettingView) {
        when (item) {
            is Icon.Large -> TODO()
            is Icon.Medium -> TODO()
            is Icon.Small -> TODO()
            is Icon.None -> TODO()
            else -> {}
        }
    }

    class Factory(
        private val orchestrator: Orchestrator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceIconViewModel(orchestrator) as T
        }
    }

    sealed class State {
        data class Success(val items: List<Icon>) : State()
        object Dismiss : State()
    }
}