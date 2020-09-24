package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget

class HeaderOneBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Text.Header.One

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_header_one_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextInputWidget>(R.id.headerOne).apply {
            enableReadMode()
            setBlockText(this, block.text, block)
            processTextColor(
                textView = this,
                colorImage = colorView,
                color = block.color
            )
        }
        processBackgroundColor(
            root = view.findViewById(R.id.root),
            color = block.backgroundColor,
            bgImage = backgroundView
        )
        setConstraints()
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(
        first = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_size_header_one),
        second = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_padding_header_one)
    )
}