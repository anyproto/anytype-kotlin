package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

class UploadActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Upload

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_PICTURE_UPLOAD -> R.layout.item_block_picture_uploading_preview
            BlockViewHolder.HOLDER_VIDEO_UPLOAD -> R.layout.item_block_video_uploading_preview
            BlockViewHolder.HOLDER_FILE_UPLOAD -> R.layout.item_block_file_uploading_preview
            else -> R.layout.item_block_picture_uploading_preview
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        setConstraints()
    }
}