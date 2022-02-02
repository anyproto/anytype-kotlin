package com.anytypeio.anytype.presentation.objects.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ObjectAppearanceCoverViewModel(
    private val orchestrator: Orchestrator
) : ViewModel() {

    val state = MutableSharedFlow<State>(replay = 0)
    private val jobs = mutableListOf<Job>()

    fun onStart(ctx: Id) {
        jobs += viewModelScope.launch {
            val block = orchestrator.stores.views.current().firstOrNull { it.id == ctx }
            if (block != null && block is BlockView.LinkToObject.Default) {
                val params = block.appearanceParams
            }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    class Factory(
        private val orchestrator: Orchestrator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectAppearanceCoverViewModel(orchestrator) as T
        }
    }

    sealed class State {
        data class Success(val items: List<ObjectAppearanceSettingView>) : State()
        object Dismiss : State()
    }
}