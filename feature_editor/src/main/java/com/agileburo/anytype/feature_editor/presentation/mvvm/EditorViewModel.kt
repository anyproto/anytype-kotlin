package com.agileburo.anytype.feature_editor.presentation.mvvm

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.core_utils.BaseSchedulerProvider
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.presentation.converter.BlockContentTypeConverter
import com.agileburo.anytype.feature_editor.ui.EditBlockAction
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EditorViewModel(
    private val interactor: EditorInteractor,
    private val contentTypeConverter: BlockContentTypeConverter,
    private val schedulerProvider: BaseSchedulerProvider
) : ViewModel() {

    private val subscriptions by lazy { CompositeDisposable() }
    private val blocks by lazy { mutableListOf<Block>() }
    private val progress by lazy { BehaviorRelay.create<EditorState>() }

    init {
        fetchBlocks()
    }

    fun observeState() = progress

    fun onContentTypeClicked(action: EditBlockAction) =
        when (action) {
            is EditBlockAction.TextClick -> convertBlock(block = action.block, contentType = ContentType.P)
            is EditBlockAction.Header1Click -> convertBlock(block = action.block, contentType = ContentType.H1)
            is EditBlockAction.Header2Click -> convertBlock(block = action.block, contentType = ContentType.H2)
            is EditBlockAction.Header3Click -> convertBlock(block = action.block, contentType = ContentType.H3)
            is EditBlockAction.Header4Click -> convertBlock(block = action.block, contentType = ContentType.H4)
            is EditBlockAction.HighLightClick -> convertBlock(block = action.block, contentType = ContentType.Quote)
            is EditBlockAction.BulletClick -> convertBlock(block = action.block, contentType = ContentType.UL)
            is EditBlockAction.NumberedClick -> convertBlock(block = action.block, contentType = ContentType.NumberedList)
            is EditBlockAction.CheckBoxClick -> convertBlock(block = action.block, contentType = ContentType.Check)
            is EditBlockAction.CodeClick -> convertBlock(block = action.block, contentType = ContentType.Code)
            is EditBlockAction.ArchiveBlock -> removeBlock(id = action.id)
        }.also { progress.accept(EditorState.HideToolbar) }
            .also { progress.accept(EditorState.HideLinkChip) }

    fun hideToolbar() = progress.accept(EditorState.HideToolbar)

    fun onBlockClicked(id: String) = blocks.first { it.id == id }.let {
        progress.accept(
            EditorState.ShowToolbar(
                block = it,
                typesToHide = contentTypeConverter.getForbiddenTypes(it.contentType)
            )
        )
        progress.accept(EditorState.HideLinkChip)
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

    private fun onBlockReceived(items : List<Block>) {
        blocks.addAll(items)
        progress.accept(EditorState.Result(blocks))
    }

    private fun convertBlock(block: Block, contentType: ContentType) {

        if (block.contentType != contentType) {

            val converted = contentTypeConverter.convert(
                blocks = blocks,
                targetType = contentType,
                target = block
            )

            blocks.clear()
            blocks.addAll(converted)

            progress.accept(EditorState.Updates(blocks))


        }
    }

    private fun removeBlock(id: String) {

        val index = blocks.indexOfFirst { it.id == id }

        require(index > -1 && index < blocks.size)

        blocks.removeAt(index)

        val converted = contentTypeConverter.normalizeNumbers(blocks)

        blocks.clear()
        blocks.addAll(converted)

        progress.accept(EditorState.Updates(blocks))
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}
