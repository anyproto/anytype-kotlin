package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_PICTURE_PLACEHOLDER
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class PlaceholderActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.MediaPlaceholder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            HOLDER_PICTURE_PLACEHOLDER -> R.layout.item_block_picture_placeholder
            HOLDER_VIDEO_PLACEHOLDER -> R.layout.item_block_video_placeholder
            HOLDER_FILE_PLACEHOLDER -> R.layout.item_block_file_placeholder
            HOLDER_BOOKMARK_PLACEHOLDER -> R.layout.item_block_bookmark_placeholder
            else -> R.layout.item_block_picture_placeholder
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        view.findViewById<FrameLayout>(R.id.root)
            .updateLayoutParams<LayoutParams> {
                topMargin = 0
                bottomMargin = 0
                leftMargin = 0
                rightMargin = 0
            }
        setConstraints()
    }
}