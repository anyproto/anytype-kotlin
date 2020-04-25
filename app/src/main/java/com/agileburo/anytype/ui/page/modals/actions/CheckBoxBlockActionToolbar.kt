package com.agileburo.anytype.ui.page.modals.actions

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.ext.setReadOnly

class CheckBoxBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Checkbox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_checkbox
    override fun getBlock(): BlockView = block

    override fun initUi(view: View) {
        view.findViewById<EditText>(R.id.checkboxContent).apply {
            setReadOnly(true)
            movementMethod = ScrollingMovementMethod()
            updateTextColor(
                context = requireContext(),
                view = this,
                isSelected = block.isChecked
            )
            if (block.marks.isNotEmpty())
                setText(block.toSpannable(), TextView.BufferType.SPANNABLE)
            else
                setText(block.text)
        }
        view.findViewById<ImageView>(R.id.checkboxIcon).apply {
            isSelected = block.isChecked
        }
    }

    private fun updateTextColor(context: Context, view: TextView, isSelected: Boolean) =
        view.setTextColor(
            context.color(
                if (isSelected) R.color.grey_50 else R.color.black
            )
        )
}