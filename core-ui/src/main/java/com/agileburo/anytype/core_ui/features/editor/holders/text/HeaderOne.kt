package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.view.View
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_header_one.view.*

class HeaderOne(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Header(view) {

    override val header: TextInputWidget = itemView.headerOne
    override val content: TextInputWidget get() = header
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.HEADER)
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_one),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_one)
        )
    }
}