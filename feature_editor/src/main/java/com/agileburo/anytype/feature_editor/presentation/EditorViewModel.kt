package com.agileburo.anytype.feature_editor.presentation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.ui.EditBlockAction
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EditorViewModel(
    private val interactor: EditorInteractor,
    private val contentTypeConverter: BlockContentTypeConverter
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val progress = BehaviorRelay.create<EditorState>()

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
            is EditBlockAction.NumberedClick -> convertBlock(block = action.block, contentType = ContentType.OL)
            is EditBlockAction.CheckBoxClick -> convertBlock(block = action.block, contentType = ContentType.Check)
            is EditBlockAction.CodeClick -> convertBlock(block = action.block, contentType = ContentType.Code)
        }.also { progress.accept(EditorState.HideToolbar) }

    fun onBlockClicked(block: Block) =
        progress.accept(
            EditorState.ShowToolbar(
                block = block,
                typesToHide = contentTypeConverter.getForbiddenTypes(block.contentType)
            )
        )

    private fun convertBlock(block: Block, contentType: ContentType) {
        if (block.contentType != contentType)
            progress.accept(EditorState.Update(contentTypeConverter.convert(block, contentType)))
    }


    fun getBlocks() {
        interactor.getBlocks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { blocks -> progress.accept(EditorState.Result(blocks)) },
                { error -> Timber.d("Get blocks error : $error") }
            )
            .disposedBy(disposable)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
