package com.agileburo.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE_PLACEHOLDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_VIDEO_PLACEHOLDER

class PlaceholderActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.MediaPlaceholder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            HOLDER_PICTURE_PLACEHOLDER -> R.layout.item_block_picture_placeholder_preview
            HOLDER_VIDEO_PLACEHOLDER -> R.layout.item_block_video_empty_preview
            HOLDER_FILE_PLACEHOLDER -> R.layout.item_block_file_placeholder_preview
            HOLDER_BOOKMARK_PLACEHOLDER -> R.layout.item_block_bookmark_placeholder
            else -> R.layout.item_block_picture_placeholder_preview
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        if (block.getViewType() == HOLDER_BOOKMARK_PLACEHOLDER) {
            initBookmarkPlaceholder(view)
        }
        setConstraints()
    }

    private fun initBookmarkPlaceholder(view: View) {
        view.findViewById<ConstraintLayout>(R.id.bookmarkPlaceholderRoot)
            .updateLayoutParams<LayoutParams> {
                this.apply {
                    rightMargin = 0
                }
            }
    }
}