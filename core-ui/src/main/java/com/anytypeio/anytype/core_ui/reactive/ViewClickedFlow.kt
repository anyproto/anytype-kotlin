package com.anytypeio.anytype.core_ui.reactive

import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun View.clicks(): Flow<Unit> = callbackFlow {
    checkMainThread()
    val listener = View.OnClickListener {
        trySend(Unit)
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate()

fun View.longClicks(withHaptic: Boolean = false): Flow<Unit> = callbackFlow {
    checkMainThread()
    val listener = View.OnLongClickListener {
        trySend(Unit)
        if (withHaptic) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        true
    }
    setOnLongClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate()

fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
    checkMainThread()
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun afterTextChanged(s: Editable) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            trySend(s)
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}.conflate()

fun EditText.focusChanges(): Flow<Boolean> = callbackFlow {
    checkMainThread()
    val listener = View.OnFocusChangeListener { _, hasFocus -> trySend(hasFocus) }
    onFocusChangeListener = listener
    awaitClose { onFocusChangeListener = null }
}.conflate()


fun View.touches(handled: (MotionEvent) -> Boolean = { true }): Flow<MotionEvent> =
    callbackFlow<MotionEvent> {
        checkMainThread()
        val listener = View.OnTouchListener { _, event ->
            performClick()
            if (handled(event)) {
                trySend(event)
                true
            } else {
                false
            }
        }
        setOnTouchListener(listener)
        awaitClose { setOnTouchListener(null) }
    }.conflate()

fun EditText.afterTextChanges(): Flow<CharSequence> = callbackFlow<CharSequence> {
    checkMainThread()
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable) {
            trySend(s.toString())
        }
    }
    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}.conflate()

fun View.layoutChanges(): Flow<Unit> = callbackFlow {
    checkMainThread()
    val listener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> trySend(Unit) }
    addOnLayoutChangeListener(listener)
    awaitClose { removeOnLayoutChangeListener(listener) }
}.conflate()

fun TextView.editorActionEvents(
    handled: (Int) -> Boolean
): Flow<Int> = callbackFlow {
    checkMainThread()
    val listener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (handled(actionId)) {
            trySend(actionId)
            true
        } else {
            false
        }
    }
    setOnEditorActionListener(listener)
    awaitClose { setOnEditorActionListener(null) }
}.conflate()

fun checkMainThread() = check(Looper.myLooper() == Looper.getMainLooper()) {
    "Expected to be called on the main thread but was " + Thread.currentThread().name
}

/**
 * Should be called from [BaseFragment.onStart]
 */
fun BaseFragment<*>.click(
    view: View,
    action: () -> Unit
) {
    jobs += view.clicks()
        .throttleFirst()
        .onEach { action() }
        .launchIn(lifecycleScope)
}


/**
 * Should be called from [BaseBottomSheetFragment.onStart]
 */
fun BaseBottomSheetFragment<*>.click(
    view: View,
    action: () -> Unit
) {
    jobs += view.clicks()
        .throttleFirst()
        .onEach { action() }
        .launchIn(lifecycleScope)
}