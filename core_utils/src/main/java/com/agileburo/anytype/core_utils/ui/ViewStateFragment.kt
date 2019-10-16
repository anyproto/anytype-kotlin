package com.agileburo.anytype.core_utils.ui

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer

abstract class ViewStateFragment<VS>(
    @LayoutRes private val layout: Int
) : BaseFragment(layout), Observer<VS> {
    override fun onChanged(state: VS) {
        render(state)
    }

    abstract fun render(state: VS)
}