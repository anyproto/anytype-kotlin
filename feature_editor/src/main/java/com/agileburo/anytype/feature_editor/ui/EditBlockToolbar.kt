package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentParam
import com.agileburo.anytype.feature_editor.domain.ContentType
import kotlinx.android.synthetic.main.view_edit_block_toolbar.view.*

class EditBlockToolbar : ConstraintLayout {

    private var block = Block(
        id = "",
        parentId = "",
        contentType = ContentType.P,
        content = Content.Text(
            text = "",
            marks = emptyList(),
            param = ContentParam.empty()
        )
    )

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

    fun show(initialBlock: Block, typesToHide: Set<ContentType>) {
        this.block = initialBlock
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
        header4Click: (Block) -> Unit,
        hightLitedClick: (Block) -> Unit,
        bulletedClick: (Block) -> Unit,
        numberedClick: (Block) -> Unit,
        checkBoxClick: (Block) -> Unit,
        codeClick: (Block) -> Unit,
        archiveClick: (String) -> Unit
    ) {
        setClick(btnText, textClick)
        setClick(btnHeader1, header1Click)
        setClick(btnHeader2, header2Click)
        setClick(btnHeader3, header3Click)
        setClick(btnHeader4, header4Click)
        setClick(btnHighlighted, hightLitedClick)
        setClick(btnBulleted, bulletedClick)
        setClick(btnNumberedList, numberedClick)
        setClick(btnCheckbox, checkBoxClick)
        setClick(btnCode, codeClick)
        setArchiveClick(btnArchive, archiveClick)
    }

    private fun setClick(view: View, click: (Block) -> Unit) {
        view.setOnClickListener {
            it.isSelected = !it.isSelected
            click(block)
        }
    }

    private fun setArchiveClick(view: View, click: (String) -> Unit) {
        view.setOnClickListener {
            it.isSelected = !it.isSelected
            click(block.id)
        }
    }

    private fun getButton(type: ContentType) =
        when (type) {
            ContentType.P -> btnText
            ContentType.H1 -> btnHeader1
            ContentType.H2 -> btnHeader2
            ContentType.H3 -> btnHeader3
            ContentType.H4 -> btnHeader4
            ContentType.UL -> btnBulleted
            ContentType.Quote -> btnHighlighted
            ContentType.NumberedList -> btnNumberedList
            ContentType.Check -> btnCheckbox
            ContentType.Code -> btnCode
            else -> btnText
        }

    private fun unSelectViews() {
        btnText.isSelected = false
        btnHeader1.isSelected = false
        btnHeader2.isSelected = false
        btnHeader3.isSelected = false
        btnHeader4.isSelected = false
        btnHighlighted.isSelected = false
        btnBulleted.isSelected = false
        btnNumberedList.isSelected = false
        btnCheckbox.isSelected = false
        btnCode.isSelected = false
        btnArchive.isSelected = false
    }
}