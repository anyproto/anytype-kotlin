package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.getBlockTextColor
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class HeaderOneBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Text.Header.One

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_header_one
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            setPadding(0,0,0,0)
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
            }
            processBackgroundColor(
                root = this,
                color = block.backgroundColor,
                bgImage = backgroundView
            )
        }
        view.findViewById<TextInputWidget>(R.id.headerOne).apply {
            enableReadMode()
            setBlockText(this, block.text, block, block.getBlockTextColor())
            processTextColor(
                textView = this,
                colorImage = colorView,
                color = block.color
            )
        }
        setConstraints()
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(
        first = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_size_header_one),
        second = resources.getDimensionPixelSize(com.anytypeio.anytype.core_ui.R.dimen.mention_span_image_padding_header_one)
    )
}