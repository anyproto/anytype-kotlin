package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.getBlockTextColor
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.addDot
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class NumberedBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Text.Numbered

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_numbered_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextView>(R.id.number).apply {
            gravity = when (block.number) {
                in 1..19 -> Gravity.CENTER_HORIZONTAL
                else -> Gravity.START
            }
            text = block.number.addDot()
            processTextColor(
                textView = this,
                colorImage = colorView,
                color = block.color
            )
        }
        view.findViewById<TextInputWidget>(R.id.numberedListContent).apply {
            enableReadMode()
            setBlockText(this, block.text, block, block.getBlockTextColor())
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
}