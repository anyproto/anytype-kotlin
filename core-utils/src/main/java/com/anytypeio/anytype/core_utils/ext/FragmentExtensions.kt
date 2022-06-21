package com.anytypeio.anytype.core_utils.ext

import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <reified T> Fragment.arg(key: String): T {
    return checkNotNull(requireArguments().get(key)) {
        "Fragment args missing value for $key"
    } as T
}

inline fun <reified T> Fragment.argOrNull(key: String): T? {
    return arguments?.get(key) as T?
}

fun Fragment.argString(key: String): String {
    val value = requireArguments().getString(key)
    return checkNotNull(value) { "Value missing for $key" }
}

fun Fragment.argStringOrNull(key: String): String? {
    return requireArguments().getString(key)
}

fun Fragment.argInt(key: String): Int {
    return requireArguments().getInt(key)
}

fun Fragment.argLong(key: String): Long {
    return requireArguments().getLong(key)
}

fun <T : Parcelable> Fragment.argList(key: String): ArrayList<T> {
    val value = requireArguments().getParcelableArrayList<T>(key)
    return checkNotNull(value)
}

fun <T> CoroutineScope.subscribe(flow: Flow<T>, body: suspend (T) -> Unit): Job =
    flow.onEach { body(it) }.launchIn(this)