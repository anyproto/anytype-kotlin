package com.agileburo.anytype.feature_editor

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 21.03.2019.
 */

fun Disposable.disposedBy(subscriptions: CompositeDisposable) {
    subscriptions.add(this)
}
