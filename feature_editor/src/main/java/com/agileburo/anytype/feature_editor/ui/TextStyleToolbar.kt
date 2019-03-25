
package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.feature_editor.R
import kotlinx.android.synthetic.main.view_text_style_toolbar.view.*

class TextStyleToolbar : ConstraintLayout {

    private lateinit var btnBold: ImageView
    private lateinit var btnItalic: ImageView
    private lateinit var btnStrokeThrough: ImageView

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

    private fun initialize(context: Context) {
        View.inflate(context, R.layout.view_text_style_toolbar, this)

        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnStrokeThrough = findViewById(R.id.btnStroke)
    }

    fun setMainActions(
        boldClick: (Boolean) -> Unit,
        italicClick: (Boolean) -> Unit,
        strokeClick: (Boolean) -> Unit,
        underlineClick : (Boolean) -> Unit,
        codeBlockClick : (Boolean) -> Unit
    ) {
        btnBold.setOnClickListener {
            it.isSelected = !it.isSelected
            boldClick(it.isSelected)
        }
        btnItalic.setOnClickListener {
            it.isSelected = !it.isSelected
            italicClick(it.isSelected)
        }
        btnStrokeThrough.setOnClickListener {
            it.isSelected = !it.isSelected
            strokeClick(it.isSelected)
        }
        underline.setOnClickListener { button ->
            button.isSelected = !button.isSelected
            underlineClick(button.isSelected)
        }
        codeBlock.setOnClickListener { button ->
            button.isSelected = !button.isSelected
            codeBlockClick(button.isSelected)
        }

    }

}