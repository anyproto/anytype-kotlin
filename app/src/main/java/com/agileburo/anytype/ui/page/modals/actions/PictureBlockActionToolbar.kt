package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.bumptech.glide.Glide

class PictureBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Picture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_PICTURE -> R.layout.item_block_picture_preview
            BlockViewHolder.HOLDER_PICTURE_PLACEHOLDER -> R.layout.item_block_picture_placeholder_preview
            BlockViewHolder.HOLDER_PICTURE_ERROR -> R.layout.item_block_picture_error_preview
            else -> R.layout.item_block_picture_uploading_preview
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_PICTURE -> initPicture(view)
            else -> Unit
        }
    }

    private fun initPicture(view: View) {
        val item = block as BlockView.Picture.View
        view.findViewById<ImageView>(R.id.image).apply {
            Glide.with(this).load(item.url).into(this)
        }
        setConstraints()
    }
}