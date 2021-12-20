package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

@Deprecated("Legacy")
class PageBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.LinkToObject.Default
    lateinit var emoji: ImageView
    lateinit var image: ImageView
    lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_page_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {

        image = view.findViewById(R.id.linkImage)
        emoji = view.findViewById(R.id.linkEmoji)
        title = view.findViewById(R.id.pageTitle)

        title.text = if (block.text.isNullOrEmpty()) getString(R.string.untitled) else block.text
        setConstraints()
    }
}