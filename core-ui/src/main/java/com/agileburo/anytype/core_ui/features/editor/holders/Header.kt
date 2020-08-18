package com.agileburo.anytype.core_ui.features.editor.holders

import android.view.View
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen

abstract class Header(
    view: View
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder {

    abstract val header: TextInputWidget

    fun setBlockTextColor(color: String?) {
        if (color != null)
            setTextColor(color)
        else
            setTextColor(content.context.color(R.color.black))
    }

    fun setLinksClickable(block: BlockView.HeaderOne) {
        if (block.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }

    fun setLinksClickable(block: BlockView.HeaderTwo) {
        if (block.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }

    fun setLinksClickable(block: BlockView.HeaderThree) {
        if (block.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        header.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}