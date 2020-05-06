package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView

class PageBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Page
    lateinit var icon: ImageView
    lateinit var emoji: TextView
    lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() = R.layout.item_block_page_preview
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        icon = view.findViewById(R.id.pageIcon)
        emoji = view.findViewById(R.id.emoji)
        title = view.findViewById(R.id.pageTitle)

        title.text = if (block.text.isNullOrEmpty()) getString(R.string.untitled) else block.text
        when {
            block.emoji != null -> emoji.text = block.emoji
            block.isEmpty -> icon.setImageResource(R.drawable.ic_block_empty_page)
            else -> icon.setImageResource(R.drawable.ic_block_page_without_emoji)
        }
    }
}