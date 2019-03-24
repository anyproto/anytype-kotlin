package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.feature_editor.R

class EditBlockToolbar : ConstraintLayout {

    private lateinit var btnText: ImageView
    private lateinit var btnHeader1: ImageView
    private lateinit var btnHeader2: ImageView
    private lateinit var btnHeader3: ImageView
    private lateinit var btnHightLighted: ImageView
    private lateinit var btnBulleted: ImageView

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
        View.inflate(context, R.layout.view_edit_block_toolbar, this)

        btnText = findViewById(R.id.btnText)
        btnHeader1 = findViewById(R.id.btnHeader1)
        btnHeader2 = findViewById(R.id.btnHeader2)
        btnHeader3 = findViewById(R.id.btnHeader3)
        btnHightLighted = findViewById(R.id.btnHighlighted)
        btnBulleted = findViewById(R.id.btnBulleted)
    }

    fun setMainActions(
        textClick: (Boolean) -> Unit,
        header1Click: (Boolean) -> Unit,
        header2Click: (Boolean) -> Unit,
        header3Click: (Boolean) -> Unit,
        hightLitedClick: (Boolean) -> Unit,
        bulledClick: (Boolean) -> Unit
    ) {
        btnText.setOnClickListener {
            it.isSelected = !it.isSelected
            textClick(it.isSelected)
        }
        btnHeader1.setOnClickListener {
            it.isSelected = !it.isSelected
            header1Click(it.isSelected)
        }
        btnHeader2.setOnClickListener {
            it.isSelected = !it.isSelected
            header2Click(it.isSelected)
        }
        btnHeader3.setOnClickListener {
            it.isSelected = !it.isSelected
            header3Click(it.isSelected)
        }
        btnHightLighted.setOnClickListener {
            it.isSelected = !it.isSelected
            hightLitedClick(it.isSelected)
        }
        btnBulleted.setOnClickListener {
            it.isSelected = !it.isSelected
            bulledClick(it.isSelected)
        }

    }
}