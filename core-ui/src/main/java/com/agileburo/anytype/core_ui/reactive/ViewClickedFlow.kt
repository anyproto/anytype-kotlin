package com.agileburo.anytype.core_ui.reactive

import android.os.Looper
import android.view.View
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@UseExperimental(ExperimentalCoroutinesApi::class)
fun View.clicks(): Flow<Unit> = callbackFlow<Unit> {
    checkMainThread()
    val listener = View.OnClickListener {
        safeOffer(Unit)
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate()

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <E> SendChannel<E>.safeOffer(value: E): Boolean {
    return runCatching { offer(value) }.getOrDefault(false)
}

fun checkMainThread() = check(Looper.myLooper() == Looper.getMainLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
}