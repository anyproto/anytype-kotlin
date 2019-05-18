package com.agileburo.anytype.core_utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object UIExtensions {

    fun hideSoftKeyBoard(activity: Activity, view: View?) {
        (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(
                view?.applicationWindowToken,
                0
            )
    }

}