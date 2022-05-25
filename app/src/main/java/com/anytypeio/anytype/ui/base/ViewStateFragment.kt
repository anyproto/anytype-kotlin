package com.anytypeio.anytype.ui.base

import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding

abstract class ViewStateFragment<VS, BINDING : ViewBinding>(
    @LayoutRes private val layout: Int
) : NavigationFragment<BINDING>(layout), Observer<VS> {
    override fun onChanged(state: VS) {
        render(state)
    }

    abstract fun render(state: VS)
}