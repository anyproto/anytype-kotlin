package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.ext.setReadOnly

class HighlightBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Highlight

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_highlight
    override fun getBlock(): BlockView = block

    override fun initUi(view: View) {
        view.findViewById<EditText>(R.id.highlightContent).apply {
            setReadOnly(true)
            movementMethod = ScrollingMovementMethod()
            setText(block.text)
        }
    }
}