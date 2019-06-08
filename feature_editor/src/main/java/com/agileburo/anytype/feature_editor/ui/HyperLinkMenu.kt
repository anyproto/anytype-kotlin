package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import com.agileburo.anytype.feature_editor.R

class HyperLinkMenu(
    private val context: Context,
    private val editText: EditText,
    private val start: Int, private val end: Int
) : PopupWindow(context) {

    init {
        setupView()
    }

    private fun setupView() {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_hyperlink, null)
        isFocusable = true
        isOutsideTouchable = true
        contentView = view
        setClicks()
    }

    private fun setClicks() {
        val edtLink = contentView.findViewById<EditText>(R.id.edtLink)
        contentView.findViewById<Button>(R.id.btnLink).setOnClickListener {
            editText.text = SpannableStringBuilder(editText.text).apply {
                setSpan(URLSpan(edtLink.text.toString()), start, end, 1)
            }
            dismiss()
        }
    }
}