package com.anytypeio.anytype.ui.page.modals.actions

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ThemeColorCode
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.widgets.text.CodeTextInputWidget

class CodeBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_code_snippet
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
                leftMargin = 0
                rightMargin = 0
            }
        }
        view.findViewById<CodeTextInputWidget>(R.id.snippet).apply {
            enableReadMode()
            setText(block.text)
        }
        view.findViewById<LinearLayout>(R.id.snippetContainer).apply {
            if (block.backgroundColor != null) {
                val value =
                    ThemeColorCode.values().find { value -> value.title == block.backgroundColor }
                if (value != null) {
                    (background as? GradientDrawable)?.setColor(value.background)
                }
            }
        }
        setConstraints()
    }
}