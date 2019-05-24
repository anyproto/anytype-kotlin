package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.*
import kotlinx.android.synthetic.main.view_edit_block_toolbar.view.*
import timber.log.Timber

class EditBlockToolbar : ConstraintLayout {

    private var block = Block(
        id = "",
        parentId = "",
        contentType = ContentType.P,
        content = Content.Text(
            text = "",
            marks = emptyList(),
            param = ContentParam.empty()
        ),
        blockType = BlockType.Editable
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
        archiveClick: (String) -> Unit,
        outsideClickListener: () -> Unit
    ) {
        setClick(btn_cont_type_toolbar_p, textClick)
        setClick(btn_cont_type_toolbar_h1, header1Click)
        setClick(btn_cont_type_toolbar_h2, header2Click)
        setClick(btn_cont_type_toolbar_h3, header3Click)
        setClick(btn_cont_type_toolbar_h4, header4Click)
        setClick(btn_cont_type_toolbar_quote, hightLitedClick)
        setClick(btn_cont_type_toolbar_bullet, bulletedClick)
        setClick(btn_cont_type_toolbar_numbered, numberedClick)
        setClick(btn_cont_type_toolbar_checkbox, checkBoxClick)
        setClick(btn_cont_type_toolbar_code, codeClick)
        setArchiveClick(btn_cont_type_toolbar_archive, archiveClick)
        setOutsideListener(outsideClickListener)
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

    private fun setOutsideListener(outsideClickListener: () -> Unit) =
        outside_toolbar.setOnClickListener { outsideClickListener.invoke() }

    private fun getButton(type: ContentType) =
        when (type) {
            ContentType.P -> btn_cont_type_toolbar_p
            ContentType.H1 -> btn_cont_type_toolbar_h1
            ContentType.H2 -> btn_cont_type_toolbar_h2
            ContentType.H3 -> btn_cont_type_toolbar_h3
            ContentType.H4 -> btn_cont_type_toolbar_h4
            ContentType.UL -> btn_cont_type_toolbar_bullet
            ContentType.Quote -> btn_cont_type_toolbar_quote
            ContentType.NumberedList -> btn_cont_type_toolbar_numbered
            ContentType.Check -> btn_cont_type_toolbar_checkbox
            ContentType.Code -> btn_cont_type_toolbar_code
            else -> btn_cont_type_toolbar_p
        }

    private fun unSelectViews() {
        btn_cont_type_toolbar_p.isSelected = false
        btn_cont_type_toolbar_h1.isSelected = false
        btn_cont_type_toolbar_h2.isSelected = false
        btn_cont_type_toolbar_h3.isSelected = false
        btn_cont_type_toolbar_h4.isSelected = false
        btn_cont_type_toolbar_quote.isSelected = false
        btn_cont_type_toolbar_bullet.isSelected = false
        btn_cont_type_toolbar_numbered.isSelected = false
        btn_cont_type_toolbar_checkbox.isSelected = false
        btn_cont_type_toolbar_code.isSelected = false
        btn_cont_type_toolbar_archive.isSelected = false
    }
}