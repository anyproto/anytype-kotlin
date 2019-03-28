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

class EditorViewModel(private val interactor: EditorInteractor) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val progress = BehaviorRelay.create<EditorState>()

    fun observeState() = progress

    fun onBlockClicked(action: EditBlockAction) {
        when (action) {
            is EditBlockAction.TextClick -> accept(block = action.block, contentType = ContentType.P)
            is EditBlockAction.Header1Click -> accept(block = action.block, contentType = ContentType.H1)
            is EditBlockAction.Header2Click -> accept(block = action.block, contentType = ContentType.H2)
            is EditBlockAction.Header3Click -> accept(block = action.block, contentType = ContentType.H3)
            is EditBlockAction.HighLightClick -> accept(block = action.block, contentType = ContentType.Quote)
            is EditBlockAction.BulletClick -> accept(block = action.block, contentType = ContentType.UL)
            is EditBlockAction.NumberedClick -> accept(block = action.block, contentType = ContentType.OL)
            is EditBlockAction.CheckBoxClick -> accept(block = action.block, contentType = ContentType.Check)
            is EditBlockAction.CodeClick -> accept(block = action.block, contentType = ContentType.Code)
        }
    }

    private fun accept(block: Block, contentType: ContentType) =
        progress.accept(EditorState.Update(block.copy(contentType = contentType)))

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
