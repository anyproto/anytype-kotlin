package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.View
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_header_three.view.*

class HeaderThree(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Header(view) {

    override val header: TextInputWidget = itemView.headerThree
    override val content: TextInputWidget get() = header
    override val root: View = itemView

    init {
        setup(onContextMenuStyleClick)
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_three),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }
}