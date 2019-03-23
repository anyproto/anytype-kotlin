package com.agileburo.anytype.feature_editor.presentation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import com.agileburo.anytype.feature_editor.ui.EditorState
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EditorViewModel(private val interactor: EditorInteractor) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val state: MutableList<Block> = mutableListOf()

    private val progress = BehaviorRelay.create<EditorState>()

    fun observeState() = progress

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

    fun persist() {
        interactor.saveState(state)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
