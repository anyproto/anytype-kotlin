package com.anytypeio.anytype.core_utils.ext

import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.arg(key: String): T {
    return requireArguments().get(key) as T
}

inline fun <reified T> Fragment.argOrNull(key: String): T? {
    return requireArguments().get(key) as T?
}