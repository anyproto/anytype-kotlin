package com.agileburo.anytype.core_utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.Group

fun Context.dimen(res: Int): Float {
    return resources
        .getDimension(res)
}

fun Group.setOnClickListeners(listener: View.OnClickListener?) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun hideKeyboard(view: View?) {
    view?.also {
        val inputMethodManager = it.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!inputMethodManager.isActive) {
            return
        }
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }
}