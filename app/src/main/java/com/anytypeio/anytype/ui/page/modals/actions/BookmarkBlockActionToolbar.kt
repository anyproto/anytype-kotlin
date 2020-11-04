package com.anytypeio.anytype.ui.page.modals.actions

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.bumptech.glide.Glide

class BookmarkBlockActionToolbar : BlockActionToolbar() {

    lateinit var block: BlockView.Media.Bookmark

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout(): Int = R.layout.item_block_bookmark
    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        initBookmark(view)
        setConstraints()
    }

    override fun getBlockPaddingTop(): Int = 0
    override fun getBlockPaddingBottom(): Int = 0

    private fun initBookmark(view: View) {
        val item = block
        view.findViewById<TextView>(R.id.bookmarkTitle).apply {
            text = item.title
        }
        view.findViewById<TextView>(R.id.bookmarkDescription).apply {
            text = item.description
        }
        view.findViewById<TextView>(R.id.bookmarkUrl).apply {
            text = item.url
        }
        item.imageUrl?.let { url ->
            view.findViewById<ImageView>(R.id.bookmarkImage).apply {
                Glide.with(this)
                    .load(url)
                    .into(this)
            }
        }
        item.faviconUrl?.let { url ->
            view.findViewById<ImageView>(R.id.bookmarkLogo).apply {
                Glide.with(this)
                    .load(url)
                    .into(this)
            }
        }

        view.findViewById<FrameLayout>(R.id.root).apply {
            updateLayoutParams<FrameLayout.LayoutParams> {
                leftMargin = 0
                topMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }
            setPadding(0, 0, 0, 0)
        }

        view.findViewById<FrameLayout>(R.id.block_container)
            .setPadding(
                resources.getDimensionPixelOffset(R.dimen.dp_12),
                resources.getDimensionPixelOffset(R.dimen.dp_6),
                resources.getDimensionPixelOffset(R.dimen.dp_12),
                resources.getDimensionPixelOffset(R.dimen.dp_6))
    }
}