package com.agileburo.anytype.feature_editor.presentation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_editor.disposedBy
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
            is EditBlockAction.TextClick -> {
                progress.accept(EditorState.Update(action.block.copy(contentType = ContentType.P)))
            }
            is EditBlockAction.Header1Click -> {
                progress.accept(EditorState.Update(action.block.copy(contentType = ContentType.H1)))
            }
            is EditBlockAction.Header2Click -> {
                progress.accept(EditorState.Update(action.block.copy(contentType = ContentType.H2)))
            }
            is EditBlockAction.Header3Click -> {
                progress.accept(EditorState.Update(action.block.copy(contentType = ContentType.H3)))
            }
            is EditBlockAction.HighLightClick -> {
            }
            is EditBlockAction.BulletClick -> {
            }
        }
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
