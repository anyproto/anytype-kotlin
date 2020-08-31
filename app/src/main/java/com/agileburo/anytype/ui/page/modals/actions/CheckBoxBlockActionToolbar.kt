package com.agileburo.anytype.ui.page.modals.actions

import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.BlockView

class CheckBoxBlockActionToolbar : BlockActionToolbar() {

    val block: BlockView.Text.Checkbox
        get() = arguments?.getParcelable(ARG_BLOCK) ?: throw IllegalStateException()

    override fun blockLayout() = R.layout.item_block_checkbox_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextView>(R.id.checkboxContent).apply {
            movementMethod = ScrollingMovementMethod()
            text = block.toSpannable()
            if (block.isChecked) {
                setTextColor(requireContext().color(R.color.checkbox_state_checked))
            } else {
                processTextColor(
                    textView = this,
                    colorImage = colorView,
                    color = block.color
                )
            }
        }
        view.findViewById<ImageView>(R.id.checkboxIcon).apply {
            isActivated = block.isChecked
        }
        processBackgroundColor(
            root = view.findViewById(R.id.root),
            color = block.backgroundColor,
            bgImage = backgroundView
        )
        setConstraints()
    }
}