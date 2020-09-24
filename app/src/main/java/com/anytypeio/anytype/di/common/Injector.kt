package com.anytypeio.anytype.di.common

import androidx.fragment.app.Fragment
import com.anytypeio.anytype.app.AndroidApplication

fun Fragment.componentManager(): ComponentManager {
    return (requireActivity().applicationContext as AndroidApplication).componentManager
}