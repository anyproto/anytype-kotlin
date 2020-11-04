package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

class ErrorActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Error

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        if (block.getViewType() == BlockViewHolder.HOLDER_BOOKMARK_ERROR) {
            initBookmarkError(view)
        }
        setConstraints()
    }

    private fun initBookmarkError(view: View) {
        val item = block as BlockView.Error.Bookmark
        view.findViewById<TextView>(R.id.errorBookmarkUrl).apply {
            text = item.url
        }
        view.findViewById<FrameLayout>(R.id.bookmarkErrorRoot)
            .updateLayoutParams<FrameLayout.LayoutParams> {
                this.apply {
                    rightMargin = 0
                }
            }
    }

    override fun getBlock(): BlockView = block

    override fun blockLayout(): Int =
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_FILE_ERROR -> R.layout.item_block_file_error_preview
            BlockViewHolder.HOLDER_BOOKMARK_ERROR -> R.layout.item_block_bookmark_error
            BlockViewHolder.HOLDER_VIDEO_ERROR -> R.layout.item_block_video_error_preview
            BlockViewHolder.HOLDER_PICTURE_ERROR -> R.layout.item_block_picture_error_preview
            else -> R.layout.item_block_file_error_preview
        }
}