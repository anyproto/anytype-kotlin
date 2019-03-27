package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentType
import kotlinx.android.synthetic.main.view_edit_block_toolbar.view.*

class EditBlockToolbar : ConstraintLayout {

    private var block = Block("", "", ContentType.P, content = Content.Text("", emptyList()))

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

    fun show(typesToHide: List<ContentType>, initialBlock: Block) {
        typesToHide.forEach { getButton(it).visibility = View.GONE }
        getButton(initialBlock.contentType).isSelected = true
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_edit_block_toolbar, this)
    }

    fun setMainActions(
        textClick: (Block) -> Unit,
        header1Click: (Block) -> Unit,
        header2Click: (Block) -> Unit,
        header3Click: (Block) -> Unit,
        hightLitedClick: (Block) -> Unit,
        bulledClick: (Block) -> Unit
    ) {
        btnText.setOnClickListener {
            it.isSelected = !it.isSelected
            textClick(block)
        }
        btnHeader1.setOnClickListener {
            it.isSelected = !it.isSelected
            header1Click(block)
        }
        btnHeader2.setOnClickListener {
            it.isSelected = !it.isSelected
            header2Click(block)
        }
        btnHeader3.setOnClickListener {
            it.isSelected = !it.isSelected
            header3Click(block)
        }
        btnHighlighted.setOnClickListener {
            it.isSelected = !it.isSelected
            hightLitedClick(block)
        }
        btnBulleted.setOnClickListener {
            it.isSelected = !it.isSelected
            bulledClick(block)
        }

    }

    private fun getButton(type: ContentType) =
        when (type) {
            ContentType.P -> btnText
            ContentType.H2 -> btnHeader1
            ContentType.H3 -> btnHeader2
            ContentType.H4 -> btnHeader3
            ContentType.UL -> btnBulleted
            ContentType.Toggle -> btnCode
            ContentType.Quote -> btnHighlighted
            ContentType.OL -> btnNumberedList
            ContentType.Check -> btnCheckbox
            ContentType.Code -> btnCode
            else -> btnText
        }
}

fun EditBlockToolbar.hide() = {
    types.isSelected = false
   // this.visibility = View.INVISIBLE
}