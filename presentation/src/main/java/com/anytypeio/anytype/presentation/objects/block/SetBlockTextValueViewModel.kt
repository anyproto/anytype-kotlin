package com.anytypeio.anytype.presentation.objects.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.updateText
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class SetBlockTextValueViewModel(
    private val updateText: UpdateText,
    private val storage: Editor.Storage
) : BaseViewModel() {

    private val doc: List<BlockView> get() = storage.views.current()
    val state = MutableStateFlow<ViewState>(ViewState.Loading)
    private val jobs = mutableListOf<Job>()

    fun onStart(tableId: Id, blockId: Id) {
        jobs += viewModelScope.launch {
            storage.views.stream().mapNotNull { views ->
                val table = views.firstOrNull { it.id == tableId }
                if (table != null && table is BlockView.Table) {
                    val block = table.cells.firstOrNull { cell ->
                        when (cell) {
                            is BlockView.Table.Cell.Empty -> cell.getId() == blockId
                            is BlockView.Table.Cell.Text -> cell.getId() == blockId
                            BlockView.Table.Cell.Space -> false
                        }
                    }
                    if (block is BlockView.Table.Cell.Text) {
                        block.block.copy(inputAction = BlockView.InputAction.Done)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.collectLatest { block ->
                state.value = ViewState.Success(data = listOf(block))
            }
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onKeyboardDoneKeyClicked(
        ctx: Id,
        tableId: String,
        targetId: String,
        text: String,
        marks: List<Markup.Mark>,
        markup: List<Block.Content.Text.Mark>
    ) {
        viewModelScope.launch {
            storage.views.update(doc.map { view ->
                if (view.id == tableId && view is BlockView.Table) {
                    val updated = view.cells.map { it ->
                        if (it is BlockView.Table.Cell.Text && it.block.id == targetId) {
                            it.copy(block = it.block.copy(text = text, marks = marks))
                        } else {
                            it
                        }
                    }
                    view.copy(cells = updated)
                } else {
                    view
                }
            })
        }

        val update = TextUpdate.Default(target = targetId, text = text, markup = markup)

        val updated = storage.document.get().map { block ->
            if (block.id == update.target) {
                block.updateText(update)
            } else
                block
        }
        storage.document.update(updated)

        viewModelScope.launch {
            updateText(
                UpdateText.Params(
                    context = ctx,
                    target = targetId,
                    text = text,
                    marks = markup
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while updating block text value")
                    _toasts.emit("Error while updating block text value ${e.localizedMessage}")
                },
                success = { state.value = ViewState.Exit }
            )
        }
    }

    fun onClickListener(clicked: ListenerType) {
        if (clicked is ListenerType.Mention) {
            state.value = ViewState.OnMention(clicked.target)
        }
    }

    class Factory(
        private val updateText: UpdateText,
        private val storage: Editor.Storage
    ) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetBlockTextValueViewModel(
                updateText = updateText,
                storage = storage
            ) as T
        }
    }

    sealed class ViewState {
        data class Success(val data: List<BlockView>) : ViewState()
        data class OnMention(val targetId: String) : ViewState()
        object Exit : ViewState()
        object Loading : ViewState()
    }
}