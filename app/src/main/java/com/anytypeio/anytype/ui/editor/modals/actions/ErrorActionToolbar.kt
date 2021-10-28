package com.anytypeio.anytype.ui.editor.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_BOOKMARK_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_FILE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PICTURE_ERROR
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_VIDEO_ERROR

class ErrorActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Error

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        if (block.getViewType() == HOLDER_BOOKMARK_ERROR) {
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
            HOLDER_FILE_ERROR -> R.layout.item_block_file_error_preview
            HOLDER_BOOKMARK_ERROR -> R.layout.item_block_bookmark_error
            HOLDER_VIDEO_ERROR -> R.layout.item_block_video_error_preview
            HOLDER_PICTURE_ERROR -> R.layout.item_block_picture_error_preview
            else -> R.layout.item_block_file_error_preview
        }
}