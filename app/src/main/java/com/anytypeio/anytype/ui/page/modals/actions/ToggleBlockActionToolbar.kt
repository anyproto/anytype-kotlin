package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.getBlockTextColor
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Toggle
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget

class ToggleBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Text.Toggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_toggle_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<TextInputWidget>(R.id.toggleContent).apply {
            enableReadMode()
            setBlockText(this, block.text, block, block.getBlockTextColor())
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
                if (block.toggled) Toggle.EXPANDED_ROTATION
                else Toggle.COLLAPSED_ROTATION
        }
        setConstraints()
    }
}