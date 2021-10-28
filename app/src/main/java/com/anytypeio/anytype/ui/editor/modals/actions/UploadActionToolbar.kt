package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_UPLOAD
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_UPLOAD

class UploadActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Upload

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            HOLDER_PICTURE_UPLOAD -> R.layout.item_block_picture_uploading_preview
            HOLDER_VIDEO_UPLOAD -> R.layout.item_block_video_uploading_preview
            HOLDER_FILE_UPLOAD -> R.layout.item_block_file_uploading_preview
            else -> R.layout.item_block_picture_uploading_preview
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        setConstraints()
    }
}