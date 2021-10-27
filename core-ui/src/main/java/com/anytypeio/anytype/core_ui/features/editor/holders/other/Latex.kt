package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.judemanutd.katexview.KatexView
import kotlinx.android.synthetic.main.item_block_latex.view.*

class Latex(
    parent: ViewGroup
) : BlockViewHolder(
    view = LayoutInflater.from(parent.context).inflate(
        R.layout.item_block_latex,
        parent,
        false
    )
), BlockViewHolder.IndentableHolder {

    val content : View = itemView.latexContainer
    val latexView: KatexView = itemView.latexView

    fun bind(item: BlockView.Latex) {
        indentize(item)
        setIsSelected(item)
        setLatex(item.latex)
        setBackground(item.backgroundColor)
    }

    private fun setIsSelected(item: BlockView.Latex) {
        itemView.latexContainer.isSelected = item.isSelected
    }

    private fun setLatex(latex: String) {
        val encode = "$$ $latex $$"
        itemView.latexView.setText(encode)
    }

    private fun setBackground(backgroundColor: String?) {
        val value = ThemeColor.values().find { it.title == backgroundColor }
        if (value != null) {
            itemView.setBackgroundColor(value.background)
        } else {
            itemView.background = null
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Latex
    ) {
        payloads.forEach { payload ->
            if (payload.isLatexChanged) setLatex(item.latex)
            if (payload.isSelectionChanged) setIsSelected(item)
            if (payload.isBackgroundColorChanged) setBackground(item.backgroundColor)
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.latexView.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}