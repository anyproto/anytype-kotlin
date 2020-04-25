package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.ext.addDot
import com.agileburo.anytype.core_utils.ext.setReadOnly

class NumberedBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Numbered

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_numbered
    override fun getBlock(): BlockView = block

    override fun initUi(view: View) {
        view.findViewById<TextView>(R.id.number).apply {
            gravity = when (block.number) {
                in 1..19 -> Gravity.CENTER_HORIZONTAL
                else -> Gravity.START
            }
            text = block.number.addDot()
        }
        view.findViewById<EditText>(R.id.numberedListContent).apply {
            setReadOnly(true)
            movementMethod = ScrollingMovementMethod()
            setText(block.toSpannable(), TextView.BufferType.SPANNABLE)
            val textColor = block.color
            if (textColor != null) {
                setBlockTextColor(this, textColor)
            } else {
                setTextColor(context.color(R.color.black))
            }
        }
    }
}