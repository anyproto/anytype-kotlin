package com.anytypeio.anytype.core_utils.ext

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <reified T> Fragment.arg(key: String): T {
    return requireArguments().get(key) as T
}

inline fun <reified T> Fragment.argOrNull(key: String): T? {
    return requireArguments().get(key) as T?
}

inline fun <reified T> Fragment.withParent(action: T.() -> Unit) {
    check(parentFragment is T) { "Parent is not ${T::class.java}. Please specify correct type" }
    (parentFragment as T).action()
}

fun <T> CoroutineScope.subscribe(flow: Flow<T>, body: suspend (T) -> Unit) {
    flow.onEach { body(it) }.launchIn(this)
}