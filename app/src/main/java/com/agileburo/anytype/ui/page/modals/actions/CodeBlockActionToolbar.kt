package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView

class CodeBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_code_snippet
    override fun getBlock(): BlockView = block

    override fun initUi(view: View) {
        TODO()
    }
}