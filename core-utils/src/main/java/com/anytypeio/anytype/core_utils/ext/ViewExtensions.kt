package com.anytypeio.anytype.core_utils.ext

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.setVisible(visible: Boolean) {
    if (visible) this.visible() else this.invisible()
}

fun View.showSnackbar(text: String) {
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
}

fun View.showSnackbar(msgId: Int, length: Int) = showSnackbar(context.getString(msgId), length)
fun View.showSnackbar(msg: String, length: Int) = showSnackbar(msg, length, null, {})

fun View.showSnackbar(
    msgId: Int,
    length: Int,
    actionMessageId: Int,
    action: (View) -> Unit
): Snackbar =
    showSnackbar(context.getString(msgId), length, context.getString(actionMessageId), action)

fun View.showSnackbar(
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
): Snackbar {
    val snackbar = Snackbar.make(this, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }
    }
    snackbar.show()
    return snackbar
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (!inputMethodManager.isActive) return
    inputMethodManager.hideSoftInputFromWindow(
        windowToken,
        InputMethodManager.RESULT_UNCHANGED_SHOWN
    )
}

fun Activity.hideSoftInput() {
    val imm: InputMethodManager? = getSystemService()
    val currentFocus = currentFocus
    if (currentFocus != null && imm != null) {
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

fun Fragment.hideSoftInput() = requireActivity().hideSoftInput()

fun RecyclerView.containsItemDecoration(decoration: RecyclerView.ItemDecoration): Boolean {
    if (itemDecorationCount > 0) {
        for (i in 0..itemDecorationCount.dec()) {
            val d = getItemDecorationAt(i)
            if (d == decoration)
                return true
        }
        return false
    } else {
        return false
    }
}

fun RecyclerView.smoothSnapToPosition(
    position: Int,
    snapMode: Int = LinearSmoothScroller.SNAP_TO_START,
    scrollDuration: Float = 500f) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return scrollDuration / computeHorizontalScrollRange();
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

val Activity.statusBarHeight: Int
    get() {
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top
    }

fun EditText.showKeyboard() {
    post {
        this.apply {
            if (!hasFocus()) {
                if (requestFocus()) {
                    context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
                } else {
                    Timber.d("Couldn't gain focus")
                }
            } else {
                Timber.d("Already had focus")
            }
        }
    }
}