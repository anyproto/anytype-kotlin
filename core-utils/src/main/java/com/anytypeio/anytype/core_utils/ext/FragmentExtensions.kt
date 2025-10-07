package com.anytypeio.anytype.core_utils.ext

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_utils.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

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

fun Fragment.argBoolean(key: String): Boolean {
    return requireArguments().getBoolean(key)
}

fun Fragment.argLong(key: String): Long {
    return requireArguments().getLong(key)
}

fun <T : Parcelable> Fragment.argList(key: String): ArrayList<T> {
    val value = requireArguments().getParcelableArrayList<T>(key)
    return checkNotNull(value)
}

fun Fragment.argStringList(key: String): ArrayList<String> {
    return requireArguments().getStringArrayList(key) ?: ArrayList()
}

fun <T> CoroutineScope.subscribe(flow: Flow<T>, body: suspend (T) -> Unit): Job =
    flow.cancellable().onEach { body(it) }.launchIn(this)

fun <T> Fragment.subscribe(flow: Flow<T>, body: (T) -> Unit): Job =
    flow.cancellable().onEach { body(it) }.launchIn(viewLifecycleOwner.lifecycleScope)

fun Fragment.startMarketPageOrWeb() {
    runCatching {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("${getString(R.string.play_market_url)}${context?.packageName}")
            )
        )
    }.onFailure {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.download_anytype_url))
            )
        )
    }
}

inline fun <reified T> Fragment.withParentSafe(action: T.() -> Unit) {
    if (parentFragment is T) {
        (parentFragment as T).action()
    } else {
        Timber.w("Invalid parent fragment: fragment is not ${T::class.java}")
    }
}