package com.agileburo.anytype.feature_editor.presentation.mvvm

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.BaseSchedulerProvider
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.presentation.util.SwapRequest
import com.agileburo.anytype.feature_editor.ui.BlockMenuAction
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

const val useDiffUtils = true

class EditorViewModel(
    private val interactor: EditorInteractor,
    private val contentTypeConverter: BlockContentTypeConverter,
    private val schedulerProvider: BaseSchedulerProvider
) : ViewModel() {

    private val subscriptions by lazy { CompositeDisposable() }
    private val blocks by lazy { mutableListOf<Block>() }
    private val progress by lazy { BehaviorRelay.create<EditorState>() }

    private var positionInFocus: Int = -1

    init {
        fetchBlocks()
    }

    fun observeState() = progress

    fun onBlockChanged(block: Block) {
        val index = blocks.indexOfFirst { it.id == block.id }
        if (index >= 0 && index < blocks.size) {
            blocks[index] = block
        }
    }

    fun onBlockMenuAction(action: BlockMenuAction) {
        when (action) {
            is BlockMenuAction.ContentTypeAction -> {
                convertBlock(
                    block = blocks.first { it.id == action.id },
                    contentType = action.newType
                )
            }
            is BlockMenuAction.ArchiveAction -> {
                removeBlock(action.id)
            }
            is BlockMenuAction.DuplicateAction -> {
                throw NotImplementedError()
            }
        }
    }

    fun onBlockFocus(position: Int) {
        positionInFocus = position
    }

    fun onSwap(request: SwapRequest) {
        blocks.swap(request.from, request.to)
        progress.accept(EditorState.Swap(request))
    }

    fun onSwapFinished() {
        val normalized = contentTypeConverter.normalizeNumbers(blocks)
        blocks.clear()
        blocks.addAll(normalized)
        dispatchBlocksToView()
    }

    private fun clearBlockFocus() =
        blocks.getOrNull(positionInFocus)?.let {
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
                { error -> Timber.e(error, "Error while fetching blocks") }
            ).disposedBy(subscriptions)
    }

    private fun onBlockReceived(items: List<Block>) {
        blocks.addAll(items)
        progress.accept(EditorState.Result(blocks))
    }

    private fun convertBlock(block: Block, contentType: ContentType) =
        if (useDiffUtils)
            convertBlockDiffUtils(
                block, contentType
            ) else
            convertBlockWithoutDiffUtils(block, contentType)

    private fun convertBlockDiffUtils(block: Block, contentType: ContentType) {
        if (block.contentType != contentType) {

            val converted = contentTypeConverter.convert(
                blocks = blocks,
                targetType = contentType,
                target = block
            )

            blocks.clear()
            blocks.addAll(converted)

            dispatchBlocksToView()
        }
    }

    private fun convertBlockWithoutDiffUtils(block: Block, contentType: ContentType) {
        if (block.contentType != contentType) {
            blocks.first { it.id == block.id }.contentType = contentType
            block.contentType = contentType
            progress.accept(EditorState.Update(block))
        }
    }

    private fun removeBlock(id: String) {

        val index = blocks.indexOfFirst { it.id == id }

        require(index > -1 && index < blocks.size)

        blocks.removeAt(index)

        val converted = contentTypeConverter.normalizeNumbers(blocks)

        blocks.clear()
        blocks.addAll(converted)

        dispatchBlocksToView()
    }

    private fun dispatchBlocksToView() {
        progress.accept(EditorState.Updates(blocks))
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}
