package com.agileburo.anytype.feature_editor.presentation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_editor.disposedBy
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EditorViewModel(private val interactor: EditorInteractor) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val state: MutableList<Block> = mutableListOf()

    fun getBlocks() {
        interactor.getBlocks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ blocks: List<Block> ->
                blocks.forEach {
                    Timber.d("Block : ${it.id}")
                }
                state.addAll(blocks)
            },
                { t: Throwable ->
                    Timber.d("Get blocks error : $t")
                })
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
