package com.anytypeio.anytype.core_utils.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.CustomGridLayoutManager.LayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.google.android.material.snackbar.Snackbar


fun View.showActionableSnackBar(
    from: String?,
    to: String?,
    icon: ObjectIcon,
    @StringRes middleString: Int,
    anchor: View? = null,
    click: () -> Unit
) {

    val snackbar: Snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG)

    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    val snackbarLayout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    snackbarLayout.setPadding(0, 0, 0, 0)

    val newView: View = LayoutInflater.from(context).inflate(R.layout.snackbar_actionable, null)
    newView.setOnClickListener { snackbar.dismiss() }

    with(newView.findViewById<TextView>(R.id.snackbar_text)) {
        if (from.isNullOrEmpty()) {
            this.text =
                "${resources.getString(R.string.untitled)} ${resources.getString(middleString)}"
        } else {
            this.text = "$from ${resources.getString(middleString)}"
        }
    }

    with(newView.findViewById<TextView>(R.id.snackbar_action)) {
        if (!to.isNullOrEmpty()) {
            this.text = to
        }
        setOnClickListener {
            click()
            snackbar.dismiss()
        }
    }
    if (icon != ObjectIcon.None) {
        with(newView.findViewById<ObjectIconWidget>(R.id.icon)) {
            isVisible = true
            setIcon(icon)
        }
    }

    snackbarLayout.addView(newView, 0)
    snackbar.anchorView = anchor

    snackbar.show()
}

fun View.showMessageSnackBar(text: String, anchor: View? = null) {

    val snackbar: Snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG)

    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    val snackbarLayout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    snackbarLayout.setPadding(0, 0, 0, 0)

    val newView: View = LayoutInflater.from(context).inflate(R.layout.snackbar_message, null)
    newView.setOnClickListener { snackbar.dismiss() }

    newView.findViewById<TextView>(R.id.snackbar_text).text = text

    snackbarLayout.addView(newView, 0)
    snackbar.anchorView = anchor

    snackbar.show()
}

fun View.showMessageIconSnackBar(
    textStart: String,
    textEnd: String,
    icon: ObjectIcon,
    anchor: View? = null
) {

    val snackbar: Snackbar = Snackbar.make(this, "", Snackbar.LENGTH_LONG)

    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    snackbar.view.invisible()

    val snackbarLayout: Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout
    snackbarLayout.updateLayoutParams<FrameLayout.LayoutParams> {
        width = LayoutParams.WRAP_CONTENT
        gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
    }

    val newView: View = LayoutInflater.from(context).inflate(R.layout.snackbar_msg_icon, null)
    newView.setOnClickListener { snackbar.dismiss() }

    newView.findViewById<TextView>(R.id.snackbar_text_start).text = textStart
    newView.findViewById<TextView>(R.id.snackbar_text_end).text = textEnd
    newView.findViewById<ObjectIconWidget>(R.id.snackbar_icon).setIcon(icon)

    snackbarLayout.addView(newView, 0)
    snackbar.anchorView = anchor

    snackbar.show()
}