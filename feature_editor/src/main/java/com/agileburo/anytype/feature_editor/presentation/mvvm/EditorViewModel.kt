package com.agileburo.anytype.feature_editor.presentation.mvvm

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.BaseSchedulerProvider
import com.agileburo.anytype.core_utils.shift
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.util.DragDropAction
import com.agileburo.anytype.feature_editor.ui.BlockMenuAction
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class EditorViewModel(
    private val interactor: EditorInteractor,
    private val contentTypeConverter: BlockContentTypeConverter,
    private val schedulerProvider: BaseSchedulerProvider
) : ViewModel() {

    private val subscriptions by lazy { CompositeDisposable() }
    private val document : Document by lazy { mutableListOf<Block>() }
    private val progress by lazy { BehaviorRelay.create<EditorState>() }

    private var positionInFocus: Int = -1

    init {
        fetchBlocks()
    }

    fun observeState() = progress

    fun onBlockContentChanged(block: Block) {
        document.updateContent(
            targetId = block.id,
            targetContentUpdate = block.content
        )
    }

    fun onExpandClicked(view : BlockView) {
        check(view is BlockView.ToggleView)

        document.flatSearch(view.id)?.let { block ->
            block.state.expanded = !view.expanded
            dispatchBlocksToView()
        }
    }

    fun onBlockMenuAction(action: BlockMenuAction) {
        when (action) {
            is BlockMenuAction.ContentTypeAction -> {
                document.changeContentType(targetId = action.id, targetType = action.newType)
                document.fixNumberOrder()
                dispatchBlocksToView()
            }
            is BlockMenuAction.ArchiveAction -> {
                removeBlock(action.id)
            }
            is BlockMenuAction.DuplicateAction -> {
                TODO()
            }
        }
    }

    fun onBlockDragAndDropAction(action: DragDropAction) = when (action) {
        is DragDropAction.Shift -> onShiftAction(from = action.from, to = action.to)
        is DragDropAction.Consume -> onConsumeAction(target = action.target, consumer = action.consumer)
    }

    private fun onShiftAction(from: Int, to: Int) {
        val newBlocks = document.shift(from, to)
        document.clear()
        document.addAll(newBlocks)
        dispatchBlocksToView()
        normalizeBlocks()
    }

    //Todo Update with proper consume action
    private fun onConsumeAction(target: Int, consumer: Int) {
        document.removeAt(target)
        progress.accept(EditorState.Remove(target))
        normalizeBlocks()
    }

    fun onConsumeRequested(consumerId : String, consumableId : String) {
        document.apply {
            consume(consumerId = consumerId, consumableId = consumableId)
            fixNumberOrder()
        }
        dispatchBlocksToView()
    }

    fun onMoveAfter(previousId : String, targetId : String) {
        document.apply {
            moveAfter(previousId, targetId)
            fixNumberOrder()
        }
        dispatchBlocksToView()
    }

    fun onBlockFocus(position: Int) {
        if (position == -1) {
            positionInFocus = position
            clearBlockFocus()
            return
        }
        progress.accept(EditorState.DragDropOff)
        positionInFocus = position
    }

    fun onEnterClicked(id : String) {

        Timber.d("On enter clicked with id: $id")

        document.apply {
            applyToAll { it.state.focused = false }
            insertNewBlockAfter(previousBlockId = id)
            fixNumberOrder()
        }
        dispatchBlocksToView()
    }


    private fun normalizeBlocks() {
        val normalized = contentTypeConverter.normalizeNumbers(document)
        document.clear()
        document.addAll(normalized)
        dispatchBlocksToView()
    }

    private fun clearBlockFocus() =
        document.getOrNull(positionInFocus)?.let {
            progress.accept(EditorState.DragDropOn)
            progress.accept(
                EditorState.ClearBlockFocus(positionInFocus, it.contentType)
            )
        }

    private fun fetchBlocks() {
        interactor.getBlocks()
            .observeOn(schedulerProvider.ui())
            .subscribeOn(schedulerProvider.io())
            .subscribe(
                { data -> onBlockReceived(data) },
                { error -> Timber.e(error, "Error while fetching document") }
            ).disposedBy(subscriptions)
    }

    private fun onBlockReceived(items: List<Block>) {
        document.addAll(items)
        progress.accept(EditorState.Result(document))
    }

    private fun removeBlock(id: String) {
        document.apply {
            delete(id)
            fixNumberOrder()
        }
        dispatchBlocksToView()
    }

    private fun dispatchBlocksToView() {
        progress.accept(EditorState.Updates(document))
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}
