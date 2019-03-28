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
        unSelectViews()
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
        bulletedClick: (Block) -> Unit,
        numberedClick: (Block) -> Unit,
        checkBoxClick: (Block) -> Unit,
        codeClick: (Block) -> Unit
    ) {
        btnText.setClick(textClick, block)
        btnHeader1.setClick(header1Click, block)
        btnHeader2.setClick(header2Click, block)
        btnHeader3.setClick(header3Click, block)
        btnHighlighted.setClick(hightLitedClick, block)
        btnBulleted.setClick(bulletedClick, block)
        btnNumberedList.setClick(numberedClick, block)
        btnCheckbox.setClick(checkBoxClick, block)
        btnCode.setClick(codeClick, block)
    }

    private fun getButton(type: ContentType) =
        when (type) {
            ContentType.P -> btnText
            ContentType.H2 -> btnHeader1
            ContentType.H3 -> btnHeader2
            ContentType.H4 -> btnHeader3
            ContentType.UL -> btnBulleted
            ContentType.Quote -> btnHighlighted
            ContentType.OL -> btnNumberedList
            ContentType.Check -> btnCheckbox
            ContentType.Code -> btnCode
            else -> btnText
        }

    private fun unSelectViews() {
        btnText.isSelected = false
        btnHeader1.isSelected = false
        btnHeader2.isSelected = false
        btnHeader3.isSelected = false
        btnHighlighted.isSelected = false
        btnBulleted.isSelected = false
        btnNumberedList.isSelected = false
        btnCheckbox.isSelected = false
        btnCode.isSelected = false
    }
}

fun View.setClick(click: (Block) -> Unit, block: Block) =
    this.setOnClickListener {

        it.isSelected = !it.isSelected
        click(block)
    }
