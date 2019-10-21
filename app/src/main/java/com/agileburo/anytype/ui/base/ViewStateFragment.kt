package com.agileburo.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer

abstract class ViewStateFragment<VS>(
    @LayoutRes private val layout: Int
) : NavigationFragment(layout), Observer<VS> {
    override fun onChanged(state: VS) {
        render(state)
    }

    abstract fun render(state: VS)
}