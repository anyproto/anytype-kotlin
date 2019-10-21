package com.agileburo.anytype.di.common

import androidx.fragment.app.Fragment
import com.agileburo.anytype.app.AndroidApplication

fun Fragment.componentManager(): ComponentManager {
    return (requireActivity().applicationContext as AndroidApplication).componentManager
}