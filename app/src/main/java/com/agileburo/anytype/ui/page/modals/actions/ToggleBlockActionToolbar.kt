package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder

class ToggleBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Toggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_toggle_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextView>(R.id.toggleContent).apply {
            movementMethod = ScrollingMovementMethod()
            text = block.toSpannable()
            processTextColor(
                textView = this,
                colorImage = colorView,
                color = block.color,
                imageView = view.findViewById(R.id.toggle)
            )
        }
        processBackgroundColor(
            root = view.findViewById(R.id.root),
            color = block.backgroundColor,
            bgImage = backgroundView
        )
        view.findViewById<ImageView>(R.id.toggle).apply {
            rotation =
                if (block.toggled) BlockViewHolder.Toggle.EXPANDED_ROTATION
                else BlockViewHolder.Toggle.COLLAPSED_ROTATION
        }
        setConstraints()
    }
}