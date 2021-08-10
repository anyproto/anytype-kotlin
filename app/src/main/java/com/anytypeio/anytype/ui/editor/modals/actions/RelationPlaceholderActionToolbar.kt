package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class RelationPlaceholderActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Relation.Placeholder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                topMargin = 0
                bottomMargin = 0
                leftMargin = 0
                rightMargin = 0
            }
            setPadding(0, 0, 0, 0)
        }
        setConstraints()
    }

    override fun getBlock(): BlockView = block

    override fun blockLayout(): Int = R.layout.item_block_relation_placeholder
}