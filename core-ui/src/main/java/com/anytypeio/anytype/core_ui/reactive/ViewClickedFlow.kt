package com.anytypeio.anytype.core_ui.reactive

import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.CheckResult
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

@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
fun EditText.textChanges(): Flow<CharSequence> = callbackFlow<CharSequence> {
    checkMainThread()
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun afterTextChanged(s: Editable) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            safeOffer(s)
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}.conflate()

@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
fun EditText.afterTextChanges(): Flow<CharSequence> = callbackFlow<CharSequence> {
    checkMainThread()
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable) {
            safeOffer(s.toString())
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}.conflate()

@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
fun TextView.editorActionEvents(
    handled: (Int) -> Boolean
): Flow<Int> = callbackFlow {
    checkMainThread()
    val listener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (handled(actionId)) {
            safeOffer(actionId)
            true
        } else {
            false
        }
    }
    setOnEditorActionListener(listener)
    awaitClose { setOnEditorActionListener(null) }
}.conflate()


@UseExperimental(ExperimentalCoroutinesApi::class)
fun <E> SendChannel<E>.safeOffer(value: E): Boolean {
    return runCatching { offer(value) }.getOrDefault(false)
}

fun checkMainThread() = check(Looper.myLooper() == Looper.getMainLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
}