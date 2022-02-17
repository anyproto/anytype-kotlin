package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockLatexBinding
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.judemanutd.katexview.KatexView

class Latex(
    val binding: ItemBlockLatexBinding
) : BlockViewHolder(binding.root), BlockViewHolder.IndentableHolder {

    val content: View = binding.latexContainer
    val latexView: KatexView = binding.latexView

    init {
        latexView.setTextColor(
            itemView.resources.getColor(R.color.text_primary, null)
        )
    }

    fun bind(item: BlockView.Latex) {
        indentize(item)
        setIsSelected(item)
        setLatex(item.latex)
        setBackground(item.backgroundColor)
    }

    private fun setIsSelected(item: BlockView.Latex) {
        binding.latexContainer.isSelected = item.isSelected
    }

    private fun setLatex(latex: String) {
        val encode = "$$ $latex $$"
        binding.latexView.setText(encode)
    }

    private fun setBackground(backgroundColor: String?) {
        val value = ThemeColor.values().find { it.title == backgroundColor }
        if (value != null && value != ThemeColor.DEFAULT) {
            itemView.setBackgroundColor(itemView.resources.lighter(value, 0))
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
        binding.latexView.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}