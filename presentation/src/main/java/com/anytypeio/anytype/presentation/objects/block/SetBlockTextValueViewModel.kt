package com.anytypeio.anytype.presentation.objects.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.updateText
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCopyBlockEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsPasteBlockEvent
import com.anytypeio.anytype.presentation.mapper.mark
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class SetBlockTextValueViewModel(
    private val updateText: UpdateText,
    private val storage: Editor.Storage,
    private val dispatcher: Dispatcher<Payload>,
    private val paste: Paste,
    private val copy: Copy,
    private val analytics: Analytics
) : BaseViewModel() {

    val state = MutableStateFlow<ViewState>(ViewState.Loading)
    private val jobs = mutableListOf<Job>()

    fun onStart(tableId: Id, cellId: Id) {
        awaitTextBlockFromStorage(tableId = tableId, cellId = cellId)
    }

    private fun awaitTextBlockFromStorage(tableId: Id, cellId: Id) {
        jobs += viewModelScope.launch {
            storage.views.stream().mapNotNull { views ->
                val blockText = getCellTextBlockFromTable(views, tableId, cellId)
                blockText?.copy(
                    inputAction = BlockView.InputAction.Done,
                    isFocused = blockText.text.isEmpty()
                )
            }.take(1)
                .collectLatest {
                    state.value = ViewState.Success(data = listOf(it))
                }
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onTextBlockTextChanged(textBlock: BlockView.Text, tableId: Id, cellId: Id, ctx: Id) {
        Timber.d("onTextBlockTextChanged, textBlock:[$textBlock]")

        val newViews = storage.views.current().map { view ->
            if (view.id == tableId && view is BlockView.Table) {
                val newCells = view.cells.map { cell ->
                    if (cell is BlockView.Table.Cell.Text && cell.block.id == textBlock.id) {
                        cell.copy(
                            block = cell.block.copy(
                                text = textBlock.text,
                                marks = textBlock.marks
                            )
                        )
                    } else {
                        cell
                    }
                }
                view.copy(cells = newCells)
            } else {
                view
            }
        }

        val textUpdate = TextUpdate.Default(
            target = cellId,
            text = textBlock.text,
            markup = textBlock.marks.map { it.mark() }
        )

        val newDocument = storage.document.get().map { block ->
            if (block.id == textUpdate.target) {
                block.updateText(textUpdate)
            } else
                block
        }

        storage.document.update(newDocument)
        viewModelScope.launch { storage.views.update(newViews) }

        viewModelScope.launch {
            updateText(
                params = UpdateText.Params(
                    context = ctx,
                    target = textBlock.id,
                    text = textBlock.text,
                    marks = textBlock.marks.map { it.mark() }
                )
            ).proceed(
                failure = { Timber.e(it) },
                success = {}
            )
        }
    }

    fun onKeyboardDoneKeyClicked() {
        state.value = ViewState.Exit
    }

    fun onClickListener(clicked: ListenerType) {
        if (clicked is ListenerType.Mention) {
            state.value = ViewState.OnMention(clicked.target)
        }
    }

    fun onPaddingsClick() {
        state.value = ViewState.Focus
    }

    fun onPaste(
        context: Id,
        range: IntRange,
        cellId: Id,
        tableId: Id
    ) {
        Timber.d("onPaste, range:[$range]")
        viewModelScope.launch {
            paste(
                params = Paste.Params(
                    context = context,
                    focus = cellId,
                    range = range,
                    selected = emptyList(),
                    isPartOfBlock = true
                )
            ).proceed(
                failure = { Timber.e(it) },
                success = { response ->
                    dispatcher.send(response.payload)
                    awaitTextBlockFromStorage(tableId = tableId, cellId = cellId)
                    analytics.sendAnalyticsPasteBlockEvent()
                }
            )
        }
    }

    fun onCopy(
        context: Id,
        range: IntRange?,
        cellId: Id
    ) {
        Timber.d("onCopy, range:[$range]")
        val block = storage.document.get().firstOrNull { it.id == cellId }
        if (block != null) {
            viewModelScope.launch {
                copy(
                    params = Copy.Params(
                        context = context,
                        range = range,
                        blocks = listOf(block)
                    )
                ).proceed(
                    failure = { Timber.e(it) },
                    success = { analytics.sendAnalyticsCopyBlockEvent() }
                )
            }
        }
    }

    private fun getCellTextBlockFromTable(
        views: List<BlockView>,
        tableId: Id,
        cellId: Id
    ): BlockView.Text.Paragraph? {
        val table = views.firstOrNull { it.id == tableId }
        return if (table != null && table is BlockView.Table) {
            val block = table.cells.firstOrNull { cell ->
                when (cell) {
                    is BlockView.Table.Cell.Empty -> cell.getId() == cellId
                    is BlockView.Table.Cell.Text -> cell.getId() == cellId
                }
            }
            if (block is BlockView.Table.Cell.Text) {
                block.block
            } else {
                null
            }
        } else {
            null
        }
    }

    class Factory(
        private val storage: Editor.Storage,
        private val dispatcher: Dispatcher<Payload>,
        private val paste: Paste,
        private val copy: Copy,
        private val updateText: UpdateText,
        private val analytics: Analytics
    ) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetBlockTextValueViewModel(
                storage = storage,
                dispatcher = dispatcher,
                paste = paste,
                copy = copy,
                updateText = updateText,
                analytics = analytics
            ) as T
        }
    }

    sealed class ViewState {
        data class Success(val data: List<BlockView>) : ViewState()
        data class OnMention(val targetId: String) : ViewState()
        object Exit : ViewState()
        object Loading : ViewState()
        object Focus : ViewState()
    }
}