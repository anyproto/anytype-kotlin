package com.agileburo.anytype.ui.page.modals.actions

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.ext.setReadOnly

class HeaderThreeBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.HeaderThree

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_header_three
    override fun getBlock(): BlockView = block

    override fun initUi(view: View) {
        view.findViewById<EditText>(R.id.headerThree).apply {
            setReadOnly(true)
            setText(block.text)
            val textColor = block.color
            if (textColor != null) {
                setBlockTextColor(this, textColor)
            } else {
                setTextColor(context.color(R.color.black))
            }
        }
    }
}