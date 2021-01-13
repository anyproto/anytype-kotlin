package com.anytypeio.anytype.core_utils.ext

import androidx.fragment.app.Fragment

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