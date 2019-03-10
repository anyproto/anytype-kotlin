package com.agileburo.anytype.ui

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.R

class EditorToolbar : ConstraintLayout {

    private lateinit var btnBold: ImageView
    private lateinit var btnItalic: ImageView
    private lateinit var btnStrokeThrough: ImageView

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_anytype_editor_toolbar, this)

        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnStrokeThrough = findViewById(R.id.btnStroke)
    }

    fun setMainActions(
        boldClick: (Boolean) -> Unit,
        italicClick: (Boolean) -> Unit,
        strokeClick: (Boolean) -> Unit
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
    }

}