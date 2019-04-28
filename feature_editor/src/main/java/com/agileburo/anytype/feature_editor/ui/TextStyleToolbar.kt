package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.agileburo.anytype.feature_editor.R
import kotlinx.android.synthetic.main.view_text_style_toolbar.view.*

class TextStyleToolbar : ConstraintLayout {

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initialize(context)
    }

    private fun initialize(context: Context) =
        View.inflate(context, R.layout.view_text_style_toolbar, this)

    fun show() {
        unselectButtons()
    }

    fun setMainActions(
        boldClick: (Boolean) -> Unit,
        italicClick: (Boolean) -> Unit,
        strokeClick: (Boolean) -> Unit,
        underlineClick: (Boolean) -> Unit
    ) {
        setClick(btnBold, boldClick)
        setClick(btnItalic, italicClick)
        setClick(btnStroke, strokeClick)
        setClick(btnUnderline, underlineClick)
    }

    private fun setClick(view: View, click: (Boolean) -> Unit) =
        view.setOnClickListener {
            it.isSelected = !it.isSelected
            click(it.isSelected)
        }

    private fun unselectButtons() {
        btnBold.isSelected = false
        btnItalic.isSelected = false
        btnStroke.isSelected = false
    }

}