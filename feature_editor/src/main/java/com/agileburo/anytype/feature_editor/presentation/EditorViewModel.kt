package com.agileburo.anytype.feature_editor.presentation

import androidx.lifecycle.ViewModel
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.EditorInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class EditorViewModel(private val interactor: EditorInteractor) : ViewModel() {

    val disposable = CompositeDisposable()

    fun getBlocks() {
        disposable.addAll(
            interactor.getBlocks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({t: List<Block>? ->
                    Timber.d("Get blocks success : $t")
                },
                    {t: Throwable? ->
                        Timber.d("Get blocks error : $t")
                    })
        )
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}
