package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class DividerLineBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.DividerLine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        setConstraints()
    }
    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_divider_line
}

class DividerDotsBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.DividerDots

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        setConstraints()
    }
    override fun getBlock(): BlockView = block
    override fun blockLayout(): Int = R.layout.item_block_divider_dots
}