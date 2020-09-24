package com.anytypeio.anytype.core_ui.widgets.text

import android.view.View

class EditorLongClickListener<T>(t: T, private val click: (T) -> Unit) :
    View.OnLongClickListener {

    var value: T = t

    override fun onLongClick(view: View?): Boolean =
        if (view != null && !view.hasFocus()) {
            click(value)
            true
        } else {
            false
        }
}