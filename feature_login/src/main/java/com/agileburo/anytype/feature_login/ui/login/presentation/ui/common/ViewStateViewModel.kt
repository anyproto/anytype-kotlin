package com.agileburo.anytype.feature_login.ui.login.presentation.ui.common

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

abstract class ViewStateViewModel<ViewState> : ViewModel() {
    protected val subscriptions by lazy { CompositeDisposable() }
    protected val state by lazy { BehaviorRelay.create<ViewState>() }
    open fun observeViewState(): Observable<ViewState> = state

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}
